package com.jobcompass.storage.service;

import com.jobcompass.storage.dto.SearchHistoryDto;
import com.jobcompass.storage.entity.SearchHistory;
import com.jobcompass.storage.repository.SearchHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SearchHistoryService.
 *
 * @author Palraj Jayaraj
 */
@ExtendWith(MockitoExtension.class)
class SearchHistoryServiceTest {

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @InjectMocks
    private SearchHistoryService searchHistoryService;

    private SearchHistory sampleEntry;

    @BeforeEach
    void setUp() {
        sampleEntry = SearchHistory.builder()
                .id(1L)
                .skill("Java Developer")
                .location("San Francisco")
                .searchedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void recordSearch_shouldSaveAndReturnDto() {
        when(searchHistoryRepository.save(any(SearchHistory.class))).thenReturn(sampleEntry);

        SearchHistoryDto result = searchHistoryService.recordSearch("Java Developer", "San Francisco");

        assertNotNull(result);
        assertEquals("Java Developer", result.getSkill());
        assertEquals("San Francisco", result.getLocation());
        assertNotNull(result.getSearchedAt());
        verify(searchHistoryRepository, times(1)).save(any(SearchHistory.class));
    }

    @Test
    void recordSearch_withNullSkill_shouldSaveEmptyString() {
        SearchHistory savedEntry = SearchHistory.builder()
                .id(2L)
                .skill("")
                .location("London")
                .searchedAt(LocalDateTime.now())
                .build();
        when(searchHistoryRepository.save(any(SearchHistory.class))).thenReturn(savedEntry);

        SearchHistoryDto result = searchHistoryService.recordSearch(null, "London");

        assertNotNull(result);
        assertEquals("", result.getSkill());
        assertEquals("London", result.getLocation());
        verify(searchHistoryRepository, times(1)).save(any(SearchHistory.class));
    }

    @Test
    void recordSearch_withNullLocation_shouldSaveEmptyString() {
        SearchHistory savedEntry = SearchHistory.builder()
                .id(3L)
                .skill("Python")
                .location("")
                .searchedAt(LocalDateTime.now())
                .build();
        when(searchHistoryRepository.save(any(SearchHistory.class))).thenReturn(savedEntry);

        SearchHistoryDto result = searchHistoryService.recordSearch("Python", null);

        assertNotNull(result);
        assertEquals("Python", result.getSkill());
        assertEquals("", result.getLocation());
        verify(searchHistoryRepository, times(1)).save(any(SearchHistory.class));
    }

    @Test
    void getRecentSearches_shouldReturnListOrderedByMostRecent() {
        LocalDateTime now = LocalDateTime.now();
        SearchHistory entry1 = SearchHistory.builder()
                .id(1L).skill("Java").location("NYC").searchedAt(now.minusHours(2)).build();
        SearchHistory entry2 = SearchHistory.builder()
                .id(2L).skill("Python").location("London").searchedAt(now.minusHours(1)).build();
        SearchHistory entry3 = SearchHistory.builder()
                .id(3L).skill("React").location("Berlin").searchedAt(now).build();

        when(searchHistoryRepository.findTop10ByOrderBySearchedAtDesc())
                .thenReturn(Arrays.asList(entry3, entry2, entry1));

        List<SearchHistoryDto> results = searchHistoryService.getRecentSearches();

        assertEquals(3, results.size());
        assertEquals("React", results.get(0).getSkill());
        assertEquals("Python", results.get(1).getSkill());
        assertEquals("Java", results.get(2).getSkill());
        verify(searchHistoryRepository, times(1)).findTop10ByOrderBySearchedAtDesc();
    }

    @Test
    void getRecentSearches_whenEmpty_shouldReturnEmptyList() {
        when(searchHistoryRepository.findTop10ByOrderBySearchedAtDesc())
                .thenReturn(Collections.emptyList());

        List<SearchHistoryDto> results = searchHistoryService.getRecentSearches();

        assertTrue(results.isEmpty());
        verify(searchHistoryRepository, times(1)).findTop10ByOrderBySearchedAtDesc();
    }

    @Test
    void recordSearch_withWhitespace_shouldTrimValues() {
        SearchHistory savedEntry = SearchHistory.builder()
                .id(4L)
                .skill("Spring Boot")
                .location("Tokyo")
                .searchedAt(LocalDateTime.now())
                .build();
        when(searchHistoryRepository.save(any(SearchHistory.class))).thenReturn(savedEntry);

        SearchHistoryDto result = searchHistoryService.recordSearch("  Spring Boot  ", "  Tokyo  ");

        assertNotNull(result);
        verify(searchHistoryRepository, times(1)).save(any(SearchHistory.class));
    }
}
