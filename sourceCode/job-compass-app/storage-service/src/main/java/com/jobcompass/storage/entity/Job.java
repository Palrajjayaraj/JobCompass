package com.jobcompass.storage.entity;

import com.jobcompass.common.model.Source;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing a job posting.
 * Central entity with relationships to Company, Skill, and JobApplication.
 * 
 * @author Palrajjayaraj
 */
@Entity
@Table(name = "jobs", indexes = {
    @Index(name = "idx_job_url", columnList = "url", unique = true),
    @Index(name = "idx_job_external_id", columnList = "external_id"),
    @Index(name = "idx_job_source", columnList = "source"),
    @Index(name = "idx_job_posted_date", columnList = "posted_date"),
    @Index(name = "idx_job_company_id", columnList = "company_id"),
    @Index(name = "idx_job_source_external", columnList = "source, external_id"),
    @Index(name = "idx_job_is_active", columnList = "is_active"),
    @Index(name = "idx_job_location", columnList = "location")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", length = 255)
    private String externalId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "salary_range", length = 100)
    private String salaryRange;

    @Column(name = "url", nullable = false, unique = true, length = 1000)
    private String url;

    @Column(name = "posted_date")
    private LocalDate postedDate;

    @Column(name = "job_age_days")
    private Integer jobAgeDays;

    @Column(name = "source", nullable = false, length = 50)
    private Source source;

    @Column(name = "scraped_at", nullable = false)
    private LocalDateTime scrapedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "job_skills",
        joinColumns = @JoinColumn(name = "job_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobApplication> applications = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
