package com.jobcompass.scraper.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Random;

/**
 * Selenium WebDriver configuration for headless Chrome.
 * 
 * @author Palraj Jayaraj
 */
@Configuration
public class SeleniumConfig {

    private final SeleniumProperties properties;

    public SeleniumConfig(SeleniumProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WebDriver webDriver() {
        // Use system-installed chromedriver (installed via apt in Dockerfile)
        // No need for WebDriverManager.chromedriver().setup() as we have
        // chromium-chromedriver package

        ChromeOptions options = new ChromeOptions();

        // Point to system chromium binary (not Chrome)
        options.setBinary("/usr/bin/chromium-browser");

        if (properties.isHeadless()) {
            options.addArguments("--headless=new");
        }

        // Randomly select a user-agent from the pool for rotation
        String selectedUserAgent = properties.getUserAgents().get(
                new Random().nextInt(properties.getUserAgents().size()));

        // Common options for better scraping
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=" + selectedUserAgent);
        options.addArguments("--window-size=" + properties.getWindowWidth() + "," + properties.getWindowHeight());
        options.addArguments("--disable-gpu");

        // Disable image loading for faster scraping
        options.addArguments("--blink-settings=imagesEnabled=false");

        // System property for chromedriver location (automatically found in /usr/bin
        // via chromium-chromedriver package)
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");

        ChromeDriver driver = new ChromeDriver(options);

        // Set timeouts
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(properties.getImplicitWaitSeconds()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(properties.getPageLoadTimeoutSeconds()));

        return driver;
    }
}
