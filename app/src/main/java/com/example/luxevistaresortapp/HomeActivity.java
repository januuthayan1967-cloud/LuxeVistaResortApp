package com.example.luxevistaresortapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.Intent;

public class HomeActivity extends AppCompatActivity {

    private ImageButton navHome, navServices, navAttractions, navProfile;
    private Button roomBookingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize buttons
        navHome = findViewById(R.id.navHome);
        navServices = findViewById(R.id.navServices);
        navAttractions = findViewById(R.id.navAttractions);
        navProfile = findViewById(R.id.navProfile);
        roomBookingButton = findViewById(R.id.roomBookingButton);

        // Set actions
        navHome.setOnClickListener(v -> {});

        navServices.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ServicesActivity.class));
        });

        navAttractions.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AttractionsActivity.class));
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        // Room Booking button
        roomBookingButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ViewRoomsActivity.class));
        });
    }
}
