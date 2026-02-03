package com.jobcompass.common.model;

/**
 * Enum representing supported job sources.
 * Each source has a display name and base URL.
 * 
 * @author Palrajjayaraj
 */
public enum Source {
    LINKEDIN("LinkedIn", "https://www.linkedin.com"),
    GLASSDOOR("Glassdoor", "https://www.glassdoor.com"),
    INDEED("Indeed", "https://www.indeed.com");

    private final String displayName;
    private final String baseUrl;

    /**
     * Constructor for Source enum.
     * 
     * @param displayName user-friendly display name
     * @param baseUrl     base URL of the job source
     */
    Source(String displayName, String baseUrl) {
        this.displayName = displayName;
        this.baseUrl = baseUrl;
    }

    /**
     * Get the display name for UI presentation.
     * 
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the base URL of the job source.
     * 
     * @return base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Create Source from string name (case-insensitive).
     * For backward compatibility with old record-based code.
     * 
     * @param name source name
     * @return matching Source enum
     * @throws IllegalArgumentException if name doesn't match any source
     */
    public static Source fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Source name cannot be null");
        }

        // Try exact match first
        for (Source source : values()) {
            if (source.name().equalsIgnoreCase(name) ||
                    source.displayName.equalsIgnoreCase(name)) {
                return source;
            }
        }

        throw new IllegalArgumentException("Unknown source: " + name);
    }
}
