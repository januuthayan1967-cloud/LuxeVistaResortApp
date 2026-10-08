package com.example.luxevistaresortapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;

public class ServiceBookingsActivity extends AppCompatActivity {

    private ListView serviceBookingsListView;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_bookings);

        serviceBookingsListView = findViewById(R.id.serviceBookingsListView);  // ListView in your XML layout
        dbHelper = DBHelper.getInstance(this);  // Initialize DBHelper

        // Fetch all service bookings
        Cursor cursor = dbHelper.getAllServiceBookings();

        // Define columns to display in the ListView
        String[] fromColumns = {"serviceName", "customerName", "date", "time"};
        int[] toViews = {R.id.serviceNameTextView, R.id.customerNameTextView, R.id.dateTextView, R.id.timeTextView};

        // Use SimpleCursorAdapter to bind data to ListView
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.activity_booking,  // Custom layout for each item
                cursor,
                fromColumns,
                toViews,
                0
        );

        serviceBookingsListView.setAdapter(adapter);  // Set the adapter to the ListView
    }
}
