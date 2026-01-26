package com.jobcompass.storage.entity.enums;

/**
 * Enumeration of job application statuses.
 * Tracks the current state of a user's job application.
 * 
 * @author Palrajjayaraj
 */
public enum ApplicationStatus {
    /**
     * Application has been submitted
     */
    APPLIED,
    
    /**
     * Application is under review
     */
    UNDER_REVIEW,
    
    /**
     * Candidate is in the interview process
     */
    INTERVIEWING,
    
    /**
     * Job offer has been extended
     */
    OFFER,
    
    /**
     * Application was rejected
     */
    REJECTED,
    
    /**
     * Candidate withdrew application
     */
    WITHDRAWN,
    
    /**
     * Candidate accepted the offer
     */
    ACCEPTED
}
