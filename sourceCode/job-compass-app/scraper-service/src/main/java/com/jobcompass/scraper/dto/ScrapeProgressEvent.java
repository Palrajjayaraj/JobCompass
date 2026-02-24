package com.jobcompass.scraper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a scrape progress update event.
 * Sent to the frontend via Server-Sent Events (SSE).
 *
 * @author Palraj Jayaraj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapeProgressEvent {

    /**
     * Possible states of a scrape operation.
     */
    public enum Status {
        STARTED,
        SCRAPING,
        SKILL_COMPLETED,
        PUBLISHING,
        COMPLETED,
        FAILED
    }

    private String scrapeId;
    private Status status;
    private String currentSkill;
    private String currentLocation;
    private int skillIndex;
    private int totalSkills;
    private int jobsFoundForSkill;
    private int totalJobsFound;
    private int duplicateJobsSkipped;
    private int errors;
    private String message;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
