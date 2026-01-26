package com.jobcompass.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for Storage Service.
 * Provides data persistence using Spring Data JPA and PostgreSQL.
 * 
 * @author Palrajjayaraj
 */
@SpringBootApplication
@EnableJpaAuditing
public class StorageServiceApplication {

    /**
     * Application entry point.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(StorageServiceApplication.class, args);
    }
}
