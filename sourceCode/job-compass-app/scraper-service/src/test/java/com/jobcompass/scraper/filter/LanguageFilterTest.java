package com.jobcompass.scraper.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LanguageFilter.
 * Tests English language detection for job descriptions.
 * 
 * @author Palrajjayaraj
 */
class LanguageFilterTest {

    private LanguageFilter languageFilter;

    @BeforeEach
    void setUp() {
        languageFilter = new LanguageFilter();
    }

    @Test
    void testIsEnglish_EnglishText() {
        String englishText = "We are seeking a skilled Java developer with 5+ years of experience " +
                "in Spring Boot and microservices architecture. Strong knowledge of REST APIs required.";

        assertTrue(languageFilter.isEnglish(englishText),
                "English text should be detected as English");
    }

    @Test
    void testIsEnglish_GermanText() {
        String germanText = "Wir suchen einen erfahrenen Java-Entwickler mit mehr als 5 Jahren " +
                "Erfahrung in Spring Boot und Microservices-Architektur. Fundierte Kenntnisse von REST-APIs erforderlich.";

        assertFalse(languageFilter.isEnglish(germanText),
                "German text should not be detected as English");
    }

    @Test
    void testIsEnglish_FrenchText() {
        String frenchText = "Nous recherchons un développeur Java expérimenté avec plus de 5 ans " +
                "d'expérience dans Spring Boot et l'architecture des microservices.";

        assertFalse(languageFilter.isEnglish(frenchText),
                "French text should not be detected as English");
    }

    @Test
    void testIsEnglish_SpanishText() {
        String spanishText = "Buscamos un desarrollador Java experimentado con más de 5 años de " +
                "experiencia en Spring Boot y arquitectura de microservicios.";

        assertFalse(languageFilter.isEnglish(spanishText),
                "Spanish text should not be detected as English");
    }

    @Test
    void testIsEnglish_NullText() {
        assertFalse(languageFilter.isEnglish(null),
                "Null text should return false");
    }

    @Test
    void testIsEnglish_EmptyText() {
        assertFalse(languageFilter.isEnglish(""),
                "Empty text should return false");
        assertFalse(languageFilter.isEnglish("   "),
                "Whitespace text should return false");
    }

    @Test
    void testIsEnglish_ShortText() {
        String shortText = "Java Developer";

        assertFalse(languageFilter.isEnglish(shortText),
                "Text shorter than 20 characters should return false");
    }

    @Test
    void testValidateJobDescription_ValidEnglish() {
        String jobDescription = "We are looking for a Senior Software Engineer to join our team. " +
                "You will be responsible for developing scalable backend systems using Java and Spring.";

        assertTrue(languageFilter.validateJobDescription(jobDescription),
                "Valid English job description should pass validation");
    }

    @Test
    void testValidateJobDescription_German() {
        String jobDescription = "Wir suchen einen Senior-Softwareentwickler für unser Team. " +
                "Sie werden für die Entwicklung skalierbarer Backend-Systeme mit Java und Spring verantwortlich sein.";

        assertFalse(languageFilter.validateJobDescription(jobDescription),
                "German job description should fail validation");
    }

    @Test
    void testIsEnglish_TechnicalTerms() {
        String technicalText = "Experience with Kubernetes, Docker, CI/CD pipelines, " +
                "and cloud platforms like AWS or Azure is essential for this role.";

        assertTrue(languageFilter.isEnglish(technicalText),
                "English text with technical terms should be detected as English");
    }

    @Test
    void testIsEnglish_DutchText() {
        String dutchText = "We zijn op zoek naar een ervaren Java-ontwikkelaar met meer dan " +
                "5 jaar ervaring in Spring Boot en microservices architectuur.";

        assertFalse(languageFilter.isEnglish(dutchText),
                "Dutch text should not be detected as English");
    }
}
