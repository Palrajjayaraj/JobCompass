package com.jobcompass.storage.controller;

import com.jobcompass.common.model.Source;
import com.jobcompass.storage.dto.JobDto;
import com.jobcompass.storage.entity.Job;
import com.jobcompass.storage.entity.Skill;
import com.jobcompass.storage.service.JobApplicationService;
import com.jobcompass.storage.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for Job-related operations.
 * Provides endpoints for job retrieval and search.
 * 
 * @author Palrajjayaraj
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;
    private final JobApplicationService jobApplicationService;

    /**
     * Get all active jobs.
     * 
     * @return list of active jobs
     */
    @GetMapping
    public ResponseEntity<List<JobDto>> getAllJobs() {
        log.info("Fetching all active jobs");
        List<Job> jobs = jobService.findAllActiveJobs();
        return ResponseEntity.ok(jobs.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList()));
    }

    /**
     * Get a job by ID.
     * 
     * @param id the job ID
     * @return the job details
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobById(@PathVariable Long id) {
        log.info("Fetching job with ID: {}", id);
        return jobService.findById(id)
            .map(job -> ResponseEntity.ok(convertToDto(job)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get recent jobs posted within the last N days.
     * 
     * @param days number of days (default: 7)
     * @return list of recent jobs
     */
    @GetMapping("/recent")
    public ResponseEntity<List<JobDto>> getRecentJobs(
        @RequestParam(defaultValue = "7") int days
    ) {
        log.info("Fetching jobs from last {} days", days);
        List<Job> jobs = jobService.findRecentJobs(days);
        return ResponseEntity.ok(jobs.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList()));
    }

    /**
     * Search jobs by skills.
     * 
     * @param skills comma-separated skill names
     * @return list of jobs requiring the specified skills
     */
    @GetMapping("/search/by-skills")
    public ResponseEntity<List<JobDto>> searchBySkills(
        @RequestParam Set<String> skills
    ) {
        log.info("Searching jobs by skills: {}", skills);
        List<Job> jobs = jobService.findBySkills(skills);
        return ResponseEntity.ok(jobs.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList()));
    }

    /**
     * Search jobs by location.
     * 
     * @param location the location keyword
     * @return list of jobs matching the location
     */
    @GetMapping("/search/by-location")
    public ResponseEntity<List<JobDto>> searchByLocation(
        @RequestParam String location
    ) {
        log.info("Searching jobs by location: {}", location);
        List<Job> jobs = jobService.findByLocation(location);
        return ResponseEntity.ok(jobs.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList()));
    }

    /**
     * Search jobs by company name.
     * 
     * @param companyName the company name keyword
     * @return list of jobs from matching companies
     */
    @GetMapping("/search/by-company")
    public ResponseEntity<List<JobDto>> searchByCompany(
        @RequestParam String companyName
    ) {
        log.info("Searching jobs by company: {}", companyName);
        List<Job> jobs = jobService.findByCompanyName(companyName);
        return ResponseEntity.ok(jobs.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList()));
    }

    /**
     * Get jobs by source.
     * 
     * @param source the job source
     * @return list of jobs from the source
     */
    @GetMapping("/source/{source}")
    public ResponseEntity<List<JobDto>> getJobsBySource(@PathVariable Source source) {
        log.info("Fetching jobs from source: {}", source);
        List<Job> jobs = jobService.findBySource(source);
        return ResponseEntity.ok(jobs.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList()));
    }

    /**
     * Convert Job entity to JobDto.
     * 
     * @param job the job entity
     * @return the job DTO
     */
    private JobDto convertToDto(Job job) {
        return JobDto.builder()
            .id(job.getId())
            .title(job.getTitle())
            .description(job.getDescription())
            .location(job.getLocation())
            .salaryRange(job.getSalaryRange())
            .url(job.getUrl())
            .postedDate(job.getPostedDate())
            .jobAgeDays(job.getJobAgeDays())
            .source(job.getSource())
            .scrapedAt(job.getScrapedAt())
            .companyName(job.getCompany() != null ? job.getCompany().getName() : null)
            .skills(job.getSkills().stream().map(Skill::getName).collect(Collectors.toSet()))
            .applicationCount(jobApplicationService.countApplicationsForJob(job.getId()))
            .isActive(job.getIsActive())
            .build();
    }
}
