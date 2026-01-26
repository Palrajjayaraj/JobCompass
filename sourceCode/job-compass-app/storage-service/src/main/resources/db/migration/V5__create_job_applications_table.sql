-- Create job_applications table
CREATE TABLE job_applications (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'APPLIED',
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_application_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);

-- Create index on job_id for joins
CREATE INDEX idx_job_application_job_id ON job_applications(job_id);

-- Create index on user_email for user-specific queries
CREATE INDEX idx_job_application_user_email ON job_applications(user_email);

-- Create composite index on user_email and status for filtering
CREATE INDEX idx_job_application_user_status ON job_applications(user_email, status);

-- Create unique constraint to prevent duplicate applications
CREATE UNIQUE INDEX idx_job_application_user_job ON job_applications(user_email, job_id);

-- Add comment to table
COMMENT ON TABLE job_applications IS 'Tracks user job applications and their status';
