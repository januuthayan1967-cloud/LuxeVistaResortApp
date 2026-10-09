package com.example.luxevistaresortapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private ImageView navHome, navServices, navAttractions, navProfile;
    private Button roomBookingButton;
    private TextView welcomeGreeting, welcomeSub;

    // Quick Action shortcuts (6 cards matching design reference)
    private View actionExploreRooms, actionMyReservations, actionOffers;
    private View actionServices, actionAttractions, actionProfile;

    // Top and Featured views
    private ImageButton btnNotificationBell;
    private View btnSeeAllFeatured, cardFeaturedExperience;

    // Featured Suite CTAs (preserved for tests)
    private Button btnHomeBookOceanSuite, btnHomeBookFamilyRoom;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_home);

        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupPersonalizedWelcome();
        setupNavigationAndActions();
        EdgeToEdgeHelper.setupBottomNavigation(this, EdgeToEdgeHelper.TAB_HOME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh greeting in case user profile was updated or time of day changed
        setupPersonalizedWelcome();
    }

    private void initViews() {
        welcomeGreeting = findViewById(R.id.welcomeGreeting);
        welcomeSub = findViewById(R.id.welcomeSub);
        btnNotificationBell = findViewById(R.id.btnNotificationBell);

        roomBookingButton = findViewById(R.id.roomBookingButton);

        actionExploreRooms = findViewById(R.id.actionExploreRooms);
        actionMyReservations = findViewById(R.id.actionMyReservations);
        actionOffers = findViewById(R.id.actionOffers);
        actionServices = findViewById(R.id.actionServices);
        actionAttractions = findViewById(R.id.actionAttractions);
        actionProfile = findViewById(R.id.actionProfile);

        btnSeeAllFeatured = findViewById(R.id.btnSeeAllFeatured);
        cardFeaturedExperience = findViewById(R.id.cardFeaturedExperience);

        btnHomeBookOceanSuite = findViewById(R.id.btnHomeBookOceanSuite);
        btnHomeBookFamilyRoom = findViewById(R.id.btnHomeBookFamilyRoom);

        navHome = findViewById(R.id.navHome);
        navServices = findViewById(R.id.navServices);
        navAttractions = findViewById(R.id.navAttractions);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupPersonalizedWelcome() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = getTimeBasedGreeting(hour);
        String guestName = sessionManager != null && sessionManager.isLoggedIn()
                ? sessionManager.getUserName()
                : "Valued Guest";

        if (welcomeGreeting != null) {
            welcomeGreeting.setText(formatPersonalizedGreeting(greeting, guestName));
        }

        if (welcomeSub != null) {
            if (sessionManager != null && sessionManager.isLoggedIn()) {
                welcomeSub.setText("Discover Luxury. Create Memories.");
            } else {
                welcomeSub.setText("Discover Luxury. Create Memories.");
            }
        }
    }

    private void setupNavigationAndActions() {
        // Notification bell
        if (btnNotificationBell != null) {
            btnNotificationBell.setOnClickListener(v ->
                    Toast.makeText(HomeActivity.this, "No new resort notifications at this time", Toast.LENGTH_SHORT).show());
        }

        // Quick Action Shortcuts (6 cards)
        if (actionExploreRooms != null) {
            actionExploreRooms.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ViewRoomsActivity.class)));
        }

        if (actionMyReservations != null) {
            actionMyReservations.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ViewBookingsActivity.class)));
        }

        if (actionOffers != null) {
            actionOffers.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, HotelOffersActivity.class)));
        }

        if (actionServices != null) {
            actionServices.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ServicesActivity.class)));
        }

        if (actionAttractions != null) {
            actionAttractions.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, AttractionsActivity.class)));
        }

        if (actionProfile != null) {
            actionProfile.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        }

        // Featured Experiences
        if (btnSeeAllFeatured != null) {
            btnSeeAllFeatured.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ViewRoomsActivity.class)));
        }

        if (cardFeaturedExperience != null) {
            cardFeaturedExperience.setOnClickListener(v -> openRoomBooking("Ocean View Suite"));
        }

        // Featured Room CTAs (Preserved for tests)
        if (btnHomeBookOceanSuite != null) {
            btnHomeBookOceanSuite.setOnClickListener(v -> openRoomBooking("Ocean View Suite"));
        }

        if (btnHomeBookFamilyRoom != null) {
            btnHomeBookFamilyRoom.setOnClickListener(v -> openRoomBooking("Family Room"));
        }

        // Master Room Catalog CTA Button (Preserved for tests)
        if (roomBookingButton != null) {
            roomBookingButton.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ViewRoomsActivity.class)));
        }

        // Bottom Navigation Bar Actions
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // Already on Home dashboard
            });
        }

        if (navServices != null) {
            navServices.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ServicesActivity.class)));
        }

        if (navAttractions != null) {
            navAttractions.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, AttractionsActivity.class)));
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        }
    }

    private void openRoomBooking(String roomType) {
        Intent intent = new Intent(HomeActivity.this, RoomBookingActivity.class);
        intent.putExtra(RoomBookingActivity.EXTRA_ROOM_TYPE, roomType);
        intent.putExtra("roomType", roomType);
        startActivity(intent);
    }

    /**
     * Determines the appropriate greeting based on local device hour (0-23).
     */
    public static String getTimeBasedGreeting(int hourOfDay) {
        if (hourOfDay >= 5 && hourOfDay < 12) {
            return "Good Morning";
        } else if (hourOfDay >= 12 && hourOfDay < 17) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }

    /**
     * Formats greeting with guest's stored name or falls back to Valued Guest.
     */
    public static String formatPersonalizedGreeting(String greeting, String userName) {
        if (userName == null || userName.trim().isEmpty() || userName.trim().equalsIgnoreCase("Valued Guest")) {
            return greeting + ", Valued Guest";
        }
        return greeting + ", " + userName.trim();
    }
}
