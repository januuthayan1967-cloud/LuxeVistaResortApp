package com.example.luxevistaresortapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    TextView userName, userEmail;
    Button viewRoomBookingsButton, viewServiceBookingsButton, logoutButton;

    DBHelper dbHelper;
    String userEmailValue, userNameValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        viewRoomBookingsButton = findViewById(R.id.viewBookingsButton);
        viewServiceBookingsButton = findViewById(R.id.viewServiceBookingsButton);
        logoutButton = findViewById(R.id.logoutButton);

        dbHelper = DBHelper.getInstance(this);

        // Get passed user email from intent
        userEmailValue = getIntent().getStringExtra("email");

        if (userEmailValue != null) {
            userNameValue = dbHelper.getUserNameByEmail(userEmailValue);
            userName.setText("Name: " + userNameValue);
            userEmail.setText("Email: " + userEmailValue);
        } else {
            userName.setText("Name: vanojan");
            userEmail.setText("Email: vanojan10@gmail.com");
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
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
