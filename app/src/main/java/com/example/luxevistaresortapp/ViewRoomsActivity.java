package com.example.luxevistaresortapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ViewRoomsActivity extends AppCompatActivity {

    private Button bookOceanSuite, bookDeluxeRoom, bookSingleRoom, bookFamilyRoom;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rooms);

        // Initialize buttons
        bookOceanSuite = findViewById(R.id.bookOceanSuite);
        bookDeluxeRoom = findViewById(R.id.bookDeluxeRoom);
        bookSingleRoom = findViewById(R.id.bookSingleRoom);
        bookFamilyRoom = findViewById(R.id.bookFamilyRoom);

        // Set button actions
        bookOceanSuite.setOnClickListener(v -> openRoomBooking("Ocean View Suite"));
        bookDeluxeRoom.setOnClickListener(v -> openRoomBooking("Deluxe Room"));
        bookSingleRoom.setOnClickListener(v -> openRoomBooking("Single Room"));
        bookFamilyRoom.setOnClickListener(v -> openRoomBooking("Family Room"));  // <-- Added Family Room action
    }

    private void openRoomBooking(String roomType) {
        Intent intent = new Intent(ViewRoomsActivity.this, RoomBookingActivity.class);
        intent.putExtra("roomType", roomType);
        startActivity(intent);
    }
}
