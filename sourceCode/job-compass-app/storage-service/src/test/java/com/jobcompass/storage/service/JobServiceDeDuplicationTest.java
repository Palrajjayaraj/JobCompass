package com.jobcompass.storage.service;

import com.jobcompass.common.events.ProcessedJobEvent;
import com.jobcompass.common.model.Source;
import com.jobcompass.storage.entity.Company;
import com.jobcompass.storage.entity.Job;
import com.jobcompass.storage.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JobService de-duplication logic.
 */
@ExtendWith(MockitoExtension.class)
public class JobServiceDeDuplicationTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private SkillService skillService;

    @InjectMocks
    private JobService jobService;

    private ProcessedJobEvent jobEvent;

    @BeforeEach
    public void setUp() {
        jobEvent = ProcessedJobEvent.builder()
                .title("Software Engineer")
                .url("https://linkedin.com/jobs/view/123456")
                .company("Tech Corp")
                .source(Source.of("LinkedIn"))
                .jobAgeInDays(1)
                .build();
    }

    @Test
    public void testSaveOrUpdateJob_NewJob_CreatesNewRecord() {
        // Arrange
        when(jobRepository.findByUrl(jobEvent.getUrl())).thenReturn(Optional.empty());
        when(companyService.findOrCreateCompany("Tech Corp")).thenReturn(new Company());
        when(jobRepository.save(any(Job.class))).thenAnswer(i -> {
            Job job = i.getArgument(0);
            job.setId(1L); // Simulate DB save
            return job;
        });

        // Act
        Job result = jobService.saveOrUpdateJob(jobEvent);

        // Assert
        assertNotNull(result);
        verify(jobRepository, times(1)).save(any(Job.class));
        verify(jobRepository, times(1)).findByUrl(jobEvent.getUrl());
    }

    @Test
    public void testSaveOrUpdateJob_ExistingJob_UpdatesRecord() {
        // Arrange
        Job existingJob = Job.builder()
                .id(1L)
                .title("Old Title")
                .url("https://linkedin.com/jobs/view/123456")
                .build();

        when(jobRepository.findByUrl(jobEvent.getUrl())).thenReturn(Optional.of(existingJob));
        when(companyService.findOrCreateCompany("Tech Corp")).thenReturn(new Company());
        when(jobRepository.save(any(Job.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Job result = jobService.saveOrUpdateJob(jobEvent);

        // Assert
        assertEquals(existingJob.getId(), result.getId()); // ID should allow be same
        assertEquals("Software Engineer", result.getTitle()); // Title should be updated
        verify(jobRepository, times(1)).save(existingJob); // Should update, not create new
        verify(jobRepository, times(1)).findByUrl(jobEvent.getUrl());
    }
}
