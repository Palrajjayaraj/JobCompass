package com.jobcompass.storage.dto;

import com.jobcompass.common.model.Source;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Data Transfer Object for Job entity.
 * Used for API responses to avoid exposing internal entity structure.
 * 
 * @author Palrajjayaraj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {

    private Long id;
    private String title;
    private String description;
    private String location;
    private String salaryRange;
    private String url;
    private LocalDate postedDate;
    private Integer jobAgeDays;
    private Source source;
    private LocalDateTime scrapedAt;
    private String companyName;
    private Set<String> skills;
    private Long applicationCount;
    private Boolean isActive;
}
