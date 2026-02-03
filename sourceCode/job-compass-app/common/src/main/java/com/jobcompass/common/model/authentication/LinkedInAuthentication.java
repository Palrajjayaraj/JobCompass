package com.jobcompass.common.model.authentication;

import com.jobcompass.common.model.Source;

/**
 * LinkedIn-specific authentication credentials.
 * Requires the li_at session cookie.
 * 
 * @author Palrajjayaraj
 */
public record LinkedInAuthentication(String liAt) implements SourceAuthentication {

    /**
     * Validate that li_at is not null or empty.
     */
    public LinkedInAuthentication {
        if (liAt == null || liAt.trim().isEmpty()) {
            throw new IllegalArgumentException("LinkedIn li_at cookie cannot be null or empty");
        }
    }

    @Override
    public Source getSource() {
        return Source.LINKEDIN;
    }
}
