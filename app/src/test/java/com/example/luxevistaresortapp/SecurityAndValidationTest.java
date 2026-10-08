package com.example.luxevistaresortapp;

import org.junit.Test;
import static org.junit.Assert.*;

public class SecurityAndValidationTest {

    @Test
    public void testSaltGeneration() {
        String salt1 = SecurityUtils.generateSalt();
        String salt2 = SecurityUtils.generateSalt();
        assertNotNull(salt1);
        assertNotNull(salt2);
        assertEquals(32, salt1.length()); // 16 bytes hex is 32 characters
        assertNotEquals(salt1, salt2); // Salts must be unique
    }

    @Test
    public void testPasswordHashing() {
        String salt = SecurityUtils.generateSalt();
        String pass = "SecurePass123";

        String hash1 = SecurityUtils.hashPassword(pass, salt);
        String hash2 = SecurityUtils.hashPassword(pass, salt);

        assertNotNull(hash1);
        assertEquals(hash1, hash2); // Deterministic with same salt

        String differentSalt = SecurityUtils.generateSalt();
        String hashDifferentSalt = SecurityUtils.hashPassword(pass, differentSalt);
        assertNotEquals(hash1, hashDifferentSalt); // Different salt yields different hash
    }

    @Test
    public void testDateSequenceValidation() {
        // Correct chronological order
        assertTrue(SecurityUtils.isCheckOutAfterCheckIn("10/10/2026", "15/10/2026"));

        // Same day is not after
        assertFalse(SecurityUtils.isCheckOutAfterCheckIn("10/10/2026", "10/10/2026"));

        // CheckOut before CheckIn
        assertFalse(SecurityUtils.isCheckOutAfterCheckIn("15/10/2026", "10/10/2026"));

        // Malformed dates
        assertFalse(SecurityUtils.isCheckOutAfterCheckIn("invalid", "10/10/2026"));
    }

    @Test
    public void testPriceExtraction() {
        String rawPrice = "Rs. 25,000 per night";
        String extracted = rawPrice.replaceAll("[^0-9]", "");
        assertEquals("25000", extracted);

        String rawPrice2 = "Rs. 18000";
        assertEquals("18000", rawPrice2.replaceAll("[^0-9]", ""));
    }

    @Test
    public void testUserModel() {
        UserModel user = new UserModel(1, "Alexander", "alex@luxevista.com");
        assertEquals(1, user.getId());
        assertEquals("Alexander", user.getName());
        assertEquals("alex@luxevista.com", user.getEmail());
    }
}
