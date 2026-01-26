package com.jobcompass.storage.service;

import com.jobcompass.common.events.ProcessedJobEvent;
import com.jobcompass.common.model.Source;
import com.jobcompass.storage.entity.Company;
import com.jobcompass.storage.entity.Job;
import com.jobcompass.storage.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service class for Job entity operations.
 * Handles business logic for job management including upsert operations.
 * 
 * @author Palrajjayaraj
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyService companyService;
    private final SkillService skillService;

    /**
     * Save or update a job from ProcessedJobEvent.
     * Implements upsert logic to prevent duplicates based on URL.
     * 
     * @param event the processed job event
     * @return the saved or updated job
     */
    @Transactional
    public Job saveOrUpdateJob(ProcessedJobEvent event) {
        log.debug("Processing job: {} at {}", event.getTitle(), event.getCompany());

        // Try to find existing job by URL
        Optional<Job> existingJob = jobRepository.findByUrl(event.getUrl());

        Job job;
        if (existingJob.isPresent()) {
            log.info("Updating existing job: {}", event.getUrl());
            job = existingJob.get();
            updateJobFromEvent(job, event);
        } else {
            log.info("Creating new job: {}", event.getUrl());
            job = createJobFromEvent(event);
        }

        // Handle company relationship
        if (event.getCompany() != null && !event.getCompany().trim().isEmpty()) {
            Company company = companyService.findOrCreateCompany(event.getCompany());
            job.setCompany(company);
        }

        return jobRepository.save(job);
    }

    /**
     * Find a job by ID.
     * 
     * @param id the job ID
     * @return Optional containing the job if found
     */
    public Optional<Job> findById(Long id) {
        return jobRepository.findById(id);
    }

    /**
     * Find a job by URL.
     * 
     * @param url the job URL
     * @return Optional containing the job if found
     */
    public Optional<Job> findByUrl(String url) {
        return jobRepository.findByUrl(url);
    }

    /**
     * Find all active jobs.
     * 
     * @return list of active jobs
     */
    public List<Job> findAllActiveJobs() {
        return jobRepository.findByIsActive(true);
    }

    /**
     * Find recent jobs posted within the last N days.
     * 
     * @param days number of days
     * @return list of recent active jobs
     */
    public List<Job> findRecentJobs(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return jobRepository.findRecentJobs(startDate);
    }

    /**
     * Find jobs by skills.
     * 
     * @param skillNames set of skill names
     * @return list of jobs requiring the specified skills
     */
    public List<Job> findBySkills(Set<String> skillNames) {
        return jobRepository.findBySkills(skillNames);
    }

    /**
     * Find jobs by location.
     * 
     * @param location the location keyword
     * @return list of jobs matching the location
     */
    public List<Job> findByLocation(String location) {
        return jobRepository.findByLocationContainingIgnoreCase(location);
    }

    /**
     * Find jobs by company name.
     * 
     * @param companyName the company name keyword
     * @return list of jobs from matching companies
     */
    public List<Job> findByCompanyName(String companyName) {
        return jobRepository.findByCompanyNameContainingIgnoreCase(companyName);
    }

    /**
     * Find jobs by source.
     * 
     * @param source the job source
     * @return list of active jobs from the source
     */
    public List<Job> findBySource(Source source) {
        return jobRepository.findBySourceAndIsActive(source, true);
    }

    /**
     * Deactivate a job (soft delete).
     * 
     * @param jobId the job ID
     */
    @Transactional
    public void deactivateJob(Long jobId) {
        jobRepository.findById(jobId).ifPresent(job -> {
            log.info("Deactivating job: {} - {}", job.getId(), job.getTitle());
            job.setIsActive(false);
            jobRepository.save(job);
        });
    }

    /**
     * Create a new Job entity from ProcessedJobEvent.
     * 
     * @param event the processed job event
     * @return new job entity
     */
    private Job createJobFromEvent(ProcessedJobEvent event) {
        return Job.builder()
            .title(event.getTitle())
            .location(event.getLocation())
            .url(event.getUrl())
            .salaryRange(event.getSalary())
            .postedDate(event.getPostedDate() != null ? event.getPostedDate().toLocalDate() : null)
            .jobAgeDays(event.getJobAgeInDays())
            .source(event.getSource())
            .scrapedAt(LocalDateTime.now())
            .isActive(true)
            .build();
    }

    /**
     * Update an existing Job entity from ProcessedJobEvent.
     * 
     * @param job the existing job
     * @param event the processed job event
     */
    private void updateJobFromEvent(Job job, ProcessedJobEvent event) {
        job.setTitle(event.getTitle());
        job.setLocation(event.getLocation());
        job.setSalaryRange(event.getSalary());
        job.setPostedDate(event.getPostedDate() != null ? event.getPostedDate().toLocalDate() : null);
        job.setJobAgeDays(event.getJobAgeInDays());
        job.setScrapedAt(LocalDateTime.now());
        // Keep the job active when updating
        job.setIsActive(true);
    }
}
