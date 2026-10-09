package com.example.luxevistaresortapp;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ArrayAdapter;

public class ServicesActivity extends AppCompatActivity {

    public static final String SERVICE_SPA = "Spa Treatments";
    public static final String SERVICE_DINING = "Fine Dining";
    public static final String SERVICE_CABANAS = "Poolside Cabanas";
    public static final String SERVICE_BEACH_TOURS = "Guided Beach Tours";

    private ListView servicesListView;
    private Button btnBookSpa, btnBookDining, btnBookCabanas, btnBookBeachTours;
    private ImageButton btnBackServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_services);

        initViews();
        setupServiceActions();
        setupLegacyCompatibility();
        EdgeToEdgeHelper.setupBottomNavigation(this, EdgeToEdgeHelper.TAB_SERVICES);

        // Modern back press handler
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void initViews() {
        btnBackServices = findViewById(R.id.btnBackServices);
        btnBookSpa = findViewById(R.id.btnBookSpa);
        btnBookDining = findViewById(R.id.btnBookDining);
        btnBookCabanas = findViewById(R.id.btnBookCabanas);
        btnBookBeachTours = findViewById(R.id.btnBookBeachTours);
        servicesListView = findViewById(R.id.servicesListView);

        if (btnBackServices != null) {
            btnBackServices.setOnClickListener(v -> finish());
        }
    }

    private void setupServiceActions() {
        if (btnBookSpa != null) {
            btnBookSpa.setOnClickListener(v -> openBooking(SERVICE_SPA));
        }

        if (btnBookDining != null) {
            btnBookDining.setOnClickListener(v -> openBooking(SERVICE_DINING));
        }

        if (btnBookCabanas != null) {
            btnBookCabanas.setOnClickListener(v -> openBooking(SERVICE_CABANAS));
        }

        if (btnBookBeachTours != null) {
            btnBookBeachTours.setOnClickListener(v -> openBooking(SERVICE_BEACH_TOURS));
        }
    }

    private void setupLegacyCompatibility() {
        if (servicesListView != null) {
            String[] services = {
                    SERVICE_SPA,
                    SERVICE_DINING,
                    SERVICE_CABANAS,
                    SERVICE_BEACH_TOURS
            };

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, services);
            servicesListView.setAdapter(adapter);

            servicesListView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedService = services[position];
                openBooking(selectedService);
            });
        }
    }

    private void openBooking(String serviceName) {
        Intent intent = new Intent(ServicesActivity.this, BookingActivity.class);
        intent.putExtra("serviceName", serviceName);
        startActivity(intent);
    }
}
