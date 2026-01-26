package com.jobcompass.storage.service;

import com.jobcompass.storage.entity.Skill;
import com.jobcompass.storage.entity.enums.SkillCategory;
import com.jobcompass.storage.repository.SkillRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SkillService.
 * 
 * @author Palrajjayaraj
 */
@RunWith(MockitoJUnitRunner.class)
public class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    private Skill javaSkill;
    private Skill springSkill;

    @Before
    public void setUp() {
        javaSkill = Skill.builder()
                .id(1L)
                .name("Java")
                .category(SkillCategory.PROGRAMMING_LANGUAGE)
                .build();

        springSkill = Skill.builder()
                .id(2L)
                .name("Spring Boot")
                .category(SkillCategory.FRAMEWORK)
                .build();
    }

    @Test
    public void testFindOrCreateSkills_ExistingSkills() {
        Set<String> skillNames = new HashSet<>();
        skillNames.add("Java");
        skillNames.add("Spring Boot");
        
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(javaSkill));
        when(skillRepository.findByName("Spring Boot")).thenReturn(Optional.of(springSkill));

        Set<Skill> result = skillService.findOrCreateSkills(skillNames);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(skillRepository, never()).save(any(Skill.class));
    }

    @Test
    public void testFindOrCreateSkills_NewSkill() {
        Set<String> skillNames = new HashSet<>();
        skillNames.add("Python");
        
        Skill pythonSkill = Skill.builder()
                .id(3L)
                .name("Python")
                .category(SkillCategory.PROGRAMMING_LANGUAGE)
                .build();

        when(skillRepository.findByName("Python")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenReturn(pythonSkill);

        Set<Skill> result = skillService.findOrCreateSkills(skillNames);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(skillRepository, times(1)).save(any(Skill.class));
    }

    @Test
    public void testFindOrCreateSkills_EmptySet() {
        Set<String> skillNames = new HashSet<>();

        Set<Skill> result = skillService.findOrCreateSkills(skillNames);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(skillRepository, never()).findByName(anyString());
    }

    @Test
    public void testFindOrCreateSkills_NullSet() {
        Set<Skill> result = skillService.findOrCreateSkills(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindOrCreateSkill_NewSkill() {
        when(skillRepository.findByName("Go")).thenReturn(Optional.empty());
        
        Skill goSkill = Skill.builder()
                .name("Go")
                .category(SkillCategory.PROGRAMMING_LANGUAGE)
                .build();
        
        when(skillRepository.save(any(Skill.class))).thenReturn(goSkill);

        Skill result = skillService.findOrCreateSkill("Go", SkillCategory.PROGRAMMING_LANGUAGE);

        assertNotNull(result);
        assertEquals("Go", result.getName());
        verify(skillRepository, times(1)).save(any(Skill.class));
    }

    @Test
    public void testFindOrCreateSkill_ExistingSkill() {
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(javaSkill));

        Skill result = skillService.findOrCreateSkill("Java", null);

        assertNotNull(result);
        assertEquals("Java", result.getName());
        verify(skillRepository, never()).save(any(Skill.class));
    }
}
