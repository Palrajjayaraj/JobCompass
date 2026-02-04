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
    private static final Source SOURCE = Source.LINKEDIN;

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
            parameters.getAuthFor(Source.LINKEDIN)
                    .filter(auth -> auth instanceof com.jobcompass.common.model.authentication.LinkedInAuthentication)
                    .map(auth -> (com.jobcompass.common.model.authentication.LinkedInAuthentication) auth)
                    .ifPresent(auth -> {
                        log.info("Injecting authentication cookie for LinkedIn");
                        context.addCookies(List.of(
                                new com.microsoft.playwright.options.Cookie("li_at", auth.liAt())
                                        .setDomain(".www.linkedin.com")
                                        .setPath("/")
                                        .setSecure(true)));
                    });

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
                    // Pass context to open new tabs for details
                    RawJobEvent job = extractJobFromCard(context, page, card);
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
     * Fetch full job description by opening a NEW page/tab.
     * This prevents navigating the main list page away.
     */
    private String fetchFullDescription(BrowserContext context, String jobUrl) {
        Page detailPage = null;
        try {
            detailPage = context.newPage();

            // Navigate to job details page
            detailPage.navigate(jobUrl, new Page.NavigateOptions().setTimeout(30000));

            // Wait for description to load
            detailPage.waitForSelector("div.description__text, div.show-more-less-html__markup",
                    new Page.WaitForSelectorOptions().setTimeout(10000));

            // Try multiple selectors for job description
            Locator descLocator = detailPage.locator("div.description__text").first();
            if (descLocator.count() == 0) {
                descLocator = detailPage.locator("div.show-more-less-html__markup").first();
            }
            if (descLocator.count() == 0) {
                descLocator = detailPage.locator("div.jobs-description").first();
            }

            if (descLocator.count() > 0) {
                return descLocator.innerText().trim();
            }

            log.debug("Could not find job description on page: {}", jobUrl);
            return "";

        } catch (Exception e) {
            log.warn("Failed to fetch full description from {}: {}", jobUrl, e.getMessage());
            return "";
        } finally {
            if (detailPage != null) {
                try {
                    detailPage.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Extract job details from a LinkedIn job card locator
     */
    private RawJobEvent extractJobFromCard(BrowserContext context, Page page, Locator card) {
        try {
            // Title
            String title = extractText(card, "h3.base-search-card__title", "a.base-search-card__full-link",
                    ".job-search-card__title");
            if (title.isEmpty()) {
                log.warn("Could not extract title from card");
                return null;
            }

            // Company
            String company = extractText(card, "h4.base-search-card__subtitle", "a.hidden-nested-link",
                    ".job-search-card__subtitle", ".base-search-card__subtitle");
            if (company.isEmpty()) {
                log.warn("Could not extract company for job: {}", title);
                // company = "Unknown Company"; // Let's keep it empty to let fallback verify?
                // No, better to extract correctly.
            }

            // Location
            String location = extractText(card, "span.job-search-card__location", ".job-search-card__location");

            // URL
            String url = extractAttribute(card, "href", "a.base-search-card__full-link", "a.base-card__full-link", "a");
            if (url.isEmpty()) {
                log.warn("Could not extract URL for job: {}", title);
                return null;
            }

            // Fetch FULL description by opening new page
            String description = fetchFullDescription(context, url);

            // If full description fetch fails, fall back to snippet from the LIST page
            if (description.isEmpty()) {
                if (card.locator("p.base-search-card__snippet").count() > 0
                        && card.locator("p.base-search-card__snippet").first().isVisible()) {
                    description = card.locator("p.base-search-card__snippet").first().innerText().trim();
                } else if (card.locator("div.base-search-card__info").count() > 0
                        && card.locator("div.base-search-card__info").first().isVisible()) {
                    description = card.locator("div.base-search-card__info").first().innerText().trim();
                }
            }

            // Posted date
            String postedDate = "Recently";
            String dateAttr = extractAttribute(card, "datetime", "time");
            if (!dateAttr.isEmpty()) {
                postedDate = dateAttr;
            } else {
                String dateText = extractText(card, "time");
                if (!dateText.isEmpty()) {
                    postedDate = dateText;
                }
            }

            // Language detection on FULL description
            String detectedLanguage = "unknown";
            if (!description.isEmpty()) {
                detectedLanguage = languageFilter.detectLanguage(description);
                log.debug("Detected language '{}' for job: '{}' at {}", detectedLanguage, title, company);
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
                    .language(detectedLanguage)
                    .build();

        } catch (Exception e) {
            log.warn("Could not parse card: {}", e.getMessage());
            return null;
        }
    }

    private String extractText(Locator card, String... selectors) {
        for (String selector : selectors) {
            try {
                Locator loc = card.locator(selector).first();
                if (loc.count() > 0) {
                    String text = loc.innerText().trim();
                    if (!text.isEmpty()) {
                        return text;
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return "";
    }

    private String extractAttribute(Locator card, String attribute, String... selectors) {
        for (String selector : selectors) {
            try {
                Locator loc = card.locator(selector).first();
                if (loc.count() > 0) {
                    String val = loc.getAttribute(attribute);
                    if (val != null && !val.isEmpty()) {
                        return val;
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return "";
    }
}
