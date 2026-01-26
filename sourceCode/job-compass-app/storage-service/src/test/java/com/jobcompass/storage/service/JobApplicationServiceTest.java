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
 * Unit tests for JobApplicationService.
 * 
 * @author Palrajjayaraj
 */
@RunWith(MockitoJUnitRunner.class)
public class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobApplicationService applicationService;

    private Job testJob;
    private JobApplication testApplication;

    @Before
    public void setUp() {
        testJob = Job.builder()
                .id(1L)
                .title("Senior Java Developer")
                .build();

        testApplication = JobApplication.builder()
                .id(1L)
                .job(testJob)
                .userEmail("test@example.com")
                .status(ApplicationStatus.APPLIED)
                .build();
    }

    @Test
    public void testApplyToJob_Success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(testApplication);

        JobApplication result = applicationService.applyToJob(1L, "test@example.com", "Interested in this role");

        assertNotNull(result);
        assertEquals("test@example.com", result.getUserEmail());
        verify(applicationRepository, times(1)).save(any(JobApplication.class));
    }

    @Test(expected = RuntimeException.class)
    public void testApplyToJob_JobNotFound() {
        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        applicationService.applyToJob(1L, "test@example.com", "Notes");
    }

    @Test
    public void testGetUserApplications() {
        List<JobApplication> applications = Arrays.asList(testApplication);
        when(applicationRepository.findByUserEmail("test@example.com")).thenReturn(applications);

        List<JobApplication> result = applicationService.getUserApplications("test@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test@example.com", result.get(0).getUserEmail());
    }

    @Test
    public void testUpdateStatus() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(testApplication);

        JobApplication result = applicationService.updateStatus(1L, ApplicationStatus.INTERVIEWING);

        assertNotNull(result);
        verify(applicationRepository, times(1)).save(any(JobApplication.class));
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateStatus_ApplicationNotFound() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.empty());

        applicationService.updateStatus(1L, ApplicationStatus.INTERVIEWING);
    }

    @Test
    public void testGetUserApplicationsByStatus() {
        List<JobApplication> applications = Arrays.asList(testApplication);
        when(applicationRepository.findByUserEmailAndStatus("test@example.com", ApplicationStatus.APPLIED))
                .thenReturn(applications);

        List<JobApplication> result = applicationService.getUserApplicationsByStatus(
                "test@example.com", ApplicationStatus.APPLIED);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ApplicationStatus.APPLIED, result.get(0).getStatus());
    }
}
