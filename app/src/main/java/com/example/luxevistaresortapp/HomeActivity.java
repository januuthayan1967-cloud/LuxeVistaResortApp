package com.example.luxevistaresortapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private ImageButton navHome, navServices, navAttractions, navProfile;
    private Button roomBookingButton;
    private TextView welcomeGreeting, welcomeSub;

    // Quick Action shortcuts
    private View actionExploreRooms, actionMyReservations, actionServices, actionOffers;

    // Featured Suite CTAs
    private Button btnHomeBookOceanSuite, btnHomeBookFamilyRoom;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupPersonalizedWelcome();
        setupNavigationAndActions();
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

        roomBookingButton = findViewById(R.id.roomBookingButton);

        actionExploreRooms = findViewById(R.id.actionExploreRooms);
        actionMyReservations = findViewById(R.id.actionMyReservations);
        actionServices = findViewById(R.id.actionServices);
        actionOffers = findViewById(R.id.actionOffers);

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
                welcomeSub.setText("Welcome back to your luxury sanctuary");
            } else {
                welcomeSub.setText("Experience ultimate five-star comfort");
            }
        }
    }

    private void setupNavigationAndActions() {
        // Quick Action Shortcuts
        if (actionExploreRooms != null) {
            actionExploreRooms.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ViewRoomsActivity.class)));
        }

        if (actionMyReservations != null) {
            actionMyReservations.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ViewBookingsActivity.class)));
        }

        if (actionServices != null) {
            actionServices.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ServicesActivity.class)));
        }

        if (actionOffers != null) {
            actionOffers.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, HotelOffersActivity.class)));
        }

        // Featured Room CTAs
        if (btnHomeBookOceanSuite != null) {
            btnHomeBookOceanSuite.setOnClickListener(v -> openRoomBooking("Ocean View Suite"));
        }

        if (btnHomeBookFamilyRoom != null) {
            btnHomeBookFamilyRoom.setOnClickListener(v -> openRoomBooking("Family Room"));
        }

        // Master Room Catalog CTA Button
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
