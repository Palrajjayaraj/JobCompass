package com.jobcompass.common.model.authentication;

import com.jobcompass.common.model.Source;

/**
 * Indeed-specific authentication credentials.
 * 
 * @author Palrajjayaraj
 */
public record IndeedAuthentication(String apiKey) implements SourceAuthentication {

    /**
     * Validate that API key is not null or empty.
     */
    public IndeedAuthentication {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Indeed API key cannot be null or empty");
        }
    }

    @Override
    public Source getSource() {
        return Source.INDEED;
    }
}
