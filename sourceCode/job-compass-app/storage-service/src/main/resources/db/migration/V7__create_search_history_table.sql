-- Search History table to persist recent skill/location searches across sessions
CREATE TABLE search_history (
    id          BIGSERIAL PRIMARY KEY,
    skill       VARCHAR(255),
    location    VARCHAR(255),
    searched_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_search_history_searched_at ON search_history(searched_at DESC);
