package com.jobcompass.storage.service;

import com.jobcompass.storage.entity.Company;
import com.jobcompass.storage.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Company entity operations.
 * Handles business logic for company management.
 * 
 * @author Palrajjayaraj
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;

    /**
     * Find or create a company by name.
     * If the company doesn't exist, creates a new one with the given name.
     * 
     * @param name the company name
     * @return the existing or newly created company
     */
    @Transactional
    public Company findOrCreateCompany(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        return companyRepository.findByName(name)
            .orElseGet(() -> {
                log.info("Creating new company: {}", name);
                Company newCompany = Company.builder()
                    .name(name)
                    .build();
                return companyRepository.save(newCompany);
            });
    }

    /**
     * Find a company by name.
     * 
     * @param name the company name
     * @return Optional containing the company if found
     */
    public Optional<Company> findByName(String name) {
        return companyRepository.findByName(name);
    }

    /**
     * Find all companies.
     * 
     * @return list of all companies
     */
    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    /**
     * Find all companies by industry.
     * 
     * @param industry the industry name
     * @return list of companies in the industry
     */
    public List<Company> findByIndustry(String industry) {
        return companyRepository.findByIndustry(industry);
    }

    /**
     * Update company details.
     * 
     * @param company the company to update
     * @return the updated company
     */
    @Transactional
    public Company updateCompany(Company company) {
        log.info("Updating company: {}", company.getName());
        return companyRepository.save(company);
    }
}
