package com.jobcompass.storage.repository;

import com.jobcompass.storage.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for SearchHistory entity.
 * Provides methods to persist and retrieve recent searches.
 *
 * @author Palraj Jayaraj
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    /**
     * Find the most recent 10 search history entries, ordered by newest first.
     *
     * @return list of up to 10 recent searches
     */
    List<SearchHistory> findTop10ByOrderBySearchedAtDesc();
}
