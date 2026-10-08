package com.example.luxevistaresortapp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ViewBookingsActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private ListView bookingsListView;
    private TextView emptyRoomBookingsText;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bookings);

        bookingsListView = findViewById(R.id.bookingsListView);
        emptyRoomBookingsText = findViewById(R.id.emptyRoomBookingsText);
        dbHelper = DBHelper.getInstance(this);

        SessionManager sessionManager = SessionManager.getInstance(this);
        String userEmail = sessionManager.getUserEmail();

        // Fetch bookings for logged-in user
        cursor = dbHelper.getBookingsForUser(userEmail);

        if (cursor != null && cursor.getCount() > 0) {
            String[] from = {"roomType", "checkIn", "checkOut", "price"};
            int[] to = {R.id.roomTypeText, R.id.checkInText, R.id.checkOutText, R.id.priceText};

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this, R.layout.booking_list_item, cursor, from, to, 0);

            bookingsListView.setAdapter(adapter);
            if (emptyRoomBookingsText != null) {
                emptyRoomBookingsText.setVisibility(View.GONE);
            }
        } else {
            if (emptyRoomBookingsText != null) {
                emptyRoomBookingsText.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}
