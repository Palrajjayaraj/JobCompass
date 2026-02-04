ALTER TABLE jobs ADD COLUMN language VARCHAR(10);
CREATE INDEX idx_job_language ON jobs(language);
