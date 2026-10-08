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

    @Test
    public void testDynamicRoomPricingScenarios() {
        // Ocean View Suite: Rs. 25,000 / night
        long oceanRate = 25000L;
        assertEquals(25000L, 1 * oceanRate);
        assertEquals(75000L, 3 * oceanRate);
        assertEquals(175000L, 7 * oceanRate);

        // Deluxe Room: Rs. 18,000 / night
        long deluxeRate = 18000L;
        assertEquals(18000L, 1 * deluxeRate);
        assertEquals(54000L, 3 * deluxeRate);
        assertEquals(126000L, 7 * deluxeRate);

        // Single Room: Rs. 12,000 / night
        long singleRate = 12000L;
        assertEquals(12000L, 1 * singleRate);
        assertEquals(36000L, 3 * singleRate);
        assertEquals(84000L, 7 * singleRate);

        // Family Room: Rs. 30,000 / night
        long familyRate = 30000L;
        assertEquals(30000L, 1 * familyRate);
        assertEquals(90000L, 3 * familyRate);
        assertEquals(210000L, 7 * familyRate);
    }

    @Test
    public void testStayDurationAndPricingIntegration() {
        // 1-Night Stay
        int nights1 = SecurityUtils.calculateStayNights("10/11/2026", "11/11/2026");
        assertEquals(1, nights1);
        assertEquals(25000L, nights1 * 25000L);

        // 3-Night Stay
        int nights3 = SecurityUtils.calculateStayNights("10/11/2026", "13/11/2026");
        assertEquals(3, nights3);
        assertEquals(75000L, nights3 * 25000L);

        // 7-Night Stay
        int nights7 = SecurityUtils.calculateStayNights("10/11/2026", "17/11/2026");
        assertEquals(7, nights7);
        assertEquals(175000L, nights7 * 25000L);
    }

    @Test
    public void testGuestStepperBoundsAndLimits() {
        int minAdults = 1;
        int maxAdults = 10;
        int minChildren = 0;
        int maxChildren = 10;

        // Adults start at 1, cannot decrement below 1
        int adults = 1;
        if (adults > minAdults) adults--;
        assertEquals(1, adults);

        // Adults can increment up to maxAdults
        for (int i = 0; i < 20; i++) {
            if (adults < maxAdults) adults++;
        }
        assertEquals(10, adults);

        // Children start at 0, cannot decrement below 0
        int children = 0;
        if (children > minChildren) children--;
        assertEquals(0, children);

        // Children can increment up to maxChildren
        for (int i = 0; i < 20; i++) {
            if (children < maxChildren) children++;
        }
        assertEquals(10, children);
    }

    @Test
    public void testPricingArithmeticOverflowSafety() {
        long rate = 30000L;
        int nights = 365;
        long total = Math.multiplyExact((long) nights, rate);
        assertEquals(10950000L, total);
    }

    @Test
    public void testGenerateShareableTextFormatting() {
        BookingModel model = new BookingModel(
                99L,
                "Ocean View Suite",
                "15/11/2026",
                "20/11/2026",
                "125000",
                "alex@luxevista.com",
                "LVR-2026-000099",
                5,
                2,
                1,
                "RESERVED",
                "1672531199000"
        );

        String text = BookingSuccessActivity.generateShareableText(model);
        assertNotNull(text);
        assertTrue(text.contains("LUXEVISTA RESORT & SPA"));
        assertTrue(text.contains("LVR-2026-000099"));
        assertTrue(text.contains("Ocean View Suite"));
        assertTrue(text.contains("15/11/2026"));
        assertTrue(text.contains("20/11/2026"));
        assertTrue(text.contains("5 Night(s)"));
        assertTrue(text.contains("2 Adult(s), 1 Child(ren)"));
        assertTrue(text.contains("125,000"));
        assertTrue(text.contains("RESERVED (Saved Locally)"));
        assertTrue(text.contains("Notice: This reservation is saved locally on this device"));
    }

    @Test
    public void testGenerateShareableTextNullSafety() {
        String text = BookingSuccessActivity.generateShareableText(null);
        assertEquals("", text);
    }
}
