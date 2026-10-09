package com.example.luxevistaresortapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.Intent;

public class AttractionsActivity extends AppCompatActivity {

    private Button bookBeachTours, bookWaterSports, bookFineDining;
    private ImageButton btnBackAttractions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_attractions);

        // Header and back navigation
        btnBackAttractions = findViewById(R.id.btnBackAttractions);
        if (btnBackAttractions != null) {
            btnBackAttractions.setOnClickListener(v -> finish());
        }

        View headerBar = findViewById(R.id.attractionsHeaderBar);
        if (headerBar != null) {
            EdgeToEdgeHelper.applyTopInset(headerBar);
        }

        // Get button references
        bookBeachTours = findViewById(R.id.bookBeachTours);
        bookWaterSports = findViewById(R.id.bookWaterSports);
        bookFineDining = findViewById(R.id.bookFineDining);

        // Set click actions
        if (bookBeachTours != null) {
            bookBeachTours.setOnClickListener(v -> openBooking("Beach Tours"));
        }
        if (bookWaterSports != null) {
            bookWaterSports.setOnClickListener(v -> openBooking("Water Sports"));
        }
        if (bookFineDining != null) {
            bookFineDining.setOnClickListener(v -> openBooking("Fine Dining"));
        }

        // Setup bottom navigation bar
        EdgeToEdgeHelper.setupBottomNavigation(this, EdgeToEdgeHelper.TAB_ATTRACTIONS);
    }

    private void openBooking(String serviceName) {
        Intent intent = new Intent(AttractionsActivity.this, BookingActivity.class);
        intent.putExtra("serviceName", serviceName);
        startActivity(intent);
    }
}
