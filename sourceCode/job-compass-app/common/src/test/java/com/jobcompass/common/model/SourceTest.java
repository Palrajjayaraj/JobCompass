package com.jobcompass.common.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Source enum.
 * 
 * @author Palrajjayaraj
 */
public class SourceTest {

    @Test
    public void testEnumValues() {
        assertEquals(3, Source.values().length);
        assertEquals(Source.LINKEDIN, Source.valueOf("LINKEDIN"));
        assertEquals(Source.GLASSDOOR, Source.valueOf("GLASSDOOR"));
        assertEquals(Source.INDEED, Source.valueOf("INDEED"));
    }

    @Test
    public void testDisplayNames() {
        assertEquals("LinkedIn", Source.LINKEDIN.getDisplayName());
        assertEquals("Glassdoor", Source.GLASSDOOR.getDisplayName());
        assertEquals("Indeed", Source.INDEED.getDisplayName());
    }

    @Test
    public void testBaseUrls() {
        assertEquals("https://www.linkedin.com", Source.LINKEDIN.getBaseUrl());
        assertEquals("https://www.glassdoor.com", Source.GLASSDOOR.getBaseUrl());
        assertEquals("https://www.indeed.com", Source.INDEED.getBaseUrl());
    }

    @Test
    public void testFromStringExactMatch() {
        assertEquals(Source.LINKEDIN, Source.fromString("LINKEDIN"));
        assertEquals(Source.GLASSDOOR, Source.fromString("GLASSDOOR"));
        assertEquals(Source.INDEED, Source.fromString("INDEED"));
    }

    @Test
    public void testFromStringCaseInsensitive() {
        assertEquals(Source.LINKEDIN, Source.fromString("linkedin"));
        assertEquals(Source.LINKEDIN, Source.fromString("LiNkEdIn"));
        assertEquals(Source.GLASSDOOR, Source.fromString("glassdoor"));
    }

    @Test
    public void testFromStringDisplayName() {
        assertEquals(Source.LINKEDIN, Source.fromString("LinkedIn"));
        assertEquals(Source.GLASSDOOR, Source.fromString("Glassdoor"));
        assertEquals(Source.INDEED, Source.fromString("Indeed"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringNull() {
        Source.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringInvalid() {
        Source.fromString("InvalidSource");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringEmpty() {
        Source.fromString("");
    }
}
