package com.jobcompass.common.events;

import com.jobcompass.common.model.Source;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Processed and normalized job data.
 * Published by Processor Service, consumed by Storage Service.
 * Contains cleaned, validated, and enriched job information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedJobEvent {

    private String title; // Normalized job title
    private String company; // Normalized company name
    private String location; // Normalized location
    private String salary; // Standardized salary (e.g., "$100k-120k")
    private String url; // Job posting URL
    private LocalDateTime postedDate; // Actual posting date (converted from raw text)
    private Source source; // Job source
    private Integer jobAgeInDays; // Calculated job age
}
