package com.jobcompass.storage.service;

import com.jobcompass.storage.dto.SearchHistoryDto;
import com.jobcompass.storage.entity.SearchHistory;
import com.jobcompass.storage.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing search history.
 * Records searches and retrieves the most recent entries.
 *
 * @author Palraj Jayaraj
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;

    /**
     * Record a new search with the given skill and location.
     *
     * @param skill    the skill keyword searched
     * @param location the location keyword searched
     * @return the saved search history DTO
     */
    @Transactional
    public SearchHistoryDto recordSearch(String skill, String location) {
        log.info("Recording search - skill: '{}', location: '{}'", skill, location);

        SearchHistory entry = SearchHistory.builder()
                .skill(skill != null ? skill.trim() : "")
                .location(location != null ? location.trim() : "")
                .searchedAt(LocalDateTime.now())
                .build();

        SearchHistory saved = searchHistoryRepository.save(entry);
        return convertToDto(saved);
    }

    /**
     * Retrieve the last 10 searches, ordered by most recent first.
     *
     * @return list of up to 10 recent search history DTOs
     */
    @Transactional(readOnly = true)
    public List<SearchHistoryDto> getRecentSearches() {
        log.debug("Retrieving last 10 searches");
        List<SearchHistory> entries = searchHistoryRepository.findTop10ByOrderBySearchedAtDesc();
        return entries.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert SearchHistory entity to SearchHistoryDto.
     */
    private SearchHistoryDto convertToDto(SearchHistory entity) {
        return SearchHistoryDto.builder()
                .id(entity.getId())
                .skill(entity.getSkill())
                .location(entity.getLocation())
                .searchedAt(entity.getSearchedAt())
                .build();
    }
}
