package com.example.luxevistaresortapp;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

public class ServicesActivity extends AppCompatActivity {

    public static final String SERVICE_SPA = "Spa Treatments";
    public static final String SERVICE_DINING = "Fine Dining";
    public static final String SERVICE_CABANAS = "Poolside Cabanas";
    public static final String SERVICE_BEACH_TOURS = "Guided Beach Tours";

    private ListView servicesListView;
    private Button btnBookSpa, btnBookDining, btnBookCabanas, btnBookBeachTours;
    private ImageButton btnBackServices;

    // Filter Chips
    private TextView chipFilterAll, chipFilterSpa, chipFilterDining, chipFilterWellness;

    // Service Cards
    private View cardServiceSpa, cardServiceDining, cardServiceCabanas, cardServiceBeachTours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_services);

        initViews();
        setupServiceActions();
        setupCategoryFilters();
        setupLegacyCompatibility();
        EdgeToEdgeHelper.setupBottomNavigation(this, EdgeToEdgeHelper.TAB_SERVICES);

        // Header top insets
        View topBar = findViewById(R.id.servicesTopBar);
        if (topBar != null) {
            EdgeToEdgeHelper.applyTopInset(topBar);
        }

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

        chipFilterAll = findViewById(R.id.chipFilterAll);
        chipFilterSpa = findViewById(R.id.chipFilterSpa);
        chipFilterDining = findViewById(R.id.chipFilterDining);
        chipFilterWellness = findViewById(R.id.chipFilterWellness);

        cardServiceSpa = findViewById(R.id.cardServiceSpa);
        cardServiceDining = findViewById(R.id.cardServiceDining);
        cardServiceCabanas = findViewById(R.id.cardServiceCabanas);
        cardServiceBeachTours = findViewById(R.id.cardServiceBeachTours);

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

    private void setupCategoryFilters() {
        if (chipFilterAll != null) {
            chipFilterAll.setOnClickListener(v -> selectCategoryFilter("ALL"));
        }
        if (chipFilterSpa != null) {
            chipFilterSpa.setOnClickListener(v -> selectCategoryFilter("SPA"));
        }
        if (chipFilterDining != null) {
            chipFilterDining.setOnClickListener(v -> selectCategoryFilter("DINING"));
        }
        if (chipFilterWellness != null) {
            chipFilterWellness.setOnClickListener(v -> selectCategoryFilter("WELLNESS"));
        }
    }

    public void selectCategoryFilter(String category) {
        int colorActiveText = ContextCompat.getColor(this, R.color.white);
        int colorInactiveText = ContextCompat.getColor(this, R.color.resort_navy_primary);

        // Reset all chips to inactive style
        setChipStyle(chipFilterAll, false, colorInactiveText);
        setChipStyle(chipFilterSpa, false, colorInactiveText);
        setChipStyle(chipFilterDining, false, colorInactiveText);
        setChipStyle(chipFilterWellness, false, colorInactiveText);

        switch (category.toUpperCase()) {
            case "SPA":
                setChipStyle(chipFilterSpa, true, colorActiveText);
                if (cardServiceSpa != null) cardServiceSpa.setVisibility(View.VISIBLE);
                if (cardServiceCabanas != null) cardServiceCabanas.setVisibility(View.VISIBLE);
                if (cardServiceDining != null) cardServiceDining.setVisibility(View.GONE);
                if (cardServiceBeachTours != null) cardServiceBeachTours.setVisibility(View.GONE);
                break;
            case "DINING":
                setChipStyle(chipFilterDining, true, colorActiveText);
                if (cardServiceDining != null) cardServiceDining.setVisibility(View.VISIBLE);
                if (cardServiceSpa != null) cardServiceSpa.setVisibility(View.GONE);
                if (cardServiceCabanas != null) cardServiceCabanas.setVisibility(View.GONE);
                if (cardServiceBeachTours != null) cardServiceBeachTours.setVisibility(View.GONE);
                break;
            case "WELLNESS":
                setChipStyle(chipFilterWellness, true, colorActiveText);
                if (cardServiceSpa != null) cardServiceSpa.setVisibility(View.VISIBLE);
                if (cardServiceBeachTours != null) cardServiceBeachTours.setVisibility(View.VISIBLE);
                if (cardServiceDining != null) cardServiceDining.setVisibility(View.GONE);
                if (cardServiceCabanas != null) cardServiceCabanas.setVisibility(View.GONE);
                break;
            case "ALL":
            default:
                setChipStyle(chipFilterAll, true, colorActiveText);
                if (cardServiceSpa != null) cardServiceSpa.setVisibility(View.VISIBLE);
                if (cardServiceDining != null) cardServiceDining.setVisibility(View.VISIBLE);
                if (cardServiceCabanas != null) cardServiceCabanas.setVisibility(View.VISIBLE);
                if (cardServiceBeachTours != null) cardServiceBeachTours.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setChipStyle(TextView chip, boolean isActive, int textColor) {
        if (chip != null) {
            chip.setBackgroundResource(isActive ? R.drawable.chip_filter_active : R.drawable.chip_filter_inactive);
            chip.setTextColor(textColor);
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
