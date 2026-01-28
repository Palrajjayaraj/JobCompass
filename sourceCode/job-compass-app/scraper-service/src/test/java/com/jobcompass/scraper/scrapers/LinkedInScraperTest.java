package com.jobcompass.scraper.scrapers;

import com.jobcompass.common.events.RawJobEvent;
import com.jobcompass.common.model.ScrapeParameters;
import com.jobcompass.common.model.Source;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.jobcompass.scraper.config.SeleniumProperties;
import com.jobcompass.scraper.filter.LanguageFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LinkedInScraper.
 * Note: These are basic tests. Full integration tests with Selenium
 * would require Testcontainers or similar.
 * 
 * @author Palraj Jayaraj
 */
class LinkedInScraperTest {

    @Mock
    private Browser browser;

    @Mock
    private SeleniumProperties properties;

    @Mock
    private LanguageFilter languageFilter;

    private LinkedInScraper linkedInScraper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        List<String> userAgents = new ArrayList<>();
        userAgents.add("test-agent");
        when(properties.getUserAgents()).thenReturn(userAgents);

        linkedInScraper = new LinkedInScraper(browser, properties, languageFilter);
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
    void testScrapeJobsWithMockedBrowser() {
        // Setup deep mocks for Playwright hierarchy
        BrowserContext context = mock(BrowserContext.class);
        Page page = mock(Page.class);

        when(browser.newContext(any(Browser.NewContextOptions.class))).thenReturn(context);
        when(context.newPage()).thenReturn(page);

        ScrapeParameters params = ScrapeParameters.of(7, 20);

        // Execute
        List<RawJobEvent> jobs = linkedInScraper.scrapeJobs(params);

        assertNotNull(jobs);
        assertTrue(jobs.isEmpty()); // Expect empty since we didn't mock page content
    }
}
