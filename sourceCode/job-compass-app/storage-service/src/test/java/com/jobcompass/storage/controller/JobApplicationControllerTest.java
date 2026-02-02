package com.jobcompass.storage.controller;

import com.jobcompass.storage.dto.CreateApplicationRequest;
import com.jobcompass.storage.dto.JobApplicationDto;
import com.jobcompass.storage.entity.Company;
import com.jobcompass.storage.entity.Job;
import com.jobcompass.storage.entity.JobApplication;
import com.jobcompass.storage.entity.enums.ApplicationStatus;
import com.jobcompass.storage.service.JobApplicationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JobApplicationController.
 * Tests the REST API for job applications including batch operations.
 * 
 * @author Palrajjayaraj
 */
@RunWith(MockitoJUnitRunner.class)
public class JobApplicationControllerTest {

    @Mock
    private JobApplicationService jobApplicationService;

    @InjectMocks
    private JobApplicationController jobApplicationController;

    private JobApplication testApplication;
    private Job testJob;
    private Company testCompany;

    @Before
    public void setUp() {
        testCompany = Company.builder()
                .id(1L)
                .name("Tech Corp")
                .build();

        testJob = Job.builder()
                .id(1L)
                .title("Senior Java Developer")
                .company(testCompany)
                .build();

        testApplication = JobApplication.builder()
                .id(1L)
                .job(testJob)
                .userEmail("test@example.com")
                .status(ApplicationStatus.APPLIED)
                .appliedAt(LocalDateTime.now())
                .notes("Applied via JobCompass")
                .build();
    }

    @Test
    public void testApplyToJob_Success() {
        CreateApplicationRequest request = new CreateApplicationRequest();
        request.setJobId(1L);
        request.setUserEmail("test@example.com");
        request.setNotes("Interested in this role");

        when(jobApplicationService.applyToJob(1L, "test@example.com", "Interested in this role"))
                .thenReturn(testApplication);

        ResponseEntity<?> response = jobApplicationController.applyToJob(request);

        assertNotNull(response);
        assertEquals(201, response.getStatusCodeValue());
        verify(jobApplicationService, times(1)).applyToJob(1L, "test@example.com", "Interested in this role");
    }

    @Test
    public void testApplyToJob_InvalidJobId() {
        CreateApplicationRequest request = new CreateApplicationRequest();
        request.setJobId(999L);
        request.setUserEmail("test@example.com");
        request.setNotes("Notes");

        when(jobApplicationService.applyToJob(999L, "test@example.com", "Notes"))
                .thenThrow(new IllegalArgumentException("Job not found"));

        ResponseEntity<?> response = jobApplicationController.applyToJob(request);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Job not found"));
    }

    @Test
    public void testGetUserApplications() {
        List<JobApplication> applications = Arrays.asList(testApplication);
        when(jobApplicationService.getUserApplications("test@example.com"))
                .thenReturn(applications);

        ResponseEntity<List<JobApplicationDto>> response = jobApplicationController
                .getUserApplications("test@example.com");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("test@example.com", response.getBody().get(0).getUserEmail());
    }

    @Test
    public void testGetUserApplicationsByStatus() {
        List<JobApplication> applications = Arrays.asList(testApplication);
        when(jobApplicationService.getUserApplicationsByStatus("test@example.com", ApplicationStatus.APPLIED))
                .thenReturn(applications);

        ResponseEntity<List<JobApplicationDto>> response = jobApplicationController
                .getUserApplicationsByStatus("test@example.com", ApplicationStatus.APPLIED);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(ApplicationStatus.APPLIED, response.getBody().get(0).getStatus());
    }

    @Test
    public void testGetApplicationById_Found() {
        when(jobApplicationService.getApplicationById(1L))
                .thenReturn(Optional.of(testApplication));

        ResponseEntity<JobApplicationDto> response = jobApplicationController.getApplicationById(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetApplicationById_NotFound() {
        when(jobApplicationService.getApplicationById(999L))
                .thenReturn(Optional.empty());

        ResponseEntity<JobApplicationDto> response = jobApplicationController.getApplicationById(999L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void testUpdateStatus_Success() {
        when(jobApplicationService.updateStatus(1L, ApplicationStatus.INTERVIEWING))
                .thenReturn(testApplication);

        ResponseEntity<?> response = jobApplicationController.updateStatus(1L, ApplicationStatus.INTERVIEWING);

        assertEquals(200, response.getStatusCodeValue());
        verify(jobApplicationService, times(1)).updateStatus(1L, ApplicationStatus.INTERVIEWING);
    }

    @Test
    public void testUpdateStatus_NotFound() {
        when(jobApplicationService.updateStatus(999L, ApplicationStatus.INTERVIEWING))
                .thenThrow(new IllegalArgumentException("Application not found"));

        ResponseEntity<?> response = jobApplicationController.updateStatus(999L, ApplicationStatus.INTERVIEWING);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    public void testUpdateNotes_Success() {
        String newNotes = "Follow up scheduled";
        when(jobApplicationService.updateNotes(1L, newNotes))
                .thenReturn(testApplication);

        ResponseEntity<?> response = jobApplicationController.updateNotes(1L, newNotes);

        assertEquals(200, response.getStatusCodeValue());
        verify(jobApplicationService, times(1)).updateNotes(1L, newNotes);
    }

    @Test
    public void testGetApplicationsForJob() {
        List<JobApplication> applications = Arrays.asList(testApplication);
        when(jobApplicationService.getApplicationsForJob(1L))
                .thenReturn(applications);

        ResponseEntity<List<JobApplicationDto>> response = jobApplicationController.getApplicationsForJob(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testBatchApplicationScenario() {
        // Simulating multiple rapid applications (Open Top 10 scenario)
        CreateApplicationRequest req1 = new CreateApplicationRequest();
        req1.setJobId(1L);
        req1.setUserEmail("test@example.com");
        req1.setNotes("Batch apply");

        CreateApplicationRequest req2 = new CreateApplicationRequest();
        req2.setJobId(2L);
        req2.setUserEmail("test@example.com");
        req2.setNotes("Batch apply");

        when(jobApplicationService.applyToJob(anyLong(), anyString(), anyString()))
                .thenReturn(testApplication);

        ResponseEntity<?> response1 = jobApplicationController.applyToJob(req1);
        ResponseEntity<?> response2 = jobApplicationController.applyToJob(req2);

        assertEquals(201, response1.getStatusCodeValue());
        assertEquals(201, response2.getStatusCodeValue());
        verify(jobApplicationService, times(2)).applyToJob(anyLong(), anyString(), anyString());
    }
}
