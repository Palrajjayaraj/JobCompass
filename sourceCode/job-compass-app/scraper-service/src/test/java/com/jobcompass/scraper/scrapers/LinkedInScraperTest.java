package com.jobcompass.scraper.scrapers;

import com.jobcompass.common.events.RawJobEvent;
import com.jobcompass.common.model.ScrapeParameters;
import com.jobcompass.common.model.Source;

import com.jobcompass.common.model.authentication.LinkedInAuthentication;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import com.jobcompass.scraper.config.SeleniumProperties;
import com.jobcompass.scraper.filter.LanguageFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LinkedInScraper.
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

    @Mock
    private BrowserContext context;

    @Mock
    private Page page;

    private LinkedInScraper linkedInScraper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        List<String> userAgents = new ArrayList<>();
        userAgents.add("test-agent");
        when(properties.getUserAgents()).thenReturn(userAgents);

        when(browser.newContext(any(Browser.NewContextOptions.class))).thenReturn(context);
        when(context.newPage()).thenReturn(page);
        when(languageFilter.detectLanguage(anyString())).thenReturn("en");

        linkedInScraper = new LinkedInScraper(browser, properties, languageFilter);
    }

    @Test
    void testGetSource() {
        assertEquals(Source.LINKEDIN, linkedInScraper.getSource());
    }

    @Test
    void testCookieDomainFix() {
        // Setup authentication
        LinkedInAuthentication auth = new LinkedInAuthentication("valid-cookie");
        ScrapeParameters params = mock(ScrapeParameters.class);
        when(params.getAuthFor(Source.LINKEDIN)).thenReturn(Optional.of(auth));
        when(params.skill()).thenReturn("Java");
        when(params.location()).thenReturn("Berlin");
        when(params.maxJobAgeDays()).thenReturn(1);

        // Execute
        linkedInScraper.scrapeJobs(params);

        // Verify cookie injection
        ArgumentCaptor<List<Cookie>> cookiesCaptor = ArgumentCaptor.forClass(List.class);
        verify(context).addCookies(cookiesCaptor.capture());

        List<Cookie> capturedCookies = cookiesCaptor.getValue();
        assertEquals(1, capturedCookies.size());
        Cookie cookie = capturedCookies.get(0);
        assertEquals("li_at", cookie.name);
        assertEquals("valid-cookie", cookie.value);
        assertEquals(".linkedin.com", cookie.domain, "Cookie domain must be .linkedin.com");
    }

    @Test
    void testSelectorFallbackLogic() {
        ScrapeParameters params = ScrapeParameters.of(7, 20);

        // Mock Locators
        Locator jobContainer = mock(Locator.class);
        when(jobContainer.count()).thenReturn(0); // Primary selector fails

        Locator occludableUpdate = mock(Locator.class);
        when(occludableUpdate.count()).thenReturn(2); // Secondary selector succeeds

        // Register locator calls
        when(page.locator("div.job-card-container")).thenReturn(jobContainer);
        when(page.locator("li.occludable-update")).thenReturn(occludableUpdate);

        // Mock individual card interaction to avoid NPEs during extraction
        Locator card = mock(Locator.class);
        when(occludableUpdate.nth(anyInt())).thenReturn(card);

        // Mock extraction internals (simplified to return null/empty to skip
        // processing)
        when(card.locator(anyString())).thenReturn(mock(Locator.class));

        // Execute
        List<RawJobEvent> jobs = linkedInScraper.scrapeJobs(params);

        // Verify logic flow
        verify(page).locator("div.job-card-container");
        verify(page).locator("li.occludable-update");
        // Should stop checking others if one found
        verify(page, never()).locator("div.base-card");
    }

    @Test
    void testScreenshotOnTimeout() {
        ScrapeParameters params = ScrapeParameters.of(7, 20);

        // Force timeout exception
        doThrow(new PlaywrightException("Timeout")).when(page).waitForSelector(anyString(), any());

        // Setup screenshot mock
        when(page.locator(anyString())).thenReturn(mock(Locator.class)); // prevent NPE later

        // Execute
        linkedInScraper.scrapeJobs(params);

        // Verify screenshot attempt
        verify(page).screenshot(any(Page.ScreenshotOptions.class));
    }

    @Test
    void testNewSelectorExtraction() {
        ScrapeParameters params = ScrapeParameters.of(7, 1);

        // Mock job cards found
        Locator jobCards = mock(Locator.class);
        when(jobCards.count()).thenReturn(1);
        when(page.locator("div.job-card-container")).thenReturn(jobCards);

        // Mock specific card
        Locator card = mock(Locator.class);
        when(jobCards.nth(0)).thenReturn(card);

        // Setup Locator mocks for fields
        configureLocator(card, ".job-card-list__title", "Test Title");
        configureLocator(card, ".job-card-container__primary-description", "Test Company");
        configureLocator(card, ".job-card-container__metadata-item", "Test Location");
        configureLocatorAttribute(card, "a.job-card-list__title", "href", "http://test-job.url");

        // Mock full description fetch
        Page detailPage = mock(Page.class);
        when(context.newPage()).thenReturn(page, detailPage); // Return main page first, then detail page
        configureLocator(detailPage, "div.description__text", "Full Description");

        // Execute
        List<RawJobEvent> jobs = linkedInScraper.scrapeJobs(params);

        // Verify extraction
        assertEquals(1, jobs.size());
        RawJobEvent job = jobs.get(0);
        assertEquals("Test Title", job.getTitle());
        assertEquals("Test Company", job.getCompany());
        assertEquals("Test Location", job.getLocation());
        assertEquals("http://test-job.url", job.getUrl());
    }

    @Test
    void testUrlCleaning() {
        ScrapeParameters params = ScrapeParameters.of(7, 1);

        // Mock job cards found
        Locator jobCards = mock(Locator.class);
        when(jobCards.count()).thenReturn(1);
        when(page.locator("div.job-card-container")).thenReturn(jobCards);

        // Mock specific card
        Locator card = mock(Locator.class);
        when(jobCards.nth(0)).thenReturn(card);

        // Setup Locator mocks with dirty URL
        configureLocator(card, ".job-card-list__title", "Test Title");
        configureLocator(card, ".job-card-container__primary-description", "Test Company");
        configureLocator(card, ".job-card-container__metadata-item", "Test Location");
        String dirtyUrl = "https://www.linkedin.com/jobs/view/123456/?eBP=NOT_ELIGIBLE&trackingId=xyz";
        configureLocatorAttribute(card, "a.job-card-list__title", "href", dirtyUrl);

        // Mock full description fetch
        Page detailPage = mock(Page.class);
        when(context.newPage()).thenReturn(page, detailPage);
        configureLocator(detailPage, "div.description__text", "Full Description");

        // Execute
        List<RawJobEvent> jobs = linkedInScraper.scrapeJobs(params);

        // Verify URL is cleaned
        assertEquals(1, jobs.size());
        assertEquals("https://www.linkedin.com/jobs/view/123456/", jobs.get(0).getUrl());
    }

    // Helper to mock locator with text
    private void configureLocator(Locator parent, String selector, String text) {
        Locator loc = mock(Locator.class);
        when(loc.count()).thenReturn(1);
        when(loc.first()).thenReturn(loc);
        when(loc.innerText()).thenReturn(text);
        when(parent.locator(selector)).thenReturn(loc);
    }

    private void configureLocator(Page parent, String selector, String text) {
        Locator loc = mock(Locator.class);
        when(loc.count()).thenReturn(1);
        when(loc.first()).thenReturn(loc);
        when(loc.innerText()).thenReturn(text);
        when(parent.locator(selector)).thenReturn(loc);
    }

    private void configureLocatorAttribute(Locator parent, String selector, String attr, String value) {
        Locator loc = mock(Locator.class);
        when(loc.count()).thenReturn(1);
        when(loc.first()).thenReturn(loc);
        when(loc.getAttribute(attr)).thenReturn(value);
        when(parent.locator(selector)).thenReturn(loc);
    }
}
