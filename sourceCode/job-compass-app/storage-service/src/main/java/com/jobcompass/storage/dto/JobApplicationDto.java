package com.jobcompass.storage.dto;

import com.jobcompass.storage.entity.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for JobApplication entity.
 * 
 * @author Palrajjayaraj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationDto {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String userEmail;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private String notes;
}
