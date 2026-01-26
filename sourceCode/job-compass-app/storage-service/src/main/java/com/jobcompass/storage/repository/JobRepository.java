package com.jobcompass.storage.repository;

import com.jobcompass.common.model.Source;
import com.jobcompass.storage.entity.Company;
import com.jobcompass.storage.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for Job entity.
 * Provides CRUD operations and custom queries for jobs.
 * 
 * @author Palrajjayaraj
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    /**
     * Find a job by its URL.
     * 
     * @param url the job posting URL
     * @return Optional containing the job if found
     */
    Optional<Job> findByUrl(String url);

    /**
     * Find a job by source and external ID.
     * 
     * @param source the job source
     * @param externalId the external job ID
     * @return Optional containing the job if found
     */
    Optional<Job> findBySourceAndExternalId(Source source, String externalId);

    /**
     * Find all jobs for a specific company.
     * 
     * @param company the company
     * @return list of jobs
     */
    List<Job> findByCompany(Company company);

    /**
     * Find active jobs by source.
     * 
     * @param source the job source
     * @param isActive whether the job is active
     * @return list of active jobs from the source
     */
    List<Job> findBySourceAndIsActive(Source source, boolean isActive);

    /**
     * Find all active jobs.
     * 
     * @param isActive whether the job is active
     * @return list of active jobs
     */
    List<Job> findByIsActive(boolean isActive);

    /**
     * Find recent jobs posted on or after a specific date.
     * 
     * @param startDate the start date
     * @return list of recent active jobs
     */
    @Query("SELECT j FROM Job j WHERE j.postedDate >= :startDate AND j.isActive = true ORDER BY j.postedDate DESC")
    List<Job> findRecentJobs(@Param("startDate") LocalDate startDate);

    /**
     * Find jobs by skills.
     * 
     * @param skillNames set of skill names
     * @return list of active jobs requiring the specified skills
     */
    @Query("SELECT DISTINCT j FROM Job j JOIN j.skills s WHERE s.name IN :skillNames AND j.isActive = true")
    List<Job> findBySkills(@Param("skillNames") Set<String> skillNames);

    /**
     * Find jobs by location (case-insensitive partial match).
     * 
     * @param location the location keyword
     * @return list of active jobs matching the location
     */
    @Query("SELECT j FROM Job j WHERE LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')) AND j.isActive = true")
    List<Job> findByLocationContainingIgnoreCase(@Param("location") String location);

    /**
     * Find jobs by company name (case-insensitive partial match).
     * 
     * @param companyName the company name keyword
     * @return list of active jobs from matching companies
     */
    @Query("SELECT j FROM Job j WHERE LOWER(j.company.name) LIKE LOWER(CONCAT('%', :companyName, '%')) AND j.isActive = true")
    List<Job> findByCompanyNameContainingIgnoreCase(@Param("companyName") String companyName);

    /**
     * Find jobs posted within a date range.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return list of active jobs within the date range
     */
    @Query("SELECT j FROM Job j WHERE j.postedDate BETWEEN :startDate AND :endDate AND j.isActive = true ORDER BY j.postedDate DESC")
    List<Job> findByPostedDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
