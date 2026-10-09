package com.example.luxevistaresortapp;

import org.junit.Test;
import static org.junit.Assert.*;

public class HomeAndCatalogTest {

    @Test
    public void testTimeBasedGreetingLogic() {
        // Morning: 05:00 - 11:59
        assertEquals("Good Morning", HomeActivity.getTimeBasedGreeting(5));
        assertEquals("Good Morning", HomeActivity.getTimeBasedGreeting(8));
        assertEquals("Good Morning", HomeActivity.getTimeBasedGreeting(11));

        // Afternoon: 12:00 - 16:59
        assertEquals("Good Afternoon", HomeActivity.getTimeBasedGreeting(12));
        assertEquals("Good Afternoon", HomeActivity.getTimeBasedGreeting(14));
        assertEquals("Good Afternoon", HomeActivity.getTimeBasedGreeting(16));

        // Evening / Night: 17:00 - 04:59
        assertEquals("Good Evening", HomeActivity.getTimeBasedGreeting(17));
        assertEquals("Good Evening", HomeActivity.getTimeBasedGreeting(20));
        assertEquals("Good Evening", HomeActivity.getTimeBasedGreeting(23));
        assertEquals("Good Evening", HomeActivity.getTimeBasedGreeting(0));
        assertEquals("Good Evening", HomeActivity.getTimeBasedGreeting(4));
    }

    @Test
    public void testPersonalizedGreetingGuestNameFormattingAndFallback() {
        // Valid guest names
        assertEquals("Good Morning, Alexander", HomeActivity.formatPersonalizedGreeting("Good Morning", "Alexander"));
        assertEquals("Good Evening, Victoria Sterling", HomeActivity.formatPersonalizedGreeting("Good Evening", "Victoria Sterling"));

        // Fallbacks: null, empty, whitespace, and default "Valued Guest"
        assertEquals("Good Morning, Valued Guest", HomeActivity.formatPersonalizedGreeting("Good Morning", null));
        assertEquals("Good Afternoon, Valued Guest", HomeActivity.formatPersonalizedGreeting("Good Afternoon", ""));
        assertEquals("Good Evening, Valued Guest", HomeActivity.formatPersonalizedGreeting("Good Evening", "   "));
        assertEquals("Good Morning, Valued Guest", HomeActivity.formatPersonalizedGreeting("Good Morning", "Valued Guest"));
        assertEquals("Good Morning, Valued Guest", HomeActivity.formatPersonalizedGreeting("Good Morning", "valued guest"));
    }

    @Test
    public void testVerifiedRoomCategoryConstants() {
        // Confirm Room Category String values in ViewRoomsActivity
        assertEquals("Ocean View Suite", ViewRoomsActivity.ROOM_OCEAN_SUITE);
        assertEquals("Deluxe Room", ViewRoomsActivity.ROOM_DELUXE);
        assertEquals("Single Room", ViewRoomsActivity.ROOM_SINGLE);
        assertEquals("Family Room", ViewRoomsActivity.ROOM_FAMILY);
    }

    @Test
    public void testVerifiedRoomNightlyRates() {
        // Verify nightly rates mapped in RoomBookingActivity
        assertEquals(25000L, RoomBookingActivity.getRoomRate("Ocean View Suite"));
        assertEquals(18000L, RoomBookingActivity.getRoomRate("Deluxe Room"));
        assertEquals(12000L, RoomBookingActivity.getRoomRate("Single Room"));
        assertEquals(30000L, RoomBookingActivity.getRoomRate("Family Room"));

        // Case-insensitivity check
        assertEquals(25000L, RoomBookingActivity.getRoomRate("ocean view suite"));
        assertEquals(30000L, RoomBookingActivity.getRoomRate("family room"));

        // Null and fallback
        assertEquals(25000L, RoomBookingActivity.getRoomRate(null));
        assertEquals(25000L, RoomBookingActivity.getRoomRate("Unknown Suite"));
    }

    @Test
    public void testRoomSelectionIntentExtrasAndPreselectionMapping() {
        // Test stable intent extra keys
        assertEquals("roomType", RoomBookingActivity.EXTRA_ROOM_TYPE);
        assertEquals("selectedRoomType", RoomBookingActivity.EXTRA_SELECTED_ROOM_TYPE);

        // Test position mapping
        assertEquals(0, RoomBookingActivity.getRoomTypePosition("Ocean View Suite"));
        assertEquals(1, RoomBookingActivity.getRoomTypePosition("Deluxe Room"));
        assertEquals(2, RoomBookingActivity.getRoomTypePosition("Single Room"));
        assertEquals(3, RoomBookingActivity.getRoomTypePosition("Family Room"));

        // Case-insensitivity and trim safety
        assertEquals(0, RoomBookingActivity.getRoomTypePosition(" ocean view suite "));
        assertEquals(1, RoomBookingActivity.getRoomTypePosition("deluxe room"));
        assertEquals(2, RoomBookingActivity.getRoomTypePosition("single room"));
        assertEquals(3, RoomBookingActivity.getRoomTypePosition("family room"));

        // Null and unknown fallbacks
        assertEquals(0, RoomBookingActivity.getRoomTypePosition(null));
        assertEquals(0, RoomBookingActivity.getRoomTypePosition("Non-existent Room"));
    }
}
