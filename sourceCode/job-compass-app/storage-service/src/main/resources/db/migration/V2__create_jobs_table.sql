-- Create jobs table
CREATE TABLE jobs (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(255),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    salary_range VARCHAR(100),
    url VARCHAR(1000) NOT NULL UNIQUE,
    posted_date DATE,
    job_age_days INTEGER,
    source VARCHAR(50) NOT NULL,
    scraped_at TIMESTAMP NOT NULL,
    company_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE SET NULL
);

-- Create unique index on external_id (can be null for some sources)
CREATE UNIQUE INDEX idx_job_external_id ON jobs(external_id) WHERE external_id IS NOT NULL;

-- Create unique index on url for duplicate detection
CREATE UNIQUE INDEX idx_job_url ON jobs(url);

-- Create index on source for filtering by job source
CREATE INDEX idx_job_source ON jobs(source);

-- Create index on posted_date for date range queries
CREATE INDEX idx_job_posted_date ON jobs(posted_date);

-- Create index on company_id for joins
CREATE INDEX idx_job_company_id ON jobs(company_id);

-- Create composite index on source and external_id for upsert operations
CREATE INDEX idx_job_source_external ON jobs(source, external_id);

-- Create index on is_active for filtering active jobs
CREATE INDEX idx_job_is_active ON jobs(is_active);

-- Create index on location for location-based searches
CREATE INDEX idx_job_location ON jobs(location);

-- Add comment to table
COMMENT ON TABLE jobs IS 'Stores job postings scraped from various sources';
