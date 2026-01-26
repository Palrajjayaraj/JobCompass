package com.jobcompass.scraper.scrapers;

import com.jobcompass.common.events.RawJobEvent;
import com.jobcompass.common.model.ScrapeParameters;
import com.jobcompass.common.model.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebDriver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LinkedInScraper.
 * Note: These are basic tests. Full integration tests with Selenium
 * would require Testcontainers or similar.
 * 
 * @author Palraj Jayaraj
 */
class LinkedInScraperTest {

    @Mock
    private WebDriver webDriver;

    @Mock
    private com.jobcompass.scraper.filter.LanguageFilter languageFilter;

    private LinkedInScraper linkedInScraper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        linkedInScraper = new LinkedInScraper(webDriver, languageFilter);
    }

    @Test
    void testGetSource() {
        Source source = linkedInScraper.getSource();

        assertNotNull(source);
        assertEquals("LinkedIn", source.name());
    }

    @Test
    void testIsEnabled() {
        assertTrue(linkedInScraper.isEnabled());
    }

    @Test
    void testScrapeJobsWithNullParameters() {
        ScrapeParameters params = ScrapeParameters.of(7, 20);

        // This will fail to scrape since WebDriver is mocked
        // but should not throw an exception
        List<RawJobEvent> jobs = linkedInScraper.scrapeJobs(params);

        assertNotNull(jobs);
        // With mocked driver, we expect empty list
        assertTrue(jobs.isEmpty());
    }
}
