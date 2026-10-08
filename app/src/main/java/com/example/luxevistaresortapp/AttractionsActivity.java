package com.example.luxevistaresortapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;

public class AttractionsActivity extends AppCompatActivity {

    private Button bookBeachTours, bookWaterSports, bookFineDining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attractions);

        // Get button references
        bookBeachTours = findViewById(R.id.bookBeachTours);
        bookWaterSports = findViewById(R.id.bookWaterSports);
        bookFineDining = findViewById(R.id.bookFineDining);

        // Set click actions
        bookBeachTours.setOnClickListener(v -> openBooking("Beach Tours"));
        bookWaterSports.setOnClickListener(v -> openBooking("Water Sports"));
        bookFineDining.setOnClickListener(v -> openBooking("Fine Dining"));
    }

    private void openBooking(String serviceName) {
        Intent intent = new Intent(AttractionsActivity.this, BookingActivity.class);
        intent.putExtra("serviceName", serviceName);
        startActivity(intent);
    }
}
