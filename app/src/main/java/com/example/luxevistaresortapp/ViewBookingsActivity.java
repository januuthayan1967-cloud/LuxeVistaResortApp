package com.example.luxevistaresortapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;

public class ViewBookingsActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private ListView bookingsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bookings); // make sure the XML is named correctly

        bookingsListView = findViewById(R.id.bookingsListView);
        dbHelper = DBHelper.getInstance(this); // Using singleton pattern

        Cursor cursor = dbHelper.getAllBookings();

        if (cursor != null && cursor.getCount() > 0) {
            String[] from = {"roomType", "checkIn", "checkOut", "price"};
            int[] to = {R.id.roomTypeText, R.id.checkInText, R.id.checkOutText, R.id.priceText};

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this, R.layout.booking_list_item, cursor, from, to, 0);

            bookingsListView.setAdapter(adapter);
        }
    }
}
