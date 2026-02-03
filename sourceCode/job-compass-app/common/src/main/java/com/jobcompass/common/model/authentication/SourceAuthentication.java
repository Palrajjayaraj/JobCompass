package com.jobcompass.common.model.authentication;

import com.jobcompass.common.model.Source;

/**
 * Sealed interface for source-specific authentication credentials.
 * Ensures type-safe authentication handling per job source.
 * 
 * @author Palrajjayaraj
 */
public sealed interface SourceAuthentication
        permits LinkedInAuthentication, GlassdoorAuthentication, IndeedAuthentication {

    /**
     * Get the source this authentication is for.
     * 
     * @return the job source
     */
    Source getSource();
}
