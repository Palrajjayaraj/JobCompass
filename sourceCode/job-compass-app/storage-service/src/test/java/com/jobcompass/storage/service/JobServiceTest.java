package com.jobcompass.storage.service;

import com.jobcompass.common.events.ProcessedJobEvent;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JobService.
 * Tests upsert logic and business operations.
 * 
 * @author Palrajjayaraj
 */
@RunWith(MockitoJUnitRunner.class)
public class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private SkillService skillService;

    @InjectMocks
    private JobService jobService;

    private ProcessedJobEvent testEvent;
    private Company testCompany;

    /**
     * Set up test data before each test.
     */
    @Before
    public void setUp() {
        testCompany = Company.builder()
            .id(1L)
            .name("Google")
            .build();

        testEvent = ProcessedJobEvent.builder()
            .title("Senior Java Developer")
            .company("Google")
            .location("San Francisco, CA")
            .salary("$120k-150k")
            .url("https://example.com/job123")
            .postedDate(LocalDateTime.now().minusDays(3))
            .source(Source.of("LINKEDIN"))
            .jobAgeInDays(3)
            .build();
    }

    /**
     * Test creating a new job from ProcessedJobEvent.
     */
    @Test
    public void testSaveOrUpdateJob_NewJob() {
        // Arrange
        when(jobRepository.findByUrl(testEvent.getUrl())).thenReturn(Optional.empty());
        when(companyService.findOrCreateCompany("Google")).thenReturn(testCompany);
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            job.setId(1L);
            return job;
        });

        // Act
        Job savedJob = jobService.saveOrUpdateJob(testEvent);

        // Assert
        assertNotNull(savedJob);
        assertEquals("Senior Java Developer", savedJob.getTitle());
        assertEquals("https://example.com/job123", savedJob.getUrl());
        assertEquals(testCompany, savedJob.getCompany());
        assertTrue(savedJob.getIsActive());

        verify(jobRepository, times(1)).findByUrl(testEvent.getUrl());
        verify(companyService, times(1)).findOrCreateCompany("Google");
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    /**
     * Test updating an existing job from ProcessedJobEvent.
     */
    @Test
    public void testSaveOrUpdateJob_ExistingJob() {
        // Arrange
        Job existingJob = Job.builder()
            .id(1L)
            .title("Old Title")
            .url("https://example.com/job123")
            .source(Source.of("LINKEDIN"))
            .isActive(false)
            .build();

        when(jobRepository.findByUrl(testEvent.getUrl())).thenReturn(Optional.of(existingJob));
        when(companyService.findOrCreateCompany("Google")).thenReturn(testCompany);
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Job updatedJob = jobService.saveOrUpdateJob(testEvent);

        // Assert
        assertNotNull(updatedJob);
        assertEquals(1L, updatedJob.getId().longValue());
        assertEquals("Senior Java Developer", updatedJob.getTitle());  // Updated title
        assertTrue(updatedJob.getIsActive());  // Reactivated

        verify(jobRepository, times(1)).findByUrl(testEvent.getUrl());
        verify(companyService, times(1)).findOrCreateCompany("Google");
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    /**
     * Test finding job by URL.
     */
    @Test
    public void testFindByUrl() {
        // Arrange
        Job testJob = Job.builder()
            .id(1L)
            .url("https://example.com/job123")
            .build();

        when(jobRepository.findByUrl("https://example.com/job123")).thenReturn(Optional.of(testJob));

        // Act
        Optional<Job> found = jobService.findByUrl("https://example.com/job123");

        // Assert
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId().longValue());

        verify(jobRepository, times(1)).findByUrl("https://example.com/job123");
    }

    /**
     * Test deactivating a job.
     */
    @Test
    public void testDeactivateJob() {
        // Arrange
        Job testJob = Job.builder()
            .id(1L)
            .title("Test Job")
            .isActive(true)
            .build();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        jobService.deactivateJob(1L);

        // Assert
        assertFalse(testJob.getIsActive());

        verify(jobRepository, times(1)).findById(1L);
        verify(jobRepository, times(1)).save(testJob);
    }
}
