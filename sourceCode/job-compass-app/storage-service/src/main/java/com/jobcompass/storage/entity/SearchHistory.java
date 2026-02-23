package com.jobcompass.storage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a user's search history entry.
 * Stores skill and location pairs for quick re-use across sessions.
 *
 * @author Palraj Jayaraj
 */
@Entity
@Table(name = "search_history", indexes = {
        @Index(name = "idx_search_history_searched_at", columnList = "searched_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "skill", length = 255)
    private String skill;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "searched_at", nullable = false)
    @Builder.Default
    private LocalDateTime searchedAt = LocalDateTime.now();
}
