package com.jobcompass.common.model;

/**
 * Parameters for job scraping operations.
 * Using record for immutability and easy extensibility.
 */
public record ScrapeParameters(
        Integer maxJobAgeDays, // Only scrape jobs posted within this many days
        Integer maxResults, // Maximum number of jobs to scrape per source
        String skill, // Optional: Filter by skill (e.g., "Java", "Python")
        String location // Optional: Filter by location (e.g., "San Francisco", "Remote")
) {
    /**
     * Create default scraping parameters (7 days, 20 results, no filters)
     */
    public static ScrapeParameters defaults() {
        return new ScrapeParameters(7, 20, null, null);
    }

    /**
     * Create parameters with just age and results limit
     */
    public static ScrapeParameters of(int maxJobAgeDays, int maxResults) {
        return new ScrapeParameters(maxJobAgeDays, maxResults, null, null);
    }

    /**
     * Create parameters with all filters
     */
    public static ScrapeParameters withFilters(int maxJobAgeDays, int maxResults, String skill, String location) {
        return new ScrapeParameters(maxJobAgeDays, maxResults, skill, location);
    }
}
