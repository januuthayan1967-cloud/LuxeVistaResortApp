package com.example.luxevistaresortapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ViewRoomsActivity extends AppCompatActivity {

    public static final String ROOM_OCEAN_SUITE = "Ocean View Suite";
    public static final String ROOM_DELUXE = "Deluxe Room";
    public static final String ROOM_SINGLE = "Single Room";
    public static final String ROOM_FAMILY = "Family Room";

    private Button bookOceanSuite, bookDeluxeRoom, bookSingleRoom, bookFamilyRoom;
    private ImageButton btnBackRooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rooms);

        // Initialize back navigation
        btnBackRooms = findViewById(R.id.btnBackRooms);
        if (btnBackRooms != null) {
            btnBackRooms.setOnClickListener(v -> finish());
        }

        // Initialize booking action buttons
        bookOceanSuite = findViewById(R.id.bookOceanSuite);
        bookDeluxeRoom = findViewById(R.id.bookDeluxeRoom);
        bookSingleRoom = findViewById(R.id.bookSingleRoom);
        bookFamilyRoom = findViewById(R.id.bookFamilyRoom);

        // Set verified room booking actions
        if (bookOceanSuite != null) {
            bookOceanSuite.setOnClickListener(v -> openRoomBooking(ROOM_OCEAN_SUITE));
        }
        if (bookDeluxeRoom != null) {
            bookDeluxeRoom.setOnClickListener(v -> openRoomBooking(ROOM_DELUXE));
        }
        if (bookSingleRoom != null) {
            bookSingleRoom.setOnClickListener(v -> openRoomBooking(ROOM_SINGLE));
        }
        if (bookFamilyRoom != null) {
            bookFamilyRoom.setOnClickListener(v -> openRoomBooking(ROOM_FAMILY));
        }
    }

    private void openRoomBooking(String roomType) {
        Intent intent = new Intent(ViewRoomsActivity.this, RoomBookingActivity.class);
        intent.putExtra(RoomBookingActivity.EXTRA_ROOM_TYPE, roomType);
        intent.putExtra("roomType", roomType);
        startActivity(intent);
    }
}
