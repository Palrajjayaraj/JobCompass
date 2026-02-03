package com.jobcompass.common.model.authentication;

import com.jobcompass.common.model.Source;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for LinkedInAuthentication.
 * 
 * @author Palrajjayaraj
 */
public class LinkedInAuthenticationTest {

    @Test
    public void testValidAuthentication() {
        String liAt = "valid_li_at_token";
        LinkedInAuthentication auth = new LinkedInAuthentication(liAt);

        assertEquals(liAt, auth.liAt());
        assertEquals(Source.LINKEDIN, auth.getSource());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullLiAt() {
        new LinkedInAuthentication(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyLiAt() {
        new LinkedInAuthentication("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhitespaceLiAt() {
        new LinkedInAuthentication("   ");
    }

    @Test
    public void testRecordEquality() {
        LinkedInAuthentication auth1 = new LinkedInAuthentication("token123");
        LinkedInAuthentication auth2 = new LinkedInAuthentication("token123");
        LinkedInAuthentication auth3 = new LinkedInAuthentication("different");

        assertEquals(auth1, auth2);
        assertNotEquals(auth1, auth3);
        assertEquals(auth1.hashCode(), auth2.hashCode());
    }
}
