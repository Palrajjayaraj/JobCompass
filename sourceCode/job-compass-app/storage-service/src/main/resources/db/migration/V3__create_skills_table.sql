-- Create skills table
CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create unique index on skill name
CREATE UNIQUE INDEX idx_skill_name ON skills(name);

-- Create index on category for filtering
CREATE INDEX idx_skill_category ON skills(category);

-- Add comment to table
COMMENT ON TABLE skills IS 'Stores skills and technologies associated with jobs';
