package com.jobcompass.common.model;

import com.jobcompass.common.model.authentication.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for ScrapeParameters.
 * 
 * @author Palrajjayaraj
 */
public class ScrapeParametersTest {

    @Test
    public void testDefaults() {
        ScrapeParameters params = ScrapeParameters.defaults();

        assertEquals(Integer.valueOf(7), params.maxJobAgeDays());
        assertEquals(Integer.valueOf(20), params.maxResults());
        assertNull(params.skill());
        assertNull(params.location());
        assertNotNull(params.authentications());
        assertTrue(params.authentications().isEmpty());
    }

    @Test
    public void testOf() {
        ScrapeParameters params = ScrapeParameters.of(14, 50);

        assertEquals(Integer.valueOf(14), params.maxJobAgeDays());
        assertEquals(Integer.valueOf(50), params.maxResults());
        assertNull(params.skill());
        assertNull(params.location());
    }

    @Test
    public void testWithFilters() {
        ScrapeParameters params = ScrapeParameters.withFilters(7, 30, "Java", "Remote");

        assertEquals("Java", params.skill());
        assertEquals("Remote", params.location());
    }

    @Test
    public void testWithAuth() {
        LinkedInAuthentication linkedInAuth = new LinkedInAuthentication("test_token");
        Set<SourceAuthentication> auths = Set.of(linkedInAuth);

        ScrapeParameters params = ScrapeParameters.withAuth(7, 20, null, null, auths);

        assertEquals(1, params.authentications().size());
        assertTrue(params.authentications().contains(linkedInAuth));
    }

    @Test
    public void testGetAuthFor() {
        LinkedInAuthentication linkedInAuth = new LinkedInAuthentication("linkedin_token");
        GlassdoorAuthentication glassdoorAuth = new GlassdoorAuthentication("glassdoor_token");

        Set<SourceAuthentication> auths = Set.of(linkedInAuth, glassdoorAuth);
        ScrapeParameters params = ScrapeParameters.withAuth(7, 20, null, null, auths);

        Optional<SourceAuthentication> linkedInResult = params.getAuthFor(Source.LINKEDIN);
        Optional<SourceAuthentication> glassdoorResult = params.getAuthFor(Source.GLASSDOOR);
        Optional<SourceAuthentication> indeedResult = params.getAuthFor(Source.INDEED);

        assertTrue(linkedInResult.isPresent());
        assertEquals(linkedInAuth, linkedInResult.get());

        assertTrue(glassdoorResult.isPresent());
        assertEquals(glassdoorAuth, glassdoorResult.get());

        assertFalse(indeedResult.isPresent());
    }

    @Test
    public void testGetAuthForNoAuth() {
        ScrapeParameters params = ScrapeParameters.defaults();

        Optional<SourceAuthentication> result = params.getAuthFor(Source.LINKEDIN);
        assertFalse(result.isPresent());
    }

    @Test
    public void testLegacyAuthConversion() {
        Map<String, Map<String, String>> legacyAuth = new HashMap<>();
        Map<String, String> linkedInCreds = Map.of("li_at", "legacy_token");
        legacyAuth.put("LINKEDIN", linkedInCreds);

        ScrapeParameters params = ScrapeParameters.withAuthLegacy(7, 20, null, null, legacyAuth);

        Optional<SourceAuthentication> result = params.getAuthFor(Source.LINKEDIN);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof LinkedInAuthentication);
        assertEquals("legacy_token", ((LinkedInAuthentication) result.get()).liAt());
    }

    @Test
    public void testLegacyAuthConversionMultipleSources() {
        Map<String, Map<String, String>> legacyAuth = new HashMap<>();
        legacyAuth.put("LINKEDIN", Map.of("li_at", "linkedin_token"));
        legacyAuth.put("GLASSDOOR", Map.of("sessionToken", "glassdoor_token"));

        ScrapeParameters params = ScrapeParameters.withAuthLegacy(7, 20, null, null, legacyAuth);

        assertTrue(params.getAuthFor(Source.LINKEDIN).isPresent());
        assertTrue(params.getAuthFor(Source.GLASSDOOR).isPresent());
        assertFalse(params.getAuthFor(Source.INDEED).isPresent());
    }

    @Test
    public void testLegacyAuthWithDisplayName() {
        Map<String, Map<String, String>> legacyAuth = new HashMap<>();
        legacyAuth.put("LinkedIn", Map.of("li_at", "token"));

        ScrapeParameters params = ScrapeParameters.withAuthLegacy(7, 20, null, null, legacyAuth);

        Optional<SourceAuthentication> result = params.getAuthFor(Source.LINKEDIN);
        assertTrue(result.isPresent());
    }

    @Test
    public void testLegacyAuthEmptyCredentials() {
        Map<String, Map<String, String>> legacyAuth = new HashMap<>();
        legacyAuth.put("LINKEDIN", Map.of());

        ScrapeParameters params = ScrapeParameters.withAuthLegacy(7, 20, null, null, legacyAuth);

        Optional<SourceAuthentication> result = params.getAuthFor(Source.LINKEDIN);
        assertFalse(result.isPresent());
    }

    @Test
    public void testLegacyAuthInvalidSource() {
        Map<String, Map<String, String>> legacyAuth = new HashMap<>();
        legacyAuth.put("InvalidSource", Map.of("key", "value"));

        ScrapeParameters params = ScrapeParameters.withAuthLegacy(7, 20, null, null, legacyAuth);

        // Should not throw, just skip invalid sources
        assertNotNull(params);
    }
}
