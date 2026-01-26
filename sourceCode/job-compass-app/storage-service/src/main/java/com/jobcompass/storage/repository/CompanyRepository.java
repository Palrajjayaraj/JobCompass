package com.jobcompass.storage.repository;

import com.jobcompass.storage.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Company entity.
 * Provides CRUD operations and custom queries for companies.
 * 
 * @author Palrajjayaraj
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Find a company by its name.
     * 
     * @param name the company name
     * @return Optional containing the company if found
     */
    Optional<Company> findByName(String name);

    /**
     * Find all companies in a specific industry.
     * 
     * @param industry the industry name
     * @return list of companies in the industry
     */
    List<Company> findByIndustry(String industry);

    /**
     * Check if a company exists by name.
     * 
     * @param name the company name
     * @return true if company exists, false otherwise
     */
    boolean existsByName(String name);
}
