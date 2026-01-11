package com.jobcompass.common.events;

import com.jobcompass.common.model.Source;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Raw job data scraped from job sites.
 * Published by Scraper Service, consumed by Processor Service.
 * Contains minimal unprocessed data from the job posting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawJobEvent {

    private Source source; // Job source (LinkedIn, Glassdoor, Indeed)
    private String title; // "Senior Java Developer"
    private String company; // "Google"
    private String location; // "San Francisco, CA"
    private String url; // Job posting URL
    private String postedDate; // "2 days ago" (raw text from site)
    private LocalDateTime scrapedAt; // When we scraped this job
}
