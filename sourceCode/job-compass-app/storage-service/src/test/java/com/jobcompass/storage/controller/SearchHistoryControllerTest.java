package com.jobcompass.storage.controller;

import com.jobcompass.storage.dto.SearchHistoryDto;
import com.jobcompass.storage.service.SearchHistoryService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SearchHistoryController.
 *
 * @author Palraj Jayaraj
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchHistoryControllerTest {

    @Mock
    private SearchHistoryService searchHistoryService;

    @InjectMocks
    private SearchHistoryController searchHistoryController;

    private SearchHistoryDto sampleDto;

    @Before
    public void setUp() {
        sampleDto = SearchHistoryDto.builder()
                .id(1L)
                .skill("Java Developer")
                .location("San Francisco")
                .searchedAt(LocalDateTime.now())
                .build();
    }

    @Test
    public void testRecordSearch_shouldReturnCreatedStatus() {
        SearchHistoryDto request = SearchHistoryDto.builder()
                .skill("Java Developer")
                .location("San Francisco")
                .build();

        when(searchHistoryService.recordSearch("Java Developer", "San Francisco"))
                .thenReturn(sampleDto);

        ResponseEntity<SearchHistoryDto> response = searchHistoryController.recordSearch(request);

        assertNotNull(response);
        assertEquals(201, response.getStatusCodeValue());

        SearchHistoryDto body = response.getBody();
        assertNotNull(body);
        assertEquals("Java Developer", body.getSkill());
        assertEquals("San Francisco", body.getLocation());
        assertEquals(Long.valueOf(1), body.getId());
        verify(searchHistoryService, times(1)).recordSearch("Java Developer", "San Francisco");
    }

    @Test
    public void testGetRecentSearches_shouldReturnOrderedList() {
        LocalDateTime now = LocalDateTime.now();
        SearchHistoryDto dto1 = SearchHistoryDto.builder()
                .id(1L).skill("React").location("Berlin").searchedAt(now).build();
        SearchHistoryDto dto2 = SearchHistoryDto.builder()
                .id(2L).skill("Python").location("London").searchedAt(now.minusHours(1)).build();

        when(searchHistoryService.getRecentSearches()).thenReturn(Arrays.asList(dto1, dto2));

        ResponseEntity<List<SearchHistoryDto>> response = searchHistoryController.getRecentSearches();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        List<SearchHistoryDto> body = response.getBody();
        assertNotNull(body);
        assertEquals(2, body.size());
        assertEquals("React", body.get(0).getSkill());
        assertEquals("Python", body.get(1).getSkill());
        verify(searchHistoryService, times(1)).getRecentSearches();
    }

    @Test
    public void testGetRecentSearches_whenEmpty_shouldReturnEmptyList() {
        when(searchHistoryService.getRecentSearches()).thenReturn(Collections.emptyList());

        ResponseEntity<List<SearchHistoryDto>> response = searchHistoryController.getRecentSearches();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
        verify(searchHistoryService, times(1)).getRecentSearches();
    }
}
