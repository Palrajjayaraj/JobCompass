-- Create job_skills join table (many-to-many relationship)
CREATE TABLE job_skills (
    job_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (job_id, skill_id),
    CONSTRAINT fk_job_skills_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_job_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

-- Create index on skill_id for reverse lookups
CREATE INDEX idx_job_skills_skill_id ON job_skills(skill_id);

-- Create index on job_id for lookups
CREATE INDEX idx_job_skills_job_id ON job_skills(job_id);

-- Add comment to table
COMMENT ON TABLE job_skills IS 'Join table for many-to-many relationship between jobs and skills';
