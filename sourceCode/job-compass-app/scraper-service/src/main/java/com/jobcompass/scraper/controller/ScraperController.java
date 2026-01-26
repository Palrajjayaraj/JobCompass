package com.jobcompass.scraper.controller;

import com.jobcompass.common.events.RawJobEvent;
import com.jobcompass.common.model.ScrapeParameters;
import com.jobcompass.scraper.scrapers.LinkedInScraper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for manual scraping triggers (for testing).
 * 
 * @author Palraj Jayaraj
 */
@RestController
@CrossOrigin(origins = "*") // Allow requests from any origin (Web UI is on 8085)
@RequestMapping("/api/scraper")
public class ScraperController {

    private final LinkedInScraper linkedInScraper;

    public ScraperController(LinkedInScraper linkedInScraper) {
        this.linkedInScraper = linkedInScraper;
    }

    /**
     * Trigger LinkedIn scraping manually
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
     * Health check
     */
    @GetMapping("/health")
    public String health() {
        return "Scraper Service is running!";
    }
}
