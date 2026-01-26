package com.jobcompass.scraper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for Selenium WebDriver.
 * 
 * @author Palraj Jayaraj
 */
@Component
@ConfigurationProperties(prefix = "selenium")
public class SeleniumProperties {

    private boolean headless = true;
    private List<String> userAgents;
    private int windowWidth = 1920;
    private int windowHeight = 1080;
    private int implicitWaitSeconds = 10;
    private int pageLoadTimeoutSeconds = 30;

    // Getters and setters
    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public List<String> getUserAgents() {
        return userAgents;
    }

    public void setUserAgents(List<String> userAgents) {
        this.userAgents = userAgents;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public int getImplicitWaitSeconds() {
        return implicitWaitSeconds;
    }

    public void setImplicitWaitSeconds(int implicitWaitSeconds) {
        this.implicitWaitSeconds = implicitWaitSeconds;
    }

    public int getPageLoadTimeoutSeconds() {
        return pageLoadTimeoutSeconds;
    }

    public void setPageLoadTimeoutSeconds(int pageLoadTimeoutSeconds) {
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }
}
