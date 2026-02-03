package com.jobcompass.storage.controller;

import com.jobcompass.common.model.Source;
import com.jobcompass.storage.dto.JobDto;
import com.jobcompass.storage.entity.Company;
import com.jobcompass.storage.entity.Job;
import com.jobcompass.storage.entity.Skill;
import com.jobcompass.storage.service.JobApplicationService;
import com.jobcompass.storage.service.JobService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JobController.
 * Tests the LazyInitializationException fix and DTO conversion logic.
 * 
 * @author Palrajjayaraj
 */
@RunWith(MockitoJUnitRunner.class)
public class JobControllerTest {

    @Mock
    private JobService jobService;

    @Mock
    private JobApplicationService jobApplicationService;

    @InjectMocks
    private JobController jobController;

    private Job testJob;
    private Company testCompany;
    private Set<Skill> testSkills;

    @Before
    public void setUp() {
        testCompany = Company.builder()
                .id(1L)
                .name("Tech Corp")
                .build();

        Skill skill1 = Skill.builder().id(1L).name("Java").build();
        Skill skill2 = Skill.builder().id(2L).name("Spring Boot").build();
        testSkills = new HashSet<>(Arrays.asList(skill1, skill2));

        testJob = Job.builder()
                .id(1L)
                .title("Senior Java Developer")
                .description("Great opportunity")
                .location("Germany")
                .salaryRange("€70k-€90k")
                .url("https://example.com/job/1")
                .postedDate(LocalDate.now())
                .jobAgeDays(1)
                .source(Source.LINKEDIN)
                .scrapedAt(LocalDateTime.now())
                .company(testCompany)
                .skills(testSkills)
                .isActive(true)
                .build();
    }

    @Test
    public void testGetAllJobs_WithEagerFetch_Success() {
        // Test that EAGER fetch prevents LazyInitializationException
        List<Job> jobs = Arrays.asList(testJob);
        when(jobService.findAllActiveJobs()).thenReturn(jobs);
        when(jobApplicationService.countApplicationsForJob(1L)).thenReturn(2L);

        ResponseEntity<List<JobDto>> response = jobController.getAllJobs();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        List<JobDto> result = response.getBody();
        assertNotNull(result);
        assertEquals(1, result.size());

        JobDto dto = result.get(0);
        assertEquals("Senior Java Developer", dto.getTitle());
        assertEquals("Tech Corp", dto.getCompanyName()); // Company should be accessible (EAGER)
        assertEquals(2, dto.getSkills().size()); // Skills should be accessible (EAGER)
        assertTrue(dto.getSkills().contains("Java"));
        assertTrue(dto.getSkills().contains("Spring Boot"));
        assertEquals(Long.valueOf(2), dto.getApplicationCount());
    }

    @Test
    public void testGetJobById_WithApplicationCount() {
        when(jobService.findById(1L)).thenReturn(Optional.of(testJob));
        when(jobApplicationService.countApplicationsForJob(1L)).thenReturn(3L);

        ResponseEntity<JobDto> response = jobController.getJobById(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        JobDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals(Long.valueOf(3), dto.getApplicationCount());
    }

    @Test
    public void testGetJobById_NotFound() {
        when(jobService.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<JobDto> response = jobController.getJobById(999L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void testGetRecentJobs() {
        List<Job> jobs = Arrays.asList(testJob);
        when(jobService.findRecentJobs(7)).thenReturn(jobs);
        when(jobApplicationService.countApplicationsForJob(1L)).thenReturn(0L);

        ResponseEntity<List<JobDto>> response = jobController.getRecentJobs(7);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testSearchBySkills() {
        Set<String> skills = new HashSet<>(Arrays.asList("Java", "Spring Boot"));
        List<Job> jobs = Arrays.asList(testJob);

        when(jobService.findBySkills(skills)).thenReturn(jobs);
        when(jobApplicationService.countApplicationsForJob(1L)).thenReturn(0L);

        ResponseEntity<List<JobDto>> response = jobController.searchBySkills(skills);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testSearchByLocation() {
        List<Job> jobs = Arrays.asList(testJob);
        when(jobService.findByLocation("Germany")).thenReturn(jobs);
        when(jobApplicationService.countApplicationsForJob(1L)).thenReturn(0L);

        ResponseEntity<List<JobDto>> response = jobController.searchByLocation("Germany");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testSearchByCompany() {
        List<Job> jobs = Arrays.asList(testJob);
        when(jobService.findByCompanyName("Tech Corp")).thenReturn(jobs);
        when(jobApplicationService.countApplicationsForJob(1L)).thenReturn(0L);

        ResponseEntity<List<JobDto>> response = jobController.searchByCompany("Tech Corp");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testGetJobsBySource() {
        List<Job> jobs = Arrays.asList(testJob);
        when(jobService.findBySource(Source.LINKEDIN)).thenReturn(jobs);
        when(jobApplicationService.countApplicationsForJob(1L)).thenReturn(0L);

        ResponseEntity<List<JobDto>> response = jobController.getJobsBySource(Source.LINKEDIN);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testConvertToDto_WithNullCompany() {
        Job jobWithoutCompany = Job.builder()
                .id(2L)
                .title("Test Job")
                .company(null)
                .skills(new HashSet<>())
                .source(Source.LINKEDIN)
                .build();

        List<Job> jobs = Arrays.asList(jobWithoutCompany);
        when(jobService.findAllActiveJobs()).thenReturn(jobs);
        when(jobApplicationService.countApplicationsForJob(2L)).thenReturn(0L);

        ResponseEntity<List<JobDto>> response = jobController.getAllJobs();

        JobDto dto = response.getBody().get(0);
        assertNull(dto.getCompanyName()); // Should handle null company gracefully
    }
}
