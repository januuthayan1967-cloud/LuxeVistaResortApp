package com.example.luxevistaresortapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userEmail;
    private View btnEditProfile;
    private ImageButton btnBackProfile;

    // Redesigned Menu Cards
    private View menuPersonalInfo, menuReservations, menuServiceBookings;
    private View menuSettings, menuHelpSupport, menuLogout;

    // Legacy buttons for backward compatibility
    private Button viewRoomBookingsButton, viewServiceBookingsButton, logoutButton;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_profile);

        sessionManager = SessionManager.getInstance(this);

        initViews();
        loadUserDetails();
        setupMenuActions();
        EdgeToEdgeHelper.setupBottomNavigation(this, EdgeToEdgeHelper.TAB_PROFILE);
    }

    private void initViews() {
        btnBackProfile = findViewById(R.id.btnBackProfile);
        if (btnBackProfile != null) {
            btnBackProfile.setOnClickListener(v -> finish());
        }

        View headerBar = findViewById(R.id.profileHeaderBar);
        if (headerBar != null) {
            EdgeToEdgeHelper.applyTopInset(headerBar);
        }

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        menuPersonalInfo = findViewById(R.id.menuPersonalInfo);
        menuReservations = findViewById(R.id.menuReservations);
        menuServiceBookings = findViewById(R.id.menuServiceBookings);
        menuSettings = findViewById(R.id.menuSettings);
        menuHelpSupport = findViewById(R.id.menuHelpSupport);
        menuLogout = findViewById(R.id.menuLogout);

        viewRoomBookingsButton = findViewById(R.id.viewBookingsButton);
        viewServiceBookingsButton = findViewById(R.id.viewServiceBookingsButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void loadUserDetails() {
        String email = sessionManager.getUserEmail();
        String name = sessionManager.getUserName();

        if (email != null && !email.isEmpty()) {
            if (name != null && !name.isEmpty() && !name.equalsIgnoreCase("Valued Guest")) {
                userName.setText(name);
            } else {
                DBHelper dbHelper = DBHelper.getInstance(this);
                String dbName = dbHelper.getUserNameByEmail(email);
                userName.setText(dbName != null && !dbName.isEmpty() ? dbName : "Valued Guest");
            }
            userEmail.setText(email);
        } else {
            userName.setText("Valued Guest");
            userEmail.setText("guest@luxevista.com");
        }
    }

    private void setupMenuActions() {
        // Edit Profile Action
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }

        // 1. Personal Information
        if (menuPersonalInfo != null) {
            menuPersonalInfo.setOnClickListener(v -> showPersonalInfoDialog());
        }

        // 2. My Reservations
        View.OnClickListener reservationsListener = v -> {
            Intent intent = new Intent(ProfileActivity.this, ViewBookingsActivity.class);
            startActivity(intent);
        };
        if (menuReservations != null) {
            menuReservations.setOnClickListener(reservationsListener);
        }
        if (viewRoomBookingsButton != null) {
            viewRoomBookingsButton.setOnClickListener(reservationsListener);
        }

        // 3. Service Bookings
        View.OnClickListener serviceBookingsListener = v -> {
            Intent intent = new Intent(ProfileActivity.this, ServiceBookingsActivity.class);
            startActivity(intent);
        };
        if (menuServiceBookings != null) {
            menuServiceBookings.setOnClickListener(serviceBookingsListener);
        }
        if (viewServiceBookingsButton != null) {
            viewServiceBookingsButton.setOnClickListener(serviceBookingsListener);
        }

        // 4. Settings
        if (menuSettings != null) {
            menuSettings.setOnClickListener(v -> showSettingsDialog());
        }

        // 5. Help & Support
        if (menuHelpSupport != null) {
            menuHelpSupport.setOnClickListener(v -> showHelpSupportDialog());
        }

        // 6. Logout
        View.OnClickListener logoutListener = v -> performLogout();
        if (menuLogout != null) {
            menuLogout.setOnClickListener(logoutListener);
        }
        if (logoutButton != null) {
            logoutButton.setOnClickListener(logoutListener);
        }
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile Name");

        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(sessionManager.getUserName());
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            sessionManager.updateUserName(newName);
            userName.setText(newName);
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showPersonalInfoDialog() {
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        new AlertDialog.Builder(this)
                .setTitle("Personal Information")
                .setMessage("Guest Name: " + (name != null ? name : "Valued Guest") +
                        "\n\nEmail Address: " + (email != null ? email : "guest@luxevista.com") +
                        "\n\nMembership Status: VIP Resort Member" +
                        "\n\nPreferred Language: English" +
                        "\n\nDirect Concierge: +94 77 123 4567")
                .setPositiveButton("Close", null)
                .show();
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("App Settings & Preferences")
                .setMessage("Resort Theme: Luxury Gold & Navy (Active)" +
                        "\n\nNotifications: Enabled" +
                        "\n\nCurrency Display: LKR (Sri Lankan Rupee)" +
                        "\n\nApp Version: 2.1.0 (Luxury Edition)")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showHelpSupportDialog() {
        new AlertDialog.Builder(this)
                .setTitle("LuxeVista Help & Support")
                .setMessage("24/7 Front Desk: Extension 0" +
                        "\n\nDirect Phone: +94 77 123 4567" +
                        "\n\nConcierge Email: concierge@luxevista.com" +
                        "\n\nEmergency Assistance: Extension 911")
                .setPositiveButton("OK", null)
                .show();
    }

    private void performLogout() {
        sessionManager.logoutUser();
        Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
