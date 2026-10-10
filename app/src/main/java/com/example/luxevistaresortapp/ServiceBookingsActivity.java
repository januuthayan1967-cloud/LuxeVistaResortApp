package com.example.luxevistaresortapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * Activity displaying the authenticated user's service reservation history
 * styled with the luxury LuxeVista aesthetic.
 */
public class ServiceBookingsActivity extends AppCompatActivity {

    private ListView serviceBookingsListView;
    private LinearLayout emptyStateContainer;
    private TextView emptyServiceBookingsText;
    private ImageView btnBackServiceHistory;
    private Button btnEmptyExploreServices;

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_service_bookings);

        View headerBar = findViewById(R.id.serviceBookingsHeaderBar);
        if (headerBar != null) {
            EdgeToEdgeHelper.applyTopInset(headerBar);
        }
        EdgeToEdgeHelper.applyBottomInset(findViewById(R.id.serviceBookingsListView));

        dbHelper = DBHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadServiceBookings();
    }

    private void initViews() {
        serviceBookingsListView = findViewById(R.id.serviceBookingsListView);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        emptyServiceBookingsText = findViewById(R.id.emptyServiceBookingsText);
        btnBackServiceHistory = findViewById(R.id.btnBackServiceHistory);
        btnEmptyExploreServices = findViewById(R.id.btnEmptyExploreServices);
    }

    private void setupListeners() {
        if (btnBackServiceHistory != null) {
            btnBackServiceHistory.setOnClickListener(v -> finish());
        }

        if (btnEmptyExploreServices != null) {
            btnEmptyExploreServices.setOnClickListener(v -> {
                Intent intent = new Intent(ServiceBookingsActivity.this, ServicesActivity.class);
                startActivity(intent);
            });
        }
    }

    /**
     * Loads the service reservations for the currently authenticated user
     * strictly adhering to row-level ownership.
     */
    private void loadServiceBookings() {
        String userEmail = sessionManager.getUserEmail();
        if (userEmail == null || userEmail.trim().isEmpty()) {
            showEmptyState(true);
            return;
        }

        List<ServiceBookingModel> serviceBookings = dbHelper.getServiceBookingListForUser(userEmail);

        if (serviceBookings == null || serviceBookings.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
            ServiceBookingAdapter adapter = new ServiceBookingAdapter(this, serviceBookings);
            serviceBookingsListView.setAdapter(adapter);
        }
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
            if (serviceBookingsListView != null) serviceBookingsListView.setVisibility(View.GONE);
        } else {
            if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
            if (serviceBookingsListView != null) serviceBookingsListView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Adapter binding verified ServiceBookingModel items to the luxury card layout.
     */
    private static class ServiceBookingAdapter extends ArrayAdapter<ServiceBookingModel> {

        public ServiceBookingAdapter(@NonNull Context context, @NonNull List<ServiceBookingModel> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.service_booking_list_item, parent, false);
            }

            ServiceBookingModel item = getItem(position);
            if (item == null) {
                return convertView;
            }

            TextView tvServiceItemReference = convertView.findViewById(R.id.tvServiceItemReference);
            TextView tvServiceItemStatus = convertView.findViewById(R.id.tvServiceItemStatus);
            TextView serviceNameTextView = convertView.findViewById(R.id.serviceNameTextView);
            TextView customerNameTextView = convertView.findViewById(R.id.customerNameTextView);
            TextView dateTextView = convertView.findViewById(R.id.dateTextView);
            TextView timeTextView = convertView.findViewById(R.id.timeTextView);

            if (tvServiceItemReference != null) {
                tvServiceItemReference.setText(item.getFormattedReference());
            }

            if (tvServiceItemStatus != null) {
                tvServiceItemStatus.setText("SAVED LOCALLY");
            }

            if (serviceNameTextView != null) {
                serviceNameTextView.setText(item.getServiceName().isEmpty() ? "Resort Service" : item.getServiceName());
            }

            if (customerNameTextView != null) {
                customerNameTextView.setText(item.getCustomerName().isEmpty() ? "Valued Guest" : item.getCustomerName());
            }

            if (dateTextView != null) {
                dateTextView.setText("Date: " + (item.getDate().isEmpty() ? "—" : item.getDate()));
            }

            if (timeTextView != null) {
                timeTextView.setText("Time: " + (item.getTime().isEmpty() ? "—" : item.getTime()));
            }

            return convertView;
        }
    }
}
