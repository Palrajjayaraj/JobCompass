package com.jobcompass.scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Scraper Service.
 * This service scrapes job listings from various sources using Selenium
 * WebDriver
 * and publishes raw job data to Kafka.
 * 
 * @author Palraj Jayaraj
 */
@SpringBootApplication
@EnableKafka
@EnableScheduling
public class ScraperServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScraperServiceApplication.class, args);
    }
}
