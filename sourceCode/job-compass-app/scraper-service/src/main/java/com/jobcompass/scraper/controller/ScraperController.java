package com.jobcompass.scraper.controller;

import com.jobcompass.common.events.RawJobEvent;
import com.jobcompass.common.model.ScrapeParameters;
import com.jobcompass.scraper.dto.ScrapeRequest;
import com.jobcompass.scraper.kafka.RawJobProducer;
import com.jobcompass.scraper.scrapers.LinkedInScraper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * REST controller for manual scraping triggers (for testing).
 * 
 * @author Palraj Jayaraj
 */
@RestController
@CrossOrigin(origins = "*") // Allow requests from any origin (Web UI is on 8085)
@RequestMapping("/api/scraper")
@Slf4j
public class ScraperController {

    private final LinkedInScraper linkedInScraper;
    private final RawJobProducer rawJobProducer;

    public ScraperController(LinkedInScraper linkedInScraper, RawJobProducer rawJobProducer) {
        this.linkedInScraper = linkedInScraper;
        this.rawJobProducer = rawJobProducer;
    }

    /**
     * Trigger LinkedIn scraping manually (Legacy - Query Params)
     */
    @PostMapping("/trigger/linkedin")
    public List<RawJobEvent> scrapeLinkedIn(
            @RequestParam(defaultValue = "7") int maxJobAgeDays,
            @RequestParam(defaultValue = "5") int maxResults,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String location) {
        ScrapeParameters params = ScrapeParameters.withFilters(
                maxJobAgeDays,
                maxResults,
                skill,
                location);

        return linkedInScraper.scrapeJobs(params);
    }

    /**
     * Trigger LinkedIn scraping for multiple skills (New - JSON Body)
     * Execute asynchronously to prevent request timeout
     */
    @PostMapping("/trigger/multi-skill")
    public String scrapeMultipleSkills(@RequestBody ScrapeRequest request) {
        log.info("Received multi-skill scrape request: {}", request);

        // Run scraping in background thread
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                List<RawJobEvent> allResults = new ArrayList<>();
                List<String> skills = request.getSkills() != null ? request.getSkills() : Collections.emptyList();

                if (skills.isEmpty()) {
                    log.warn("No skills provided, performing general search");
                    ScrapeParameters params = ScrapeParameters.withAuth(
                            request.getMaxJobAgeDays(),
                            request.getMaxResults(),
                            null,
                            request.getLocation(),
                            request.getAuthCookie());
                    List<RawJobEvent> results = linkedInScraper.scrapeJobs(params);
                    publishJobsToKafka(results);
                    return;
                }

                for (String skill : skills) {
                    try {
                        log.info("Scraping for skill: {}", skill);
                        ScrapeParameters params = ScrapeParameters.withAuth(
                                request.getMaxJobAgeDays(),
                                request.getMaxResults(),
                                skill.trim(),
                                request.getLocation(),
                                request.getAuthCookie());

                        List<RawJobEvent> results = linkedInScraper.scrapeJobs(params);
                        allResults.addAll(results);

                        // Add delay between skills to avoid rate limiting
                        if (skills.indexOf(skill) < skills.size() - 1) {
                            Thread.sleep(5000);
                        }
                    } catch (Exception e) {
                        log.error("Error scraping for skill: {}", skill, e);
                    }
                }

                // Publish all scraped jobs to Kafka for downstream processing
                publishJobsToKafka(allResults);
                log.info("Completed multi-skill scraping. Total jobs found: {}", allResults.size());

            } catch (Exception e) {
                log.error("Error in async scraping process", e);
            }
        });

        return "{\"status\": \"Scraping started\", \"message\": \"Jobs will be processed in background.\"}";
    }

    /**
     * Publish scraped jobs to Kafka for storage and processing
     */
    private void publishJobsToKafka(List<RawJobEvent> jobs) {
        for (RawJobEvent job : jobs) {
            rawJobProducer.publishRawJob(job);
        }
        log.info("Published {} jobs to Kafka", jobs.size());
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public String health() {
        return "Scraper Service is running!";
    }
}
