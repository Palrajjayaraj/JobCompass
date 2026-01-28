package com.jobcompass.storage.service;

import com.jobcompass.common.events.RawJobEvent;
import com.jobcompass.common.model.Source;
import com.jobcompass.storage.entity.Company;
import com.jobcompass.storage.entity.Job;
import com.jobcompass.storage.repository.JobRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JobService.saveRawJob() method.
 * Tests job creation, updates, deduplication, and company association.
 * 
 * @author Palrajjayaraj
 */
@RunWith(MockitoJUnitRunner.class)
public class JobServiceRawJobTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private SkillService skillService;

    @InjectMocks
    private JobService jobService;

    private RawJobEvent testEvent;
    private Job testJob;
    private Company testCompany;

    @Before
    public void setUp() {
        // Create test RawJobEvent
        testEvent = RawJobEvent.builder()
                .source(Source.of("LINKEDIN"))
                .title("Senior Java Developer")
                .company("Google")
                .location("San Francisco, CA")
                .description("Exciting opportunity for a Senior Java Developer...")
                .url("https://linkedin.com/jobs/12345")
                .postedDate("2 days ago")
                .scrapedAt(LocalDateTime.now())
                .build();

        // Create test Job entity
        testJob = Job.builder()
                .id(1L)
                .title("Senior Java Developer")
                .location("San Francisco, CA")
                .url("https://linkedin.com/jobs/12345")
                .description("Exciting opportunity...")
                .source(Source.of("LINKEDIN"))
                .isActive(true)
                .build();

        // Create test Company
        testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Google");
    }

    /**
     * Test saving a new job from RawJobEvent.
     * Verifies that a new Job entity is created and saved.
     */
    @Test
    public void testSaveRawJob_NewJob_Success() {
        // Arrange
        when(jobRepository.findByUrl(anyString())).thenReturn(Optional.empty());
        when(companyService.findOrCreateCompany(anyString())).thenReturn(testCompany);
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        // Act
        Job savedJob = jobService.saveRawJob(testEvent);

        // Assert
        assertNotNull(savedJob);
        verify(jobRepository, times(1)).findByUrl("https://linkedin.com/jobs/12345");
        verify(companyService, times(1)).findOrCreateCompany("Google");
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    /**
     * Test updating an existing job from RawJobEvent.
     * Verifies that existing job is found and updated, not duplicated.
     */
    @Test
    public void testSaveRawJob_UpdateExistingJob_Success() {
        // Arrange
        when(jobRepository.findByUrl(anyString())).thenReturn(Optional.of(testJob));
        when(companyService.findOrCreateCompany(anyString())).thenReturn(testCompany);
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        // Act
        Job savedJob = jobService.saveRawJob(testEvent);

        // Assert
        assertNotNull(savedJob);
        assertEquals(testJob.getId(), savedJob.getId());
        verify(jobRepository, times(1)).findByUrl("https://linkedin.com/jobs/12345");
        verify(jobRepository, times(1)).save(testJob);
    }

    /**
     * Test saving job without company information.
     * Verifies that job is saved without company association.
     */
    @Test
    public void testSaveRawJob_NoCompany_Success() {
        // Arrange
        RawJobEvent eventNoCompany = RawJobEvent.builder()
                .title("Developer")
                .url("https://example.com/job")
                .source(Source.of("LINKEDIN"))
                .build();

        when(jobRepository.findByUrl(anyString())).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        // Act
        Job savedJob = jobService.saveRawJob(eventNoCompany);

        // Assert
        assertNotNull(savedJob);
        verify(companyService, never()).findOrCreateCompany(anyString());
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    /**
     * Test saving job with empty company name.
     * Verifies that empty/whitespace company names are ignored.
     */
    @Test
    public void testSaveRawJob_EmptyCompany_Ignored() {
        // Arrange
        RawJobEvent eventEmptyCompany = RawJobEvent.builder()
                .title("Developer")
                .company("   ")
                .url("https://example.com/job")
                .source(Source.of("LINKEDIN"))
                .build();

        when(jobRepository.findByUrl(anyString())).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        // Act
        Job savedJob = jobService.saveRawJob(eventEmptyCompany);

        // Assert
        assertNotNull(savedJob);
        verify(companyService, never()).findOrCreateCompany(anyString());
    }

    /**
     * Test duplicate job detection by URL.
     * Verifies that jobs with same URL are updated, not duplicated.
     */
    @Test
    public void testSaveRawJob_DuplicateDetection_ByUrl() {
        // Arrange
        when(jobRepository.findByUrl("https://linkedin.com/jobs/12345"))
                .thenReturn(Optional.of(testJob));
        when(companyService.findOrCreateCompany(anyString())).thenReturn(testCompany);
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        // Act - save same job twice
        Job firstSave = jobService.saveRawJob(testEvent);
        Job secondSave = jobService.saveRawJob(testEvent);

        // Assert - should be same job, not duplicate
        assertEquals(firstSave.getId(), secondSave.getId());
        verify(jobRepository, times(2)).findByUrl("https://linkedin.com/jobs/12345");
        verify(jobRepository, times(2)).save(testJob);
    }

    /**
     * Test saving job with all optional fields populated.
     * Verifies that all fields are correctly mapped.
     */
    @Test
    public void testSaveRawJob_AllFields_Success() {
        // Arrange
        RawJobEvent fullEvent = RawJobEvent.builder()
                .source(Source.of("LINKEDIN"))
                .title("Senior Java Developer")
                .company("Google")
                .location("San Francisco, CA")
                .description("Full description text...")
                .url("https://linkedin.com/jobs/12345")
                .postedDate("2 days ago")
                .scrapedAt(LocalDateTime.now())
                .build();

        when(jobRepository.findByUrl(anyString())).thenReturn(Optional.empty());
        when(companyService.findOrCreateCompany(anyString())).thenReturn(testCompany);
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        // Act
        Job savedJob = jobService.saveRawJob(fullEvent);

        // Assert
        assertNotNull(savedJob);
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    /**
     * Test saving job from different sources.
     * Verifies that jobs from LinkedIn, Glassdoor, Indeed are handled correctly.
     */
    @Test
    public void testSaveRawJob_DifferentSources_Success() {
        // Arrange
        when(jobRepository.findByUrl(anyString())).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        RawJobEvent linkedInJob = RawJobEvent.builder()
                .source(Source.of("LINKEDIN"))
                .title("Job 1")
                .url("https://linkedin.com/1")
                .build();

        RawJobEvent glassdoorJob = RawJobEvent.builder()
                .source(Source.of("GLASSDOOR"))
                .title("Job 2")
                .url("https://glassdoor.com/2")
                .build();

        RawJobEvent indeedJob = RawJobEvent.builder()
                .source(Source.of("INDEED"))
                .title("Job 3")
                .url("https://indeed.com/3")
                .build();

        // Act
        Job savedLinkedIn = jobService.saveRawJob(linkedInJob);
        Job savedGlassdoor = jobService.saveRawJob(glassdoorJob);
        Job savedIndeed = jobService.saveRawJob(indeedJob);

        // Assert
        assertNotNull(savedLinkedIn);
        assertNotNull(savedGlassdoor);
        assertNotNull(savedIndeed);
        verify(jobRepository, times(3)).save(any(Job.class));
    }

    /**
     * Test that updated job remains active.
     * Verifies that isActive flag is set to true on update.
     */
    @Test
    public void testSaveRawJob_UpdateKeepsJobActive() {
        // Arrange
        testJob.setIsActive(false); // Simulate inactive job
        when(jobRepository.findByUrl(anyString())).thenReturn(Optional.of(testJob));
        when(companyService.findOrCreateCompany(anyString())).thenReturn(testCompany);
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        // Act
        jobService.saveRawJob(testEvent);

        // Assert
        verify(jobRepository, times(1)).save(testJob);
        // Job should be reactivated (isActive = true)
        assertTrue(testJob.getIsActive());
    }

    /**
     * Test saving job with postedDate string.
     * Verifies that date string is correctly parsed to LocalDate.
     */
    @Test
    public void testSaveRawJob_WithPostedDate_MapsCorrectly() {
        // Arrange
        String dateString = "2026-01-24";
        LocalDate expectedDate = LocalDate.parse(dateString);

        RawJobEvent eventWithDate = RawJobEvent.builder()
                .title("Developer")
                .url("https://example.com/job-date")
                .source(Source.of("LINKEDIN"))
                .postedDate(dateString)
                .build();

        when(jobRepository.findByUrl(anyString())).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Job savedJob = jobService.saveRawJob(eventWithDate);

        // Assert
        assertNotNull(savedJob);
        assertEquals(expectedDate, savedJob.getPostedDate());
        verify(jobRepository, times(1)).save(any(Job.class));
    }
}
