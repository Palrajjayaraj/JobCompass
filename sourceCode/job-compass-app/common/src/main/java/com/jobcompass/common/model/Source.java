package com.jobcompass.common.model;

/**
 * Represents a job source (LinkedIn, Glassdoor, Indeed, etc.)
 * Using Java record for immutability and conciseness.
 */
public record Source(String name) {

    /**
     * Factory method for convenience
     */
    public static Source of(String name) {
        return new Source(name);
    }
}
