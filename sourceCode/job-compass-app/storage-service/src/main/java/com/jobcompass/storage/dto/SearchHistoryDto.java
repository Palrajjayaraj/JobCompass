package com.jobcompass.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for SearchHistory entity.
 * Used for API requests and responses.
 *
 * @author Palraj Jayaraj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryDto {

    private Long id;
    private String skill;
    private String location;
    private LocalDateTime searchedAt;
}
