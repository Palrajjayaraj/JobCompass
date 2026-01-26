package com.jobcompass.storage.repository;

import com.jobcompass.common.model.Source;
import com.jobcompass.storage.entity.Company;
import com.jobcompass.storage.entity.Job;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for JobRepository.
 * Tests custom queries and relationship operations.
 * 
 * @author Palrajjayaraj
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class JobRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company testCompany;
    private Job testJob1;
    private Job testJob2;

    /**
     * Set up test data before each test.
     */
    @Before
    public void setUp() {
        // Create test company
        testCompany = Company.builder()
            .name("Test Company")
            .industry("Technology")
            .build();
        testCompany = companyRepository.save(testCompany);

        // Create test jobs
        testJob1 = Job.builder()
            .externalId("ext-123")
            .title("Senior Java Developer")
            .description("Java development role")
            .location("San Francisco, CA")
            .url("https://example.com/job1")
            .salaryRange("$120k-150k")
            .postedDate(LocalDate.now().minusDays(3))
            .jobAgeDays(3)
            .source(Source.of("LINKEDIN"))
            .scrapedAt(LocalDateTime.now())
            .company(testCompany)
            .isActive(true)
            .build();

        testJob2 = Job.builder()
            .externalId("ext-456")
            .title("Python Developer")
            .description("Python development role")
            .location("Remote")
            .url("https://example.com/job2")
            .salaryRange("$100k-130k")
            .postedDate(LocalDate.now().minusDays(10))
            .jobAgeDays(10)
            .source(Source.of("GLASSDOOR"))
            .scrapedAt(LocalDateTime.now())
            .company(testCompany)
            .isActive(true)
            .build();

        jobRepository.save(testJob1);
        jobRepository.save(testJob2);
    }

    /**
     * Test finding job by URL.
     */
    @Test
    public void testFindByUrl() {
        Optional<Job> found = jobRepository.findByUrl("https://example.com/job1");
        assertTrue(found.isPresent());
        assertEquals("Senior Java Developer", found.get().getTitle());
    }

    /**
     * Test finding job by source and external ID.
     */
    @Test
    public void testFindBySourceAndExternalId() {
        Optional<Job> found = jobRepository.findBySourceAndExternalId(Source.of("LINKEDIN"), "ext-123");
        assertTrue(found.isPresent());
        assertEquals("Senior Java Developer", found.get().getTitle());
    }

    /**
     * Test finding jobs by company.
     */
    @Test
    public void testFindByCompany() {
        List<Job> jobs = jobRepository.findByCompany(testCompany);
        assertEquals(2, jobs.size());
    }

    /**
     * Test finding active jobs by source.
     */
    @Test
    public void testFindBySourceAndIsActive() {
        List<Job> jobs = jobRepository.findBySourceAndIsActive(Source.of("LINKEDIN"), true);
        assertEquals(1, jobs.size());
        assertEquals("Senior Java Developer", jobs.get(0).getTitle());
    }

    /**
     * Test finding recent jobs.
     */
    @Test
    public void testFindRecentJobs() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        List<Job> jobs = jobRepository.findRecentJobs(startDate);
        assertEquals(1, jobs.size());
        assertEquals("Senior Java Developer", jobs.get(0).getTitle());
    }

    /**
     * Test finding jobs by location.
     */
    @Test
    public void testFindByLocationContainingIgnoreCase() {
        List<Job> jobs = jobRepository.findByLocationContainingIgnoreCase("san francisco");
        assertEquals(1, jobs.size());
        assertEquals("Senior Java Developer", jobs.get(0).getTitle());
    }

    /**
     * Test finding jobs by company name.
     */
    @Test
    public void testFindByCompanyNameContainingIgnoreCase() {
        List<Job> jobs = jobRepository.findByCompanyNameContainingIgnoreCase("test");
        assertEquals(2, jobs.size());
    }

    /**
     * Test URL uniqueness constraint.
     */
    @Test(expected = Exception.class)
    public void testUrlUniquenessConstraint() {
        Job duplicateJob = Job.builder()
            .title("Duplicate Job")
            .url("https://example.com/job1")  // Same URL as testJob1
            .source(Source.of("LINKEDIN"))
            .scrapedAt(LocalDateTime.now())
            .isActive(true)
            .build();

        jobRepository.save(duplicateJob);
        entityManager.flush();  // Force database constraint check
    }
}
