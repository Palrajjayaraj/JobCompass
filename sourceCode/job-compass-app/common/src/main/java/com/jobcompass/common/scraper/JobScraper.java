package com.jobcompass.common.scraper;

import com.jobcompass.common.events.RawJobEvent;
import com.jobcompass.common.model.ScrapeParameters;
import com.jobcompass.common.model.Source;

import java.util.List;

/**
 * Interface that all job scrapers must implement.
 * Each scraper (LinkedIn, Glassdoor, Indeed) will implement this contract.
 */
public interface JobScraper {

    /**
     * @return The source information for this scraper
     */
    Source getSource();

    /**
     * Scrape jobs from this source with given parameters.
     * 
     * @param parameters Scraping parameters (age limit, results limit, filters,
     *                   etc.)
     * @return List of raw job events
     */
    List<RawJobEvent> scrapeJobs(ScrapeParameters parameters);

    /**
     * Check if this scraper is currently enabled.
     * Can be overridden to enable/disable scrapers dynamically.
     * 
     * @return true if enabled, false otherwise
     */
    default boolean isEnabled() {
        return true;
    }
}
