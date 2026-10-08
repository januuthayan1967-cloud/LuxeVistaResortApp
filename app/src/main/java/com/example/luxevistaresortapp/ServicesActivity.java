package com.example.luxevistaresortapp;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;

public class ServicesActivity extends AppCompatActivity {

    private ListView servicesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        servicesListView = findViewById(R.id.servicesListView);

        String[] services = {
                "Spa Treatments",
                "Fine Dining",
                "Poolside Cabanas",
                "Guided Beach Tours"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, services);
        servicesListView.setAdapter(adapter);

        servicesListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedService = services[position];

            Intent intent = new Intent(ServicesActivity.this, BookingActivity.class);
            intent.putExtra("serviceName", selectedService);
            startActivity(intent);
        });

        // Modern back press handler
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }
}
