package com.jobcompass.scraper.controller;

import com.jobcompass.common.events.RawJobEvent;
import com.jobcompass.common.model.ScrapeParameters;
import com.jobcompass.scraper.dto.ScrapeProgressEvent;
import com.jobcompass.scraper.dto.ScrapeRequest;
import com.jobcompass.scraper.kafka.RawJobProducer;
import com.jobcompass.scraper.scrapers.LinkedInScraper;
import com.jobcompass.scraper.service.ScrapeProgressTracker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.*;

/**
 * REST controller for manual scraping triggers.
 * Supports SSE-based real-time progress tracking.
 *
 * @author Palraj Jayaraj
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/scraper")
@Slf4j
public class ScraperController {

    private final LinkedInScraper linkedInScraper;
    private final RawJobProducer rawJobProducer;
    private final ScrapeProgressTracker progressTracker;

    public ScraperController(LinkedInScraper linkedInScraper,
            RawJobProducer rawJobProducer,
            ScrapeProgressTracker progressTracker) {
        this.linkedInScraper = linkedInScraper;
        this.rawJobProducer = rawJobProducer;
        this.progressTracker = progressTracker;
    }

    /**
     * Trigger LinkedIn scraping manually (Legacy - Query Params).
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
     * Trigger LinkedIn scraping for multiple skills (New - JSON Body).
     * Returns a scrapeId for SSE progress tracking.
     */
    @PostMapping("/trigger/multi-skill")
    public Map<String, String> scrapeMultipleSkills(@RequestBody ScrapeRequest request) {
        String scrapeId = UUID.randomUUID().toString();
        log.info("Received multi-skill scrape request [{}]: {}", scrapeId, request);

        List<String> skills = request.getSkills() != null ? request.getSkills() : Collections.emptyList();
        int totalSkills = skills.isEmpty() ? 1 : skills.size();

        // Initialize event buffer BEFORE starting async work
        progressTracker.initSession(scrapeId);

        // Run scraping in background thread with progress tracking
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // Emit STARTED (will be buffered until client connects)
                progressTracker.emitProgress(scrapeId, ScrapeProgressEvent.builder()
                        .scrapeId(scrapeId)
                        .status(ScrapeProgressEvent.Status.STARTED)
                        .totalSkills(totalSkills)
                        .message("Scrape started for " + totalSkills + " skill(s)")
                        .build());

                List<RawJobEvent> allResults = new ArrayList<>();
                Set<String> seenUrls = new HashSet<>();
                int totalDuplicates = 0;
                int totalErrors = 0;

                if (skills.isEmpty()) {
                    log.warn("No skills provided, performing general search");
                    progressTracker.emitProgress(scrapeId, ScrapeProgressEvent.builder()
                            .scrapeId(scrapeId)
                            .status(ScrapeProgressEvent.Status.SCRAPING)
                            .currentSkill("General")
                            .skillIndex(1)
                            .totalSkills(1)
                            .message("Performing general search...")
                            .build());

                    ScrapeParameters params = ScrapeParameters.withAuthLegacy(
                            request.getMaxJobAgeDays(),
                            request.getMaxResults(),
                            null,
                            request.getLocation(),
                            request.getAuthentication());
                    List<RawJobEvent> results = linkedInScraper.scrapeJobs(params);

                    // Deduplicate
                    int dupsForSkill = 0;
                    for (RawJobEvent job : results) {
                        if (job.getUrl() != null && seenUrls.add(job.getUrl())) {
                            allResults.add(job);
                        } else {
                            dupsForSkill++;
                        }
                    }
                    totalDuplicates += dupsForSkill;

                    progressTracker.emitProgress(scrapeId, ScrapeProgressEvent.builder()
                            .scrapeId(scrapeId)
                            .status(ScrapeProgressEvent.Status.SKILL_COMPLETED)
                            .currentSkill("General")
                            .skillIndex(1)
                            .totalSkills(1)
                            .jobsFoundForSkill(results.size() - dupsForSkill)
                            .totalJobsFound(allResults.size())
                            .duplicateJobsSkipped(totalDuplicates)
                            .message("General search complete: " + (results.size() - dupsForSkill) + " unique jobs ("
                                    + dupsForSkill + " duplicates)")
                            .build());
                } else {
                    for (int i = 0; i < skills.size(); i++) {
                        String skill = skills.get(i).trim();
                        int skillNum = i + 1;

                        try {
                            // Emit SCRAPING for this skill
                            progressTracker.emitProgress(scrapeId, ScrapeProgressEvent.builder()
                                    .scrapeId(scrapeId)
                                    .status(ScrapeProgressEvent.Status.SCRAPING)
                                    .currentSkill(skill)
                                    .skillIndex(skillNum)
                                    .totalSkills(totalSkills)
                                    .totalJobsFound(allResults.size())
                                    .duplicateJobsSkipped(totalDuplicates)
                                    .message("Scraping \"" + skill + "\" in \"" + request.getLocation() + "\"...")
                                    .build());

                            log.info("[{}] Scraping skill {}/{}: {}", scrapeId, skillNum, totalSkills, skill);
                            ScrapeParameters params = ScrapeParameters.withAuthLegacy(
                                    request.getMaxJobAgeDays(),
                                    request.getMaxResults(),
                                    skill,
                                    request.getLocation(),
                                    request.getAuthentication());

                            List<RawJobEvent> results = linkedInScraper.scrapeJobs(params);

                            // Deduplicate across skills
                            int dupsForSkill = 0;
                            for (RawJobEvent job : results) {
                                if (job.getUrl() != null && seenUrls.add(job.getUrl())) {
                                    allResults.add(job);
                                } else {
                                    dupsForSkill++;
                                }
                            }
                            totalDuplicates += dupsForSkill;
                            int uniqueForSkill = results.size() - dupsForSkill;

                            // Emit SKILL_COMPLETED
                            progressTracker.emitProgress(scrapeId, ScrapeProgressEvent.builder()
                                    .scrapeId(scrapeId)
                                    .status(ScrapeProgressEvent.Status.SKILL_COMPLETED)
                                    .currentSkill(skill)
                                    .skillIndex(skillNum)
                                    .totalSkills(totalSkills)
                                    .jobsFoundForSkill(uniqueForSkill)
                                    .totalJobsFound(allResults.size())
                                    .duplicateJobsSkipped(totalDuplicates)
                                    .message("\"" + skill + "\" complete: " + uniqueForSkill + " unique jobs"
                                            + (dupsForSkill > 0 ? " (" + dupsForSkill + " duplicates skipped)" : ""))
                                    .build());

                            // Rate limit between skills
                            if (i < skills.size() - 1) {
                                Thread.sleep(5000);
                            }
                        } catch (Exception e) {
                            totalErrors++;
                            log.error("[{}] Error scraping skill: {}", scrapeId, skill, e);

                            progressTracker.emitProgress(scrapeId, ScrapeProgressEvent.builder()
                                    .scrapeId(scrapeId)
                                    .status(ScrapeProgressEvent.Status.SCRAPING)
                                    .currentSkill(skill)
                                    .skillIndex(skillNum)
                                    .totalSkills(totalSkills)
                                    .errors(totalErrors)
                                    .totalJobsFound(allResults.size())
                                    .duplicateJobsSkipped(totalDuplicates)
                                    .message("Error scraping \"" + skill + "\": " + e.getMessage())
                                    .build());
                        }
                    }
                }

                // Emit PUBLISHING
                progressTracker.emitProgress(scrapeId, ScrapeProgressEvent.builder()
                        .scrapeId(scrapeId)
                        .status(ScrapeProgressEvent.Status.PUBLISHING)
                        .totalJobsFound(allResults.size())
                        .totalSkills(totalSkills)
                        .duplicateJobsSkipped(totalDuplicates)
                        .errors(totalErrors)
                        .message("Publishing " + allResults.size() + " unique jobs to Kafka...")
                        .build());

                // Publish to Kafka (only unique jobs)
                publishJobsToKafka(allResults);

                // Emit COMPLETED
                progressTracker.emitProgress(scrapeId, ScrapeProgressEvent.builder()
                        .scrapeId(scrapeId)
                        .status(ScrapeProgressEvent.Status.COMPLETED)
                        .totalJobsFound(allResults.size())
                        .totalSkills(totalSkills)
                        .duplicateJobsSkipped(totalDuplicates)
                        .errors(totalErrors)
                        .message("Scrape complete! " + allResults.size() + " unique jobs found"
                                + (totalDuplicates > 0 ? ", " + totalDuplicates + " duplicates skipped" : "")
                                + (totalErrors > 0 ? ", " + totalErrors + " errors" : ""))
                        .build());

                log.info("[{}] Scrape completed. Total unique: {}, Duplicates: {}, Errors: {}",
                        scrapeId, allResults.size(), totalDuplicates, totalErrors);

            } catch (Exception e) {
                log.error("[{}] Fatal error in scraping process", scrapeId, e);

                progressTracker.emitProgress(scrapeId, ScrapeProgressEvent.builder()
                        .scrapeId(scrapeId)
                        .status(ScrapeProgressEvent.Status.FAILED)
                        .message("Scrape failed: " + e.getMessage())
                        .build());
            } finally {
                // Give client a moment to receive the last event, then close
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                progressTracker.completeSession(scrapeId);
            }
        });

        Map<String, String> response = new LinkedHashMap<>();
        response.put("scrapeId", scrapeId);
        response.put("status", "Scraping started");
        response.put("message", "Connect to /api/scraper/progress/" + scrapeId + " for live updates");
        return response;
    }

    /**
     * SSE endpoint for real-time scrape progress tracking.
     *
     * @param scrapeId the scrape session ID returned by trigger endpoint
     * @return SseEmitter streaming progress events
     */
    @GetMapping(value = "/progress/{scrapeId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(@PathVariable String scrapeId) {
        log.info("SSE client connected for scrapeId: {}", scrapeId);
        return progressTracker.createSession(scrapeId);
    }

    /**
     * Publish scraped jobs to Kafka for storage and processing.
     */
    private void publishJobsToKafka(List<RawJobEvent> jobs) {
        for (RawJobEvent job : jobs) {
            rawJobProducer.publishRawJob(job);
        }
        log.info("Published {} jobs to Kafka", jobs.size());
    }

    /**
     * Health check.
     */
    @GetMapping("/health")
    public String health() {
        return "Scraper Service is running!";
    }
}
