package com.jobcompass.storage.service;

import com.jobcompass.storage.entity.Job;
import com.jobcompass.storage.entity.JobApplication;
import com.jobcompass.storage.entity.enums.ApplicationStatus;
import com.jobcompass.storage.repository.JobApplicationRepository;
import com.jobcompass.storage.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for JobApplication entity operations.
 * Handles business logic for job application management.
 * 
 * @author Palrajjayaraj
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;

    /**
     * Apply to a job.
     * Prevents duplicate applications for the same user and job.
     * 
     * @param jobId the job ID
     * @param userEmail the user's email
     * @param notes optional notes about the application
     * @return the created job application
     * @throws IllegalArgumentException if job not found or user already applied
     */
    @Transactional
    public JobApplication applyToJob(Long jobId, String userEmail, String notes) {
        // Check if the job exists
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));

        // Check if user has already applied
        if (jobApplicationRepository.existsByUserEmailAndJobId(userEmail, jobId)) {
            throw new IllegalArgumentException("User has already applied to this job");
        }

        log.info("User {} applying to job: {} - {}", userEmail, job.getId(), job.getTitle());

        JobApplication application = JobApplication.builder()
            .job(job)
            .userEmail(userEmail)
            .status(ApplicationStatus.APPLIED)
            .appliedAt(LocalDateTime.now())
            .notes(notes)
            .build();

        return jobApplicationRepository.save(application);
    }

    /**
     * Update the status of a job application.
     * 
     * @param applicationId the application ID
     * @param status the new status
     * @return the updated application
     * @throws IllegalArgumentException if application not found
     */
    @Transactional
    public JobApplication updateStatus(Long applicationId, ApplicationStatus status) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        log.info("Updating application {} status from {} to {}", 
            applicationId, application.getStatus(), status);

        application.setStatus(status);
        return jobApplicationRepository.save(application);
    }

    /**
     * Get all applications for a specific user.
     * 
     * @param userEmail the user's email
     * @return list of applications
     */
    public List<JobApplication> getUserApplications(String userEmail) {
        return jobApplicationRepository.findByUserEmail(userEmail);
    }

    /**
     * Get applications for a specific user filtered by status.
     * 
     * @param userEmail the user's email
     * @param status the application status
     * @return list of applications
     */
    public List<JobApplication> getUserApplicationsByStatus(String userEmail, ApplicationStatus status) {
        return jobApplicationRepository.findByUserEmailAndStatus(userEmail, status);
    }

    /**
     * Get a specific application by ID.
     * 
     * @param applicationId the application ID
     * @return Optional containing the application if found
     */
    public Optional<JobApplication> getApplicationById(Long applicationId) {
        return jobApplicationRepository.findById(applicationId);
    }

    /**
     * Get all applications for a specific job.
     * 
     * @param jobId the job ID
     * @return list of applications
     */
    public List<JobApplication> getApplicationsForJob(Long jobId) {
        return jobApplicationRepository.findByJobId(jobId);
    }

    /**
     * Count total applications for a job.
     * 
     * @param jobId the job ID
     * @return number of applications
     */
    public long countApplicationsForJob(Long jobId) {
        return jobApplicationRepository.countByJobId(jobId);
    }

    /**
     * Update application notes.
     * 
     * @param applicationId the application ID
     * @param notes the new notes
     * @return the updated application
     * @throws IllegalArgumentException if application not found
     */
    @Transactional
    public JobApplication updateNotes(Long applicationId, String notes) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        log.info("Updating notes for application {}", applicationId);
        application.setNotes(notes);
        return jobApplicationRepository.save(application);
    }
}
