package com.jobcompass.storage.repository;

import com.jobcompass.storage.entity.JobApplication;
import com.jobcompass.storage.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for JobApplication entity.
 * Provides CRUD operations and custom queries for job applications.
 * 
 * @author Palrajjayaraj
 */
@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    /**
     * Find all applications by a user.
     * 
     * @param userEmail the user's email
     * @return list of applications
     */
    List<JobApplication> findByUserEmail(String userEmail);

    /**
     * Find applications by user and status.
     * 
     * @param userEmail the user's email
     * @param status the application status
     * @return list of applications
     */
    List<JobApplication> findByUserEmailAndStatus(String userEmail, ApplicationStatus status);

    /**
     * Find a specific application by user and job.
     * 
     * @param userEmail the user's email
     * @param jobId the job ID
     * @return Optional containing the application if found
     */
    Optional<JobApplication> findByUserEmailAndJobId(String userEmail, Long jobId);

    /**
     * Count applications for a specific job.
     * 
     * @param jobId the job ID
     * @return number of applications
     */
    long countByJobId(Long jobId);

    /**
     * Check if a user has already applied to a job.
     * 
     * @param userEmail the user's email
     * @param jobId the job ID
     * @return true if the user has applied, false otherwise
     */
    boolean existsByUserEmailAndJobId(String userEmail, Long jobId);

    /**
     * Find all applications for a specific job.
     * 
     * @param jobId the job ID
     * @return list of applications
     */
    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.id = :jobId ORDER BY ja.appliedAt DESC")
    List<JobApplication> findByJobId(@Param("jobId") Long jobId);
}
