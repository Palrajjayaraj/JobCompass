package com.jobcompass.scraper.scrapers;

import com.jobcompass.common.events.RawJobEvent;
import com.jobcompass.common.model.ScrapeParameters;
import com.jobcompass.common.model.Source;
import com.jobcompass.common.scraper.JobScraper;
import com.jobcompass.scraper.config.SeleniumProperties;
import com.jobcompass.scraper.filter.LanguageFilter;
import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * LinkedIn job scraper using Microsoft Playwright.
 * Replaces legacy Selenium implementation for better bot evasion and
 * performance.
 *
 * @author Palraj Jayaraj
 */
@Component
public class LinkedInScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(LinkedInScraper.class);
    private static final Source SOURCE = Source.of("LinkedIn");

    private final Browser browser;
    private final SeleniumProperties properties; // Reusing props for user agents
    private final LanguageFilter languageFilter;

    public LinkedInScraper(Browser browser, SeleniumProperties properties, LanguageFilter languageFilter) {
        this.browser = browser;
        this.properties = properties;
        this.languageFilter = languageFilter;
    }

    @Override
    public Source getSource() {
        return SOURCE;
    }

    @Override
    public List<RawJobEvent> scrapeJobs(ScrapeParameters parameters) {
        List<RawJobEvent> jobs = new ArrayList<>();

        if (properties.getUserAgents() == null || properties.getUserAgents().isEmpty()) {
            log.warn("No User-Agents configured, using default");
        }

        String userAgent = (properties.getUserAgents() != null && !properties.getUserAgents().isEmpty())
                ? properties.getUserAgents().get(new Random().nextInt(properties.getUserAgents().size()))
                : "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setUserAgent(userAgent)
                .setViewportSize(1920, 1080)
                .setJavaScriptEnabled(true);

        log.info("Starting scrape with User-Agent: {}", userAgent);

        try (BrowserContext context = browser.newContext(contextOptions)) {

            // Inject authentication cookie if provided
            if (parameters.authCookie() != null && !parameters.authCookie().isEmpty()) {
                log.info("Injecting authentication cookie for LinkedIn");
                context.addCookies(List.of(
                        new com.microsoft.playwright.options.Cookie("li_at", parameters.authCookie())
                                .setDomain(".www.linkedin.com")
                                .setPath("/")
                                .setSecure(true)));
            }

            Page page = context.newPage();

            String searchUrl = buildSearchUrl(parameters);
            log.info("Scraping LinkedIn jobs from: {}", searchUrl);

            // Navigate and wait for content
            page.navigate(searchUrl, new Page.NavigateOptions().setTimeout(60000));

            // Wait for job cards to appear - robust wait
            try {
                // Try multiple selectors as LinkedIn changes them frequently
                page.waitForSelector("div.base-card, ul.jobs-search__results-list li",
                        new Page.WaitForSelectorOptions().setTimeout(15000));
            } catch (Exception e) {
                log.warn("Timeout waiting for job cards. Page might be empty, blocked, or slow.");
            }

            // Scroll to load lazy content
            scrollToBottom(page);

            // Find all job cards using Playwright Locator
            Locator jobCards = page.locator("div.base-card");
            int cardCount = jobCards.count();

            if (cardCount == 0) {
                // Fallback selector for some LinkedIn views (e.g. guest view list)
                jobCards = page.locator("ul.jobs-search__results-list li");
                cardCount = jobCards.count();
            }

            log.info("Found {} job cards on LinkedIn", cardCount);

            int count = 0;
            for (int i = 0; i < cardCount; i++) {
                if (count >= parameters.maxResults()) {
                    break;
                }

                try {
                    // Get handle to the nth card
                    Locator card = jobCards.nth(i);
                    RawJobEvent job = extractJobFromCard(card);
                    if (job != null) {
                        jobs.add(job);
                        count++;
                    }
                } catch (Exception e) {
                    log.warn("Failed to extract job from card index {}: {}", i, e.getMessage());
                }
            }

            log.info("Successfully scraped {} jobs from LinkedIn via Playwright", jobs.size());

        } catch (Exception e) {
            log.error("Error scraping LinkedIn with Playwright: {}", e.getMessage(), e);
        }

        return jobs;
    }

    private void scrollToBottom(Page page) {
        try {
            // Scroll down a bit to trigger lazy loading
            for (int i = 0; i < 5; i++) {
                page.evaluate("window.scrollBy(0, window.innerHeight)");
                page.waitForTimeout(1000);
            }
        } catch (Exception e) {
            log.debug("Error during scroll: {}", e.getMessage());
        }
    }

    /**
     * Build LinkedIn search URL with parameters
     */
    private String buildSearchUrl(ScrapeParameters params) {
        StringBuilder url = new StringBuilder("https://www.linkedin.com/jobs/search/?");

        if (params.skill() != null && !params.skill().isEmpty()) {
            url.append("keywords=").append(params.skill().replace(" ", "%20")).append("&");
        }

        if (params.location() != null && !params.location().isEmpty()) {
            url.append("location=").append(params.location().replace(" ", "%20")).append("&");
        }

        // Time filter: f_TPR=r{seconds}
        int seconds = params.maxJobAgeDays() * 86400;
        url.append("f_TPR=r").append(seconds);

        return url.toString();
    }

    /**
     * Extract job details from a LinkedIn job card locator
     */
    private RawJobEvent extractJobFromCard(Locator card) {
        try {
            // Use .first() to handle cases where multiple elements match (though usually
            // one per card)
            String title = card.locator("h3.base-search-card__title").first().innerText().trim();
            String company = card.locator("h4.base-search-card__subtitle").first().innerText().trim();
            String location = card.locator("span.job-search-card__location").first().innerText().trim();
            String url = card.locator("a.base-card__full-link").first().getAttribute("href");

            // Extract snippet
            String description = "";
            if (card.locator("p.base-search-card__snippet").count() > 0
                    && card.locator("p.base-search-card__snippet").first().isVisible()) {
                description = card.locator("p.base-search-card__snippet").first().innerText().trim();
            } else if (card.locator("div.base-search-card__info").count() > 0
                    && card.locator("div.base-search-card__info").first().isVisible()) {
                description = card.locator("div.base-search-card__info").first().innerText().trim();
            }

            // Posted date
            String postedDate = "Recently";
            Locator timeParams = card.locator("time");
            if (timeParams.count() > 0) {
                postedDate = timeParams.first().getAttribute("datetime");
                if (postedDate == null) {
                    postedDate = timeParams.first().innerText().trim();
                }
            }

            // Language filtering
            if (!description.isEmpty()) {
                if (!languageFilter.validateJobDescription(description)) {
                    log.info("Filtered non-English job description for: '{}'", title);
                    return null;
                }
            }

            return RawJobEvent.builder()
                    .source(SOURCE)
                    .title(title)
                    .company(company)
                    .location(location)
                    .description(description)
                    .url(url)
                    .postedDate(postedDate)
                    .scrapedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            // log.debug("Could not parse card: {}", e.getMessage());
            return null;
        }
    }
}
