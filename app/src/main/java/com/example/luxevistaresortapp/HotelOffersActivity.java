package com.example.luxevistaresortapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

public class HotelOffersActivity extends AppCompatActivity {

    private Button offer1Button, offer2Button, offer3Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_offers); // Link to your XML layout

        offer1Button = findViewById(R.id.offer1Button);
        offer2Button = findViewById(R.id.offer2Button);
        offer3Button = findViewById(R.id.offer3Button);

        // Button Click Actions
        offer1Button.setOnClickListener(v -> {
            Toast.makeText(HotelOffersActivity.this, "Discounted Room Package Selected", Toast.LENGTH_SHORT).show();
            // Navigate to booking or offer details screen
            startActivity(new Intent(HotelOffersActivity.this, RoomBookingActivity.class));
        });

        offer2Button.setOnClickListener(v -> {
            Toast.makeText(HotelOffersActivity.this, "Beachfront Suites Selected", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HotelOffersActivity.this, RoomBookingActivity.class));
        });

        offer3Button.setOnClickListener(v -> {
            Toast.makeText(HotelOffersActivity.this, "All-Inclusive Package Selected", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HotelOffersActivity.this,RoomBookingActivity.class));
        });
    }
}
