package com.jobcompass.scraper.scrapers;

import com.jobcompass.common.events.RawJobEvent;
import com.jobcompass.common.model.ScrapeParameters;
import com.jobcompass.common.model.Source;
import com.jobcompass.common.scraper.JobScraper;
import com.jobcompass.scraper.filter.LanguageFilter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * LinkedIn job scraper using Selenium WebDriver.
 * Scrapes publicly available job listings without requiring authentication.
 * 
 * @author Palraj Jayaraj
 */
@Component
public class LinkedInScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(LinkedInScraper.class);
    private static final Source SOURCE = Source.of("LinkedIn");

    private final WebDriver webDriver;
    private final LanguageFilter languageFilter;

    public LinkedInScraper(WebDriver webDriver, LanguageFilter languageFilter) {
        this.webDriver = webDriver;
        this.languageFilter = languageFilter;
    }

    @Override
    public Source getSource() {
        return SOURCE;
    }

    @Override
    public List<RawJobEvent> scrapeJobs(ScrapeParameters parameters) {
        List<RawJobEvent> jobs = new ArrayList<>();

        try {
            String searchUrl = buildSearchUrl(parameters);
            log.info("Scraping LinkedIn jobs from: {}", searchUrl);

            webDriver.get(searchUrl);
            Thread.sleep(3000); // Wait for page load and dynamic content

            // Find all job cards on the page
            List<WebElement> jobCards = webDriver.findElements(
                    By.cssSelector("div.base-card"));

            log.info("Found {} job cards on LinkedIn", jobCards.size());

            int count = 0;
            for (WebElement card : jobCards) {
                if (count >= parameters.maxResults()) {
                    break;
                }

                try {
                    RawJobEvent job = extractJobFromCard(card);
                    if (job != null) {
                        jobs.add(job);
                        count++;

                        // Rate limiting between extractions
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    log.warn("Failed to extract job from card: {}", e.getMessage());
                }
            }

            log.info("Successfully scraped {} jobs from LinkedIn", jobs.size());

        } catch (Exception e) {
            log.error("Error scraping LinkedIn: {}", e.getMessage(), e);
        }

        return jobs;
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
        // Convert days to seconds
        int seconds = params.maxJobAgeDays() * 86400;
        url.append("f_TPR=r").append(seconds);

        return url.toString();
    }

    /**
     * Extract job details from a LinkedIn job card element
     */
    private RawJobEvent extractJobFromCard(WebElement card) {
        try {
            String title = extractText(card, "h3.base-search-card__title");
            String company = extractText(card, "h4.base-search-card__subtitle");
            String location = extractText(card, "span.job-search-card__location");
            String url = card.findElement(By.cssSelector("a.base-card__full-link")).getAttribute("href");

            // Extract job description snippet (if available on card)
            String description = extractText(card, "p.base-search-card__snippet");
            if (description == null || description.trim().isEmpty()) {
                description = extractText(card, "div.base-search-card__info");
            }

            // Posted date might be in different formats
            String postedDate = "Recently"; // Default
            try {
                WebElement timeElement = card.findElement(By.cssSelector("time"));
                postedDate = timeElement.getAttribute("datetime");
                if (postedDate == null || postedDate.isEmpty()) {
                    postedDate = timeElement.getText();
                }
            } catch (Exception e) {
                log.debug("Could not extract posted date for job: {}, error: {}", title, e.getMessage(), e);
            }

            // System requirement: Filter English-only jobs by checking description
            if (description != null && !description.trim().isEmpty()) {
                if (!languageFilter.validateJobDescription(description)) {
                    log.info("Filtered non-English job description for: '{}' at {}", title, company);
                    return null;
                }
            }

            return RawJobEvent.builder()
                    .source(SOURCE)
                    .title(title.trim())
                    .company(company.trim())
                    .location(location.trim())
                    .description(description != null ? description.trim() : "")
                    .url(url)
                    .postedDate(postedDate)
                    .scrapedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.warn("Could not extract all required fields from job card: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Helper method to extract text from element with error handling
     */
    private String extractText(WebElement parent, String cssSelector) {
        try {
            return parent.findElement(By.cssSelector(cssSelector)).getText();
        } catch (Exception e) {
            log.debug("Could not find element with selector '{}': {}", cssSelector, e.getMessage(), e);
            return "";
        }
    }
}
