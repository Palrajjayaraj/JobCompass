package com.jobcompass.storage.entity;

import com.jobcompass.storage.entity.enums.SkillCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a technical skill or technology.
 * Maintains many-to-many relationship with Job entities.
 * 
 * @author Palrajjayaraj
 */
@Entity
@Table(name = "skills", indexes = {
    @Index(name = "idx_skill_name", columnList = "name", unique = true),
    @Index(name = "idx_skill_category", columnList = "category")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private SkillCategory category;

    @ManyToMany(mappedBy = "skills")
    @Builder.Default
    private Set<Job> jobs = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
