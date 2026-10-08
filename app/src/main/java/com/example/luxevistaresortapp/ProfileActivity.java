package com.example.luxevistaresortapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userEmail;
    private Button viewRoomBookingsButton, viewServiceBookingsButton, logoutButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = SessionManager.getInstance(this);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        viewRoomBookingsButton = findViewById(R.id.viewBookingsButton);
        viewServiceBookingsButton = findViewById(R.id.viewServiceBookingsButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Populate authenticated user details from session
        String email = sessionManager.getUserEmail();
        String name = sessionManager.getUserName();

        if (email != null && !email.isEmpty()) {
            userName.setText("Name: " + name);
            userEmail.setText("Email: " + email);
        } else {
            // Fallback to DBHelper or prompt login
            DBHelper dbHelper = DBHelper.getInstance(this);
            String dbName = dbHelper.getUserNameByEmail(email);
            userName.setText("Name: " + (dbName != null ? dbName : "Valued Guest"));
            userEmail.setText("Email: " + (email != null && !email.isEmpty() ? email : "guest@luxevista.com"));
        }

        viewRoomBookingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ViewBookingsActivity.class);
            startActivity(intent);
        });

        viewServiceBookingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ServiceBookingsActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
