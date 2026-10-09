package com.example.luxevistaresortapp;

import org.junit.Test;
import java.time.LocalDate;
import static org.junit.Assert.*;

public class EdgeToEdgeAndServicesTest {

    @Test
    public void testEdgeToEdgeHelperTabConstants() {
        assertEquals(0, EdgeToEdgeHelper.TAB_HOME);
        assertEquals(1, EdgeToEdgeHelper.TAB_SERVICES);
        assertEquals(2, EdgeToEdgeHelper.TAB_ATTRACTIONS);
        assertEquals(3, EdgeToEdgeHelper.TAB_PROFILE);
    }

    @Test
    public void testVerifiedServiceNames() {
        assertEquals("Spa Treatments", ServicesActivity.SERVICE_SPA);
        assertEquals("Fine Dining", ServicesActivity.SERVICE_DINING);
        assertEquals("Poolside Cabanas", ServicesActivity.SERVICE_CABANAS);
        assertEquals("Guided Beach Tours", ServicesActivity.SERVICE_BEACH_TOURS);
    }

    @Test
    public void testServiceNamesPreserveOriginalDBCompatibility() {
        String[] verifiedServices = new String[] {
                ServicesActivity.SERVICE_SPA,
                ServicesActivity.SERVICE_DINING,
                ServicesActivity.SERVICE_CABANAS,
                ServicesActivity.SERVICE_BEACH_TOURS
        };

        assertEquals(4, verifiedServices.length);
        for (String service : verifiedServices) {
            assertNotNull(service);
            assertFalse(service.trim().isEmpty());
        }
    }

    @Test
    public void testEnglishMonthDateFormatParsing() {
        // Test "d MMM uuuu" format introduced for luxury UI
        LocalDate date = SecurityUtils.parseLocalDate("10 Oct 2026");
        assertNotNull(date);
        assertEquals(2026, date.getYear());
        assertEquals(10, date.getMonthValue());
        assertEquals(10, date.getDayOfMonth());

        // Test standard format
        LocalDate standardDate = SecurityUtils.parseLocalDate("10/10/2026");
        assertNotNull(standardDate);
        assertEquals(2026, standardDate.getYear());
        assertEquals(10, standardDate.getMonthValue());
        assertEquals(10, standardDate.getDayOfMonth());

        // Test isDateNotPast with future dates
        assertTrue(SecurityUtils.isDateNotPast("10 Oct 2026"));
        assertTrue(SecurityUtils.isDateNotPast("15 Dec 2028"));

        // Test past date
        assertFalse(SecurityUtils.isDateNotPast("01 Jan 2020"));
    }
}
