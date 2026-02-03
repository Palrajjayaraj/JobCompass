package com.jobcompass.common.model.authentication;

import com.jobcompass.common.model.Source;

/**
 * Glassdoor-specific authentication credentials.
 * 
 * @author Palrajjayaraj
 */
public record GlassdoorAuthentication(String sessionToken) implements SourceAuthentication {

    /**
     * Validate that session token is not null or empty.
     */
    public GlassdoorAuthentication {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Glassdoor session token cannot be null or empty");
        }
    }

    @Override
    public Source getSource() {
        return Source.GLASSDOOR;
    }
}
