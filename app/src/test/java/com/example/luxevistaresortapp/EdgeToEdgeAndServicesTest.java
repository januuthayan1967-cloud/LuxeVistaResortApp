package com.example.luxevistaresortapp;

import org.junit.Test;
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
}
