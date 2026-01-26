-- Create companies table
CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    industry VARCHAR(100),
    website VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on company name for faster lookups
CREATE INDEX idx_company_name ON companies(name);

-- Create index on industry for filtering
CREATE INDEX idx_company_industry ON companies(industry);

-- Add comment to table
COMMENT ON TABLE companies IS 'Stores company information for job postings';
