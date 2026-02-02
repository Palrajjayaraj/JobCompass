package com.jobcompass.scraper.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Request DTO for manual scraping operations.
 * Supports multiple skills and configurable parameters.
 * 
 * @author Palraj Jayaraj
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrapeRequest {

    /**
     * List of skills to scrape for (e.g., ["Java", "Spring Boot", "GenAI"])
     */
    private List<String> skills;

    /**
     * Location filter (optional)
     */
    private String location;

    /**
     * Maximum job age in days
     */
    private int maxJobAgeDays = 7;

    /**
     * Maximum results per skill
     */
    private int maxResults = 5;

    /**
     * Map of authentication credentials per source.
     * Key: Source (e.g., "LINKEDIN"), Value: Map of credentials (e.g., {"li_at":
     * "..."})
     */
    private java.util.Map<String, java.util.Map<String, String>> authentication;
}
