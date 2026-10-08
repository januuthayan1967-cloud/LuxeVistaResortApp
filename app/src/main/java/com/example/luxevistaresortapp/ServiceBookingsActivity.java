package com.example.luxevistaresortapp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ServiceBookingsActivity extends AppCompatActivity {

    private ListView serviceBookingsListView;
    private TextView emptyServiceBookingsText;
    private DBHelper dbHelper;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_bookings);

        serviceBookingsListView = findViewById(R.id.serviceBookingsListView);
        emptyServiceBookingsText = findViewById(R.id.emptyServiceBookingsText);
        dbHelper = DBHelper.getInstance(this);

        SessionManager sessionManager = SessionManager.getInstance(this);
        String userEmail = sessionManager.getUserEmail();

        // Fetch service bookings for logged-in user
        cursor = dbHelper.getServiceBookingsForUser(userEmail);

        if (cursor != null && cursor.getCount() > 0) {
            String[] fromColumns = {"serviceName", "customerName", "date", "time"};
            int[] toViews = {R.id.serviceNameTextView, R.id.customerNameTextView, R.id.dateTextView, R.id.timeTextView};

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this,
                    R.layout.service_booking_list_item,
                    cursor,
                    fromColumns,
                    toViews,
                    0
            );

            serviceBookingsListView.setAdapter(adapter);
            if (emptyServiceBookingsText != null) {
                emptyServiceBookingsText.setVisibility(View.GONE);
            }
        } else {
            if (emptyServiceBookingsText != null) {
                emptyServiceBookingsText.setVisibility(View.VISIBLE);
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
