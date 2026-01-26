package com.jobcompass.storage.service;

import com.jobcompass.storage.entity.Skill;
import com.jobcompass.storage.entity.enums.SkillCategory;
import com.jobcompass.storage.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Service class for Skill entity operations.
 * Handles business logic for skill management.
 * 
 * @author Palrajjayaraj
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {

    private final SkillRepository skillRepository;

    /**
     * Find or create a skill by name.
     * 
     * @param name the skill name
     * @param category the skill category (optional)
     * @return the existing or newly created skill
     */
    @Transactional
    public Skill findOrCreateSkill(String name, SkillCategory category) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        return skillRepository.findByName(name)
            .orElseGet(() -> {
                log.info("Creating new skill: {} (category: {})", name, category);
                Skill newSkill = Skill.builder()
                    .name(name)
                    .category(category)
                    .build();
                return skillRepository.save(newSkill);
            });
    }

    /**
     * Find or create multiple skills by names.
     * 
     * @param skillNames set of skill names
     * @return set of existing or newly created skills
     */
    @Transactional
    public Set<Skill> findOrCreateSkills(Set<String> skillNames) {
        if (skillNames == null || skillNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Skill> skills = new HashSet<>();
        for (String skillName : skillNames) {
            if (skillName != null && !skillName.trim().isEmpty()) {
                Skill skill = findOrCreateSkill(skillName, null);
                if (skill != null) {
                    skills.add(skill);
                }
            }
        }

        return skills;
    }
}
