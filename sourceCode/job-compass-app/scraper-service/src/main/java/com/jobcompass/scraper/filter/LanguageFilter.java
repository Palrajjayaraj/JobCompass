package com.jobcompass.scraper.filter;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Language detection service to identify the language of job descriptions.
 * Returns ISO 639-1 language codes (en, de, fr, es, etc.) instead of filtering.
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
     * Detect the language of the given text and return ISO 639-1 code.
     * 
     * @param text the text to analyze (job description or title)
     * @return language code ("en", "de", "fr", etc.) or "unknown" if detection
     *         fails
     */
    public String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("Empty text provided for language detection");
            return "unknown";
        }

        // Need at least some content for reliable detection
        if (text.trim().length() < 20) {
            log.debug("Text too short for reliable language detection ({}), returning unknown", text.trim().length());
            return "unknown";
        }

        try {
            Language detectedLanguage = detector.detectLanguageOf(text);
            String languageCode = mapToLanguageCode(detectedLanguage);

            log.debug("Detected language: {} (code: {}) for text length: {}",
                    detectedLanguage, languageCode, text.length());

            return languageCode;
        } catch (Exception e) {
            log.error("Error detecting language", e);
            return "unknown";
        }
    }

    /**
     * Map Lingua Language enum to ISO 639-1 language codes.
     * 
     * @param language the detected language
     * @return ISO 639-1 code (en, de, fr, etc.)
     */
    private String mapToLanguageCode(Language language) {
        if (language == null) {
            return "unknown";
        }

        switch (language) {
            case ENGLISH:
                return "en";
            case GERMAN:
                return "de";
            case FRENCH:
                return "fr";
            case SPANISH:
                return "es";
            case ITALIAN:
                return "it";
            case DUTCH:
                return "nl";
            case PORTUGUESE:
                return "pt";
            default:
                return "unknown";
        }
    }

    /**
     * Check if the given text is in English.
     * Kept for backward compatibility with existing code/tests.
     * 
     * @param text the text to check (job description or title)
     * @return true if text is in English, false otherwise
     */
    public boolean isEnglish(String text) {
        String languageCode = detectLanguage(text);
        return "en".equals(languageCode);
    }

    /**
     * Validate job description is in English.
     * Kept for backward compatibility.
     * 
     * @param description the job description
     * @return true if description is in English
     */
    public boolean validateJobDescription(String description) {
        return isEnglish(description);
    }
}
