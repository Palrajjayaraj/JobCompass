package com.jobcompass.scraper.scheduler;

import com.jobcompass.common.model.ScrapeParameters;
import com.jobcompass.scraper.scrapers.LinkedInScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Scheduled task to trigger job scraping automatically.
 * Runs daily at a configured time.
 * 
 * @author Palrajjayaraj
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledScraper {

    private final LinkedInScraper linkedInScraper;

    @Value("${app.scheduler.skills:Java,Spring Boot,React,Angular,Python,Docker,Kubernetes}")
    private String defaultSkills;

    @Value("${app.scheduler.max-job-age-days:1}")
    private int maxJobAgeDays; // Daily scrape checks last 24h

    @Value("${app.scheduler.max-results:1000}")
    private int maxResults; // Default to 1000 ("maximum")

    /**
     * Run daily scrape.
     * Default: 8:00 AM every day
     * Configurable via app.scheduler.cron
     */
    @Scheduled(cron = "${app.scheduler.cron:0 0 8 * * *}")
    public void runDailyScrape() {
        log.info("Starting scheduled daily scrape for skills: {}", defaultSkills);

        List<String> skills = Arrays.asList(defaultSkills.split(","));

        for (String skill : skills) {
            try {
                log.info("Triggering scrape for skill: {}", skill.trim());
                ScrapeParameters params = ScrapeParameters.withFilters(
                        maxJobAgeDays,
                        maxResults, // Configurable max results
                        skill.trim(),
                        null);

                linkedInScraper.scrapeJobs(params);

                // Pause between skills to avoid rate limiting
                Thread.sleep(10000);
            } catch (Exception e) {
                log.error("Error during scheduled scrape for skill: {}", skill, e);
            }
        }

        log.info("Completed scheduled daily scrape");
    }
}
