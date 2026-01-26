package com.jobcompass.storage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a company that posts jobs.
 * Maintains one-to-many relationship with Job entities.
 * 
 * @author Palrajjayaraj
 */
@Entity
@Table(name = "companies", indexes = {
    @Index(name = "idx_company_name", columnList = "name"),
    @Index(name = "idx_company_industry", columnList = "industry")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "website", length = 500)
    private String website;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<Job> jobs = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
