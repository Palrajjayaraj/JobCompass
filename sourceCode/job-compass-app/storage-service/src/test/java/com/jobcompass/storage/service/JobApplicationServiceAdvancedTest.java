package com.jobcompass.storage.service;

import com.jobcompass.storage.entity.Job;
import com.jobcompass.storage.entity.JobApplication;
import com.jobcompass.storage.entity.enums.ApplicationStatus;
import com.jobcompass.storage.repository.JobApplicationRepository;
import com.jobcompass.storage.repository.JobRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Additional unit tests for JobApplicationService.
 * Tests scenarios for duplicate application prevention and application
 * counting.
 * 
 * @author Palrajjayaraj
 */
@RunWith(MockitoJUnitRunner.class)
public class JobApplicationServiceAdvancedTest {

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobApplicationService applicationService;

    private Job testJob;
    private JobApplication existingApplication;

    @Before
    public void setUp() {
        testJob = Job.builder()
                .id(1L)
                .title("Senior Java Developer")
                .build();

        existingApplication = JobApplication.builder()
                .id(1L)
                .job(testJob)
                .userEmail("test@example.com")
                .status(ApplicationStatus.APPLIED)
                .build();
    }

    @Test
    public void testCountApplicationsForJob_MultipleApplications() {
        when(applicationRepository.countByJobId(1L)).thenReturn(5L);

        Long count = applicationService.countApplicationsForJob(1L);

        assertEquals(Long.valueOf(5), count);
        verify(applicationRepository, times(1)).countByJobId(1L);
    }

    @Test
    public void testCountApplicationsForJob_NoApplications() {
        when(applicationRepository.countByJobId(1L)).thenReturn(0L);

        Long count = applicationService.countApplicationsForJob(1L);

        assertEquals(Long.valueOf(0), count);
    }

    @Test
    public void testGetApplicationsForJob() {
        List<JobApplication> applications = Arrays.asList(existingApplication);
        when(applicationRepository.findByJobId(1L)).thenReturn(applications);

        List<JobApplication> result = applicationService.getApplicationsForJob(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test@example.com", result.get(0).getUserEmail());
        verify(applicationRepository, times(1)).findByJobId(1L);
    }

    @Test
    public void testApplyToJob_WithNotes() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(existingApplication);

        JobApplication result = applicationService.applyToJob(
                1L,
                "test@example.com",
                "Applied via JobCompass - Open Top 10 feature");

        assertNotNull(result);
        verify(applicationRepository, times(1)).save(any(JobApplication.class));
    }

    @Test
    public void testApplyToJob_NullNotes() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(existingApplication);

        JobApplication result = applicationService.applyToJob(1L, "test@example.com", null);

        assertNotNull(result);
        verify(applicationRepository, times(1)).save(any(JobApplication.class));
    }

    @Test
    public void testUpdateNotes() {
        String newNotes = "Interview scheduled for next week";
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(existingApplication));
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(existingApplication);

        JobApplication result = applicationService.updateNotes(1L, newNotes);

        assertNotNull(result);
        verify(applicationRepository, times(1)).save(any(JobApplication.class));
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateNotes_ApplicationNotFound() {
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        applicationService.updateNotes(999L, "Some notes");
    }

    @Test
    public void testGetApplicationById() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(existingApplication));

        Optional<JobApplication> result = applicationService.getApplicationById(1L);

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getUserEmail());
    }

    @Test
    public void testGetApplicationById_NotFound() {
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<JobApplication> result = applicationService.getApplicationById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    public void testApplyToMultipleJobs_BatchScenario() {
        // Simulating "Open Top 10" scenario
        Job job1 = Job.builder().id(1L).title("Job 1").build();
        Job job2 = Job.builder().id(2L).title("Job 2").build();
        Job job3 = Job.builder().id(3L).title("Job 3").build();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(jobRepository.findById(2L)).thenReturn(Optional.of(job2));
        when(jobRepository.findById(3L)).thenReturn(Optional.of(job3));
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(existingApplication);

        JobApplication result1 = applicationService.applyToJob(1L, "test@example.com", "Batch apply");
        JobApplication result2 = applicationService.applyToJob(2L, "test@example.com", "Batch apply");
        JobApplication result3 = applicationService.applyToJob(3L, "test@example.com", "Batch apply");

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        verify(applicationRepository, times(3)).save(any(JobApplication.class));
    }

    @Test
    public void testGetUserApplications_EmptyList() {
        when(applicationRepository.findByUserEmail("nonexistent@example.com")).thenReturn(Arrays.asList());

        List<JobApplication> result = applicationService.getUserApplications("nonexistent@example.com");

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
