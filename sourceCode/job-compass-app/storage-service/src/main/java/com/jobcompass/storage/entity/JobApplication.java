package com.jobcompass.storage.entity;

import com.jobcompass.storage.entity.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a user's application to a job.
 * Tracks application status and user notes.
 * 
 * @author Palrajjayaraj
 */
@Entity
@Table(name = "job_applications", indexes = {
    @Index(name = "idx_job_application_job_id", columnList = "job_id"),
    @Index(name = "idx_job_application_user_email", columnList = "user_email"),
    @Index(name = "idx_job_application_user_status", columnList = "user_email, status"),
    @Index(name = "idx_job_application_user_job", columnList = "user_email, job_id", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "applied_at", nullable = false)
    @Builder.Default
    private LocalDateTime appliedAt = LocalDateTime.now();

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
