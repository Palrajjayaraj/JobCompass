package com.jobcompass.storage.repository;

import com.jobcompass.storage.entity.Skill;
import com.jobcompass.storage.entity.enums.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for Skill entity.
 * Provides CRUD operations and custom queries for skills.
 * 
 * @author Palrajjayaraj
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    /**
     * Find a skill by its name.
     * 
     * @param name the skill name
     * @return Optional containing the skill if found
     */
    Optional<Skill> findByName(String name);

    /**
     * Find all skills by category.
     * 
     * @param category the skill category
     * @return list of skills in the category
     */
    List<Skill> findByCategory(SkillCategory category);

    /**
     * Find skills by names (case-insensitive).
     * 
     * @param names set of skill names
     * @return list of matching skills
     */
    List<Skill> findByNameIn(Set<String> names);

    /**
     * Check if a skill exists by name.
     * 
     * @param name the skill name
     * @return true if skill exists, false otherwise
     */
    boolean existsByName(String name);
}
