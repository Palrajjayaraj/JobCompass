package com.jobcompass.storage.controller;

import com.jobcompass.storage.dto.SearchHistoryDto;
import com.jobcompass.storage.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for search history operations.
 * Provides endpoints to save and retrieve recent searches.
 *
 * @author Palraj Jayaraj
 */
@RestController
@RequestMapping("/api/search-history")
@RequiredArgsConstructor
@Slf4j
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    /**
     * Save a new search entry.
     *
     * @param request the search details (skill, location)
     * @return the saved search history entry
     */
    @PostMapping
    public ResponseEntity<SearchHistoryDto> recordSearch(@RequestBody SearchHistoryDto request) {
        log.info("Recording search - skill: '{}', location: '{}'", request.getSkill(), request.getLocation());
        SearchHistoryDto saved = searchHistoryService.recordSearch(request.getSkill(), request.getLocation());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Get the last 10 searches, most recent first.
     *
     * @return list of recent searches
     */
    @GetMapping
    public ResponseEntity<List<SearchHistoryDto>> getRecentSearches() {
        log.info("Fetching recent search history");
        List<SearchHistoryDto> searches = searchHistoryService.getRecentSearches();
        return ResponseEntity.ok(searches);
    }
}
