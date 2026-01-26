package com.jobcompass.storage.service;

import com.jobcompass.storage.entity.Company;
import com.jobcompass.storage.repository.CompanyRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CompanyService.
 * Tests find-or-create logic.
 * 
 * @author Palrajjayaraj
 */
@RunWith(MockitoJUnitRunner.class)
public class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    /**
     * Test finding existing company.
     */
    @Test
    public void testFindOrCreateCompany_ExistingCompany() {
        // Arrange
        Company existingCompany = Company.builder()
            .id(1L)
            .name("Google")
            .build();

        when(companyRepository.findByName("Google")).thenReturn(Optional.of(existingCompany));

        // Act
        Company result = companyService.findOrCreateCompany("Google");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals("Google", result.getName());

        verify(companyRepository, times(1)).findByName("Google");
        verify(companyRepository, never()).save(any(Company.class));
    }

    /**
     * Test creating new company.
     */
    @Test
    public void testFindOrCreateCompany_NewCompany() {
        // Arrange
        when(companyRepository.findByName("Apple")).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
            Company company = invocation.getArgument(0);
            company.setId(2L);
            return company;
        });

        // Act
        Company result = companyService.findOrCreateCompany("Apple");

        // Assert
        assertNotNull(result);
        assertEquals("Apple", result.getName());

        verify(companyRepository, times(1)).findByName("Apple");
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    /**
     * Test handling null company name.
     */
    @Test
    public void testFindOrCreateCompany_NullName() {
        // Act
        Company result = companyService.findOrCreateCompany(null);

        // Assert
        assertNull(result);

        verify(companyRepository, never()).findByName(any());
        verify(companyRepository, never()).save(any(Company.class));
    }

    /**
     * Test handling empty company name.
     */
    @Test
    public void testFindOrCreateCompany_EmptyName() {
        // Act
        Company result = companyService.findOrCreateCompany("   ");

        // Assert
        assertNull(result);

        verify(companyRepository, never()).findByName(any());
        verify(companyRepository, never()).save(any(Company.class));
    }
}
