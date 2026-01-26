package com.jobcompass.scraper.service;

import com.jobcompass.common.events.RawJobEvent;
import com.jobcompass.common.model.ScrapeParameters;
import com.jobcompass.common.scraper.JobScraper;
import com.jobcompass.scraper.kafka.RawJobProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates job scraping across all configured scrapers.
 * Manages scraping execution and publishes results to Kafka.
 * 
 * @author Palraj Jayaraj
 */
@Service
public class ScraperOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ScraperOrchestrator.class);
    
    private final List<JobScraper> scrapers;
    private final RawJobProducer rawJobProducer;
    
    @Value("${jobcompass.scraper.max-job-age-days}")
    private int maxJobAgeDays;
    
    @Value("${jobcompass.scraper.max-jobs-per-source}")
    private int maxJobsPerSource;
    
    @Value("${jobcompass.scraper.default-skill:}")
    private String defaultSkill;
    
    @Value("${jobcompass.scraper.default-location:}")
    private String defaultLocation;

    @Value("${jobcompass.scraper.rate-limit-between-sources-ms:2000}")
    private long rateLimitBetweenSourcesMs;

    public ScraperOrchestrator(List<JobScraper> scrapers, RawJobProducer rawJobProducer) {
        this.scrapers = scrapers;
        this.rawJobProducer = rawJobProducer;
        log.info("Initialized ScraperOrchestrator with {} scrapers", scrapers.size());
    }

    /**
     * Execute scraping across all enabled scrapers
     */
    public void scrapeAll() {
        ScrapeParameters parameters = ScrapeParameters.withFilters(
            maxJobAgeDays,
            maxJobsPerSource,
            defaultSkill.isEmpty() ? null : defaultSkill,
            defaultLocation.isEmpty() ? null : defaultLocation
        );
        
        scrapeAll(parameters);
    }

    /**
     * Execute scraping with custom parameters
     */
    public void scrapeAll(ScrapeParameters parameters) {
        log.info("Starting scraping with parameters: maxAge={} days, maxResults={}, skill={}, location={}",
                parameters.maxJobAgeDays(), parameters.maxResults(), 
                parameters.skill(), parameters.location());
        
        int totalJobs = 0;
        
        for (JobScraper scraper : scrapers) {
            if (!scraper.isEnabled()) {
                log.info("Skipping disabled scraper: {}", scraper.getSource().name());
                continue;
            }
            
            try {
                log.info("Scraping from: {}", scraper.getSource().name());
                List<RawJobEvent> jobs = scraper.scrapeJobs(parameters);
                
                // Publish each job to Kafka
                for (RawJobEvent job : jobs) {
                    rawJobProducer.publishRawJob(job);
                }
                
                totalJobs += jobs.size();
                log.info("Scraped {} jobs from {}", jobs.size(), scraper.getSource().name());
                
                // Rate limiting between sources - now configurable
                Thread.sleep(rateLimitBetweenSourcesMs);
                
            } catch (Exception e) {
                log.error("Error scraping from {}: {}", scraper.getSource().name(), e.getMessage(), e);
            }
        }
        
        log.info("Scraping completed. Total jobs scraped: {}", totalJobs);
    }
}
