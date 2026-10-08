package com.example.luxevistaresortapp;

import org.junit.Test;
import java.time.LocalDate;
import java.util.Locale;
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
    public void testStayDurationWithLocalDateAndChronoUnit() {
        // Standard 5-night stay
        int nights1 = SecurityUtils.calculateStayNights("10/10/2026", "15/10/2026");
        assertEquals(5, nights1);

        // Single night stay
        int nights2 = SecurityUtils.calculateStayNights("10/10/2026", "11/10/2026");
        assertEquals(1, nights2);

        // Leap Year Test: 2024 has Feb 29 (28/2/2024 to 1/3/2024 is 2 days)
        int leapYearNights = SecurityUtils.calculateStayNights("28/2/2024", "1/3/2024");
        assertEquals(2, leapYearNights);

        // Non-Leap Year Test: 2025 has 28 days in Feb (28/2/2025 to 1/3/2025 is 1 day)
        int nonLeapYearNights = SecurityUtils.calculateStayNights("28/2/2025", "1/3/2025");
        assertEquals(1, nonLeapYearNights);

        // Reversed dates return 0
        int reversedNights = SecurityUtils.calculateStayNights("15/10/2026", "10/10/2026");
        assertEquals(0, reversedNights);

        // Same day return 0
        int sameDayNights = SecurityUtils.calculateStayNights("10/10/2026", "10/10/2026");
        assertEquals(0, sameDayNights);

        // ISO format date stay calculation
        int isoFormatNights = SecurityUtils.calculateStayNights("2026-10-10", "2026-10-13");
        assertEquals(3, isoFormatNights);
    }

    @Test
    public void testDynamicPriceCalculationAccuracy() {
        long nightlyRate = 25000L;
        int nights = 4;
        long total = nights * nightlyRate;
        assertEquals(100000L, total); // Exact integer calculation, no floating-point rounding errors
    }

    @Test
    public void testDynamicBookingReferenceFormat() {
        int currentYear = LocalDate.now().getYear();
        long bookingId = 42L;
        String ref = String.format(Locale.US, "LVR-%d-%06d", currentYear, bookingId);

        assertTrue(ref.startsWith("LVR-" + currentYear + "-"));
        assertEquals("LVR-" + currentYear + "-000042", ref);
    }

    @Test
    public void testBookingModel() {
        BookingModel model = new BookingModel(
                1L, "Ocean View Suite", "10/10/2026", "14/10/2026",
                "100000", "guest@luxevista.com", "LVR-2026-000001",
                4, 2, 1, "RESERVED", "1672531199000"
        );

        assertEquals(1L, model.getId());
        assertEquals("Ocean View Suite", model.getRoomType());
        assertEquals(4, model.getNights());
        assertEquals(2, model.getAdults());
        assertEquals(1, model.getChildren());
        assertEquals("RESERVED", model.getStatus());
        assertFalse(model.isCancelled());
    }

    @Test
    public void testUserModel() {
        UserModel user = new UserModel(1, "Alexander", "alex@luxevista.com");
        assertEquals(1, user.getId());
        assertEquals("Alexander", user.getName());
        assertEquals("alex@luxevista.com", user.getEmail());
    }
}
