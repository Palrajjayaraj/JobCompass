package com.jobcompass.storage.dto;

import com.jobcompass.storage.entity.enums.ApplicationStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a job application.
 * 
 * @author Palrajjayaraj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicationRequest {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    @NotNull(message = "User email is required")
    @Email(message = "Invalid email format")
    private String userEmail;

    private String notes;
}
