package com.jobcompass.storage.controller;

import com.jobcompass.storage.dto.CreateApplicationRequest;
import com.jobcompass.storage.dto.JobApplicationDto;
import com.jobcompass.storage.entity.JobApplication;
import com.jobcompass.storage.entity.enums.ApplicationStatus;
import com.jobcompass.storage.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for JobApplication-related operations.
 * Provides endpoints for managing job applications.
 * 
 * @author Palrajjayaraj
 */
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    /**
     * Apply to a job.
     * 
     * @param request the application request
     * @return the created application
     */
    @PostMapping
    public ResponseEntity<?> applyToJob(@Valid @RequestBody CreateApplicationRequest request) {
        log.info("Received application request for job {} from user {}", 
            request.getJobId(), request.getUserEmail());
        
        try {
            JobApplication application = jobApplicationService.applyToJob(
                request.getJobId(),
                request.getUserEmail(),
                request.getNotes()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(application));
        } catch (IllegalArgumentException e) {
            log.error("Failed to create application: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get all applications for a specific user.
     * 
     * @param email the user's email
     * @return list of applications
     */
    @GetMapping("/user/{email}")
    public ResponseEntity<List<JobApplicationDto>> getUserApplications(@PathVariable String email) {
        log.info("Fetching applications for user: {}", email);
        List<JobApplication> applications = jobApplicationService.getUserApplications(email);
        return ResponseEntity.ok(applications.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList()));
    }

    /**
     * Get applications by user and status.
     * 
     * @param email the user's email
     * @param status the application status
     * @return list of applications
     */
    @GetMapping("/user/{email}/status/{status}")
    public ResponseEntity<List<JobApplicationDto>> getUserApplicationsByStatus(
        @PathVariable String email,
        @PathVariable ApplicationStatus status
    ) {
        log.info("Fetching applications for user {} with status {}", email, status);
        List<JobApplication> applications = jobApplicationService.getUserApplicationsByStatus(email, status);
        return ResponseEntity.ok(applications.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList()));
    }

    /**
     * Get an application by ID.
     * 
     * @param id the application ID
     * @return the application details
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationDto> getApplicationById(@PathVariable Long id) {
        log.info("Fetching application with ID: {}", id);
        return jobApplicationService.getApplicationById(id)
            .map(app -> ResponseEntity.ok(convertToDto(app)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update the status of an application.
     * 
     * @param id the application ID
     * @param status the new status
     * @return the updated application
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
        @PathVariable Long id,
        @RequestParam ApplicationStatus status
    ) {
        log.info("Updating application {} status to {}", id, status);
        try {
            JobApplication application = jobApplicationService.updateStatus(id, status);
            return ResponseEntity.ok(convertToDto(application));
        } catch (IllegalArgumentException e) {
            log.error("Failed to update application status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Update application notes.
     * 
     * @param id the application ID
     * @param notes the new notes
     * @return the updated application
     */
    @PutMapping("/{id}/notes")
    public ResponseEntity<?> updateNotes(
        @PathVariable Long id,
        @RequestBody String notes
    ) {
        log.info("Updating notes for application {}", id);
        try {
            JobApplication application = jobApplicationService.updateNotes(id, notes);
            return ResponseEntity.ok(convertToDto(application));
        } catch (IllegalArgumentException e) {
            log.error("Failed to update notes: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get all applications for a specific job.
     * 
     * @param jobId the job ID
     * @return list of applications
     */
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobApplicationDto>> getApplicationsForJob(@PathVariable Long jobId) {
        log.info("Fetching applications for job: {}", jobId);
        List<JobApplication> applications = jobApplicationService.getApplicationsForJob(jobId);
        return ResponseEntity.ok(applications.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList()));
    }

    /**
     * Convert JobApplication entity to JobApplicationDto.
     * 
     * @param application the job application entity
     * @return the job application DTO
     */
    private JobApplicationDto convertToDto(JobApplication application) {
        return JobApplicationDto.builder()
            .id(application.getId())
            .jobId(application.getJob().getId())
            .jobTitle(application.getJob().getTitle())
            .companyName(application.getJob().getCompany() != null ? 
                application.getJob().getCompany().getName() : null)
            .userEmail(application.getUserEmail())
            .status(application.getStatus())
            .appliedAt(application.getAppliedAt())
            .notes(application.getNotes())
            .build();
    }
}
