package com.jobcompass.common.events;

import java.time.LocalDateTime;

/**
 * Event published by the Scheduler Service to trigger job scraping.
 * Consumed by the Scraper Service to start scraping from all sources.
 */
public record ScrapingTriggerEvent(
        String action,
        LocalDateTime timestamp) {
    /**
     * Factory method to create a trigger event with current timestamp
     */
    public static ScrapingTriggerEvent createNow() {
        return new ScrapingTriggerEvent("START_SCRAPING", LocalDateTime.now());
    }
}
