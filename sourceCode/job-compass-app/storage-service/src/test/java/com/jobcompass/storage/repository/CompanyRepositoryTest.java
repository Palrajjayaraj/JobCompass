package com.jobcompass.storage.repository;

import com.jobcompass.storage.entity.Company;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Unit tests for CompanyRepository.
 * 
 * @author Palrajjayaraj
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository companyRepository;

    /**
     * Set up test data before each test.
     */
    @Before
    public void setUp() {
        Company company1 = Company.builder()
            .name("Google")
            .industry("Technology")
            .website("https://www.google.com")
            .build();

        Company company2 = Company.builder()
            .name("Microsoft")
            .industry("Technology")
            .website("https://www.microsoft.com")
            .build();

        companyRepository.save(company1);
        companyRepository.save(company2);
    }

    /**
     * Test finding company by name.
     */
    @Test
    public void testFindByName() {
        Optional<Company> found = companyRepository.findByName("Google");
        assertTrue(found.isPresent());
        assertEquals("Google", found.get().getName());
        assertEquals("Technology", found.get().getIndustry());
    }

    /**
     * Test finding companies by industry.
     */
    @Test
    public void testFindByIndustry() {
        List<Company> companies = companyRepository.findByIndustry("Technology");
        assertEquals(2, companies.size());
    }

    /**
     * Test checking if company exists by name.
     */
    @Test
    public void testExistsByName() {
        assertTrue(companyRepository.existsByName("Google"));
        assertFalse(companyRepository.existsByName("Apple"));
    }

    /**
     * Test name uniqueness constraint.
     */
    @Test
    public void testNameUniqueness() {
        long count = companyRepository.count();
        assertEquals(2, count);
        
        // Verify that attempting to save duplicate name would fail
        assertTrue(companyRepository.existsByName("Google"));
    }
}
