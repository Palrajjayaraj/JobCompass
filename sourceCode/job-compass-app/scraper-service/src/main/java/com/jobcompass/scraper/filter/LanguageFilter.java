package com.jobcompass.scraper.filter;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Language detection filter to ensure only English job descriptions are
 * processed.
 * System requirement: All jobs must have English descriptions.
 * 
 * @author Palrajjayaraj
 */
@Component
@Slf4j
public class LanguageFilter {

    private final LanguageDetector detector;

    /**
     * Constructor initializes language detector with English and common European
     * languages.
     */
    public LanguageFilter() {
        this.detector = LanguageDetectorBuilder.fromLanguages(
                Language.ENGLISH,
                Language.GERMAN,
                Language.FRENCH,
                Language.SPANISH,
                Language.ITALIAN,
                Language.DUTCH,
                Language.PORTUGUESE).build();
    }

    /**
     * Check if the given text is in English.
     * System requirement: Only English job descriptions are allowed.
     * 
     * @param text the text to check (job description or title)
     * @return true if text is in English, false otherwise
     */
    public boolean isEnglish(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("Empty text provided for language detection");
            return false;
        }

        // Need at least some content for reliable detection
        if (text.trim().length() < 20) {
            log.debug("Text too short for reliable language detection, defaulting to false");
            return false;
        }

        try {
            Language detectedLanguage = detector.detectLanguageOf(text);
            boolean isEnglish = detectedLanguage == Language.ENGLISH;

            if (!isEnglish) {
                log.info("Non-English content detected: {} (length: {})",
                        detectedLanguage, text.length());
            }

            return isEnglish;
        } catch (Exception e) {
            log.error("Error detecting language", e);
            return false; // Fail safe: reject if we can't determine language
        }
    }

    /**
     * Validate job description is in English.
     * 
     * @param description the job description
     * @return true if description is in English
     */
    public boolean validateJobDescription(String description) {
        return isEnglish(description);
    }
}
