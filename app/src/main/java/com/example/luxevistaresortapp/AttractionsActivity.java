package com.example.luxevistaresortapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.Intent;

public class AttractionsActivity extends AppCompatActivity {

    private Button bookBeachTours, bookWaterSports, bookFineDining;
    private ImageButton btnBackAttractions;

    // Redesigned Attraction Cards
    private View cardPrivateIsland, cardSunsetViewing, cardNatureTrails, cardWaterSports;

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

        // Attraction Card Click Listeners
        cardPrivateIsland = findViewById(R.id.cardPrivateIsland);
        if (cardPrivateIsland != null) {
            cardPrivateIsland.setOnClickListener(v -> openBooking("Guided Beach Tours"));
        }

        cardSunsetViewing = findViewById(R.id.cardSunsetViewing);
        if (cardSunsetViewing != null) {
            cardSunsetViewing.setOnClickListener(v -> showAttractionDetails(
                    "Sunset Viewing Point",
                    "Witness breathtaking panoramic sunsets from our exclusive resort cliffside terrace. Complimentary evening champagne service available daily from 5:30 PM to 6:45 PM. Contact guest concierge for reserved seating."
            ));
        }

        cardNatureTrails = findViewById(R.id.cardNatureTrails);
        if (cardNatureTrails != null) {
            cardNatureTrails.setOnClickListener(v -> showAttractionDetails(
                    "Nature & Tropical Trails",
                    "Explore over 5 kilometers of manicured trails through lush botanical gardens and seaside cliffs. Guided morning eco-walks departure daily at 7:00 AM from the resort activity center."
            ));
        }

        cardWaterSports = findViewById(R.id.cardWaterSports);
        if (cardWaterSports != null) {
            cardWaterSports.setOnClickListener(v -> openBooking("Water Sports"));
        }

        // Legacy button references for full test compatibility
        bookBeachTours = findViewById(R.id.bookBeachTours);
        bookWaterSports = findViewById(R.id.bookWaterSports);
        bookFineDining = findViewById(R.id.bookFineDining);

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

    private void showAttractionDetails(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Close", null)
                .show();
    }

    private void openBooking(String serviceName) {
        Intent intent = new Intent(AttractionsActivity.this, BookingActivity.class);
        intent.putExtra("serviceName", serviceName);
        startActivity(intent);
    }
}
