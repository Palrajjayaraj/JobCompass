package com.jobcompass.common.model;

import com.jobcompass.common.model.authentication.*;

import java.util.*;

/**
 * Parameters for job scraping operations.
 * Using record for immutability and easy extensibility.
 * 
 * @author Palrajjayaraj
 */
public record ScrapeParameters(
        Integer maxJobAgeDays, // Only scrape jobs posted within this many days
        Integer maxResults, // Maximum number of jobs to scrape per source
        String skill, // Optional: Filter by skill (e.g., "Java", "Python")
        String location, // Optional: Filter by location (e.g., "San Francisco", "Remote")
        Set<SourceAuthentication> authentications, // Type-safe authentication per source
        @Deprecated java.util.Map<String, java.util.Map<String, String>> authentication // Legacy support
) {
    /**
     * Create default scraping parameters (7 days, 20 results, no filters).
     */
    public static ScrapeParameters defaults() {
        return new ScrapeParameters(7, 20, null, null, Set.of(), null);
    }

    /**
     * Create parameters with just age and results limit.
     */
    public static ScrapeParameters of(int maxJobAgeDays, int maxResults) {
        return new ScrapeParameters(maxJobAgeDays, maxResults, null, null, Set.of(), null);
    }

    /**
     * Create parameters with all filters.
     */
    public static ScrapeParameters withFilters(int maxJobAgeDays, int maxResults, String skill, String location) {
        return new ScrapeParameters(maxJobAgeDays, maxResults, skill, location, Set.of(), null);
    }

    /**
     * Create parameters with new type-safe authentication.
     */
    public static ScrapeParameters withAuth(int maxJobAgeDays, int maxResults, String skill, String location,
            Set<SourceAuthentication> authentications) {
        return new ScrapeParameters(maxJobAgeDays, maxResults, skill, location, authentications, null);
    }

    /**
     * Legacy method for backward compatibility.
     * 
     * @deprecated Use {@link #withAuth(int, int, String, String, Set)} instead
     */
    @Deprecated
    public static ScrapeParameters withAuthLegacy(int maxJobAgeDays, int maxResults, String skill, String location,
            java.util.Map<String, java.util.Map<String, String>> authentication) {
        // Convert legacy Map to new SourceAuthentication set
        Set<SourceAuthentication> auths = convertLegacyAuth(authentication);
        return new ScrapeParameters(maxJobAgeDays, maxResults, skill, location, auths, authentication);
    }

    /**
     * Get authentication for a specific source.
     * Supports both new and legacy authentication formats.
     * 
     * @param source the job source
     * @return Optional containing the authentication, or empty if not found
     */
    public Optional<SourceAuthentication> getAuthFor(Source source) {
        // First try new authentications
        if (authentications != null && !authentications.isEmpty()) {
            return authentications.stream()
                    .filter(auth -> auth.getSource() == source)
                    .findFirst();
        }

        // Fall back to legacy authentication if present
        if (authentication != null && !authentication.isEmpty()) {
            return convertLegacyAuthForSource(source);
        }

        return Optional.empty();
    }

    /**
     * Convert legacy Map-based authentication to new SourceAuthentication set.
     */
    private static Set<SourceAuthentication> convertLegacyAuth(
            java.util.Map<String, java.util.Map<String, String>> legacyAuth) {
        if (legacyAuth == null || legacyAuth.isEmpty()) {
            return Set.of();
        }

        Set<SourceAuthentication> result = new HashSet<>();

        for (Map.Entry<String, Map<String, String>> entry : legacyAuth.entrySet()) {
            String sourceName = entry.getKey();
            Map<String, String> credentials = entry.getValue();

            try {
                Source source = Source.fromString(sourceName);
                SourceAuthentication auth = createAuthFromLegacy(source, credentials);
                if (auth != null) {
                    result.add(auth);
                }
            } catch (IllegalArgumentException e) {
                // Skip unknown sources
            }
        }

        return result;
    }

    /**
     * Convert legacy authentication for a specific source.
     */
    private Optional<SourceAuthentication> convertLegacyAuthForSource(Source source) {
        if (authentication == null) {
            return Optional.empty();
        }

        Map<String, String> credentials = authentication.get(source.name());
        if (credentials == null) {
            credentials = authentication.get(source.getDisplayName());
        }

        if (credentials == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(createAuthFromLegacy(source, credentials));
    }

    /**
     * Create SourceAuthentication from legacy credential map.
     */
    private static SourceAuthentication createAuthFromLegacy(Source source, Map<String, String> credentials) {
        if (credentials == null || credentials.isEmpty()) {
            return null;
        }

        return switch (source) {
            case LINKEDIN -> {
                String liAt = credentials.get("li_at");
                yield liAt != null && !liAt.isEmpty() ? new LinkedInAuthentication(liAt) : null;
            }
            case GLASSDOOR -> {
                String token = credentials.get("sessionToken");
                yield token != null && !token.isEmpty() ? new GlassdoorAuthentication(token) : null;
            }
            case INDEED -> {
                String apiKey = credentials.get("apiKey");
                yield apiKey != null && !apiKey.isEmpty() ? new IndeedAuthentication(apiKey) : null;
            }
        };
    }
}
