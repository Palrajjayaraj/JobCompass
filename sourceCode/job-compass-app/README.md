# Job Compass

A microservices-based job aggregation platform built with Spring Boot and Kafka.

## Overview

Job Compass scrapes job listings from multiple sources (LinkedIn, Glassdoor, Indeed) using Selenium WebDriver, processes and normalizes the data, and provides a REST API for searching jobs.

## Architecture

- **Scheduler Service**: Triggers daily job scraping via Kafka
- **Scraper Service**: Web automation with Selenium to fetch job listings
- **Processor Service**: Normalizes and deduplicates job data
- **Storage Service**: Persists jobs to PostgreSQL
- **API Gateway**: REST API for job search and retrieval
- **Common**: Shared DTOs and utilities

## Technology Stack

- Java 23
- Spring Boot 3.4.1
- Apache Kafka 3.8+
- PostgreSQL 17
- Selenium WebDriver 4.x
- Docker & Docker Compose
- Maven 3.9+

## Prerequisites

- Java 23 or higher
- Docker & Docker Compose
- Maven 3.9+

## Getting Started

### 1. Start Infrastructure (Kafka, PostgreSQL, Chrome)
```bash
docker-compose up -d
```

### 2. Build All Services
```bash
mvn clean install
```

### 3. Run Services
Each service can be run individually or all together via Docker Compose.

### 4. Access API
```bash
# Get all recent jobs
curl http://localhost:8080/api/jobs

# Filter by max age (e.g., last 5 days)
curl "http://localhost:8080/api/jobs?maxJobAge=5"
```

## Configuration

Job age filtering can be customized in each service's `application.yml`:
- `jobcompass.scraper.max-job-age-days`: Maximum age of jobs to scrape (default: 7)
- `jobcompass.processor.max-job-age-days`: Validation threshold (default: 7)

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Generate coverage report
mvn test jacoco:report
```

## Project Structure

```
job-compass-app/
├── pom.xml                 # Parent POM
├── common/                 # Shared DTOs and events
├── scheduler-service/      # Daily scheduling service
├── scraper-service/        # Web scraping with Selenium
├── processor-service/      # Data normalization
├── storage-service/        # PostgreSQL persistence
├── api-gateway/            # REST API
└── docker-compose.yml      # Infrastructure setup
```

## License

Educational project for learning Spring Boot microservices and Kafka.
