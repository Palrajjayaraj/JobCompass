package com.jobcompass.scraper.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

/**
 * Configuration for Microsoft Playwright.
 * Manages the lifecycle of the Playwright instance and Browser.
 */
@Configuration
public class PlaywrightConfig {

    private Playwright playwright;
    private Browser browser;

    @Bean
    public Playwright playwright() {
        if (playwright == null) {
            playwright = Playwright.create();
        }
        return playwright;
    }

    @Bean
    public Browser browser(Playwright playwright) {
        if (browser == null) {
            // Launch Chromium in headless mode (default)
            // Can be configured via properties if needed
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true));
        }
        return browser;
    }

    @PreDestroy
    public void close() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
