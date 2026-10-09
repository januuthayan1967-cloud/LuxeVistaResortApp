package com.example.luxevistaresortapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

public class BookingActivity extends AppCompatActivity {

    private TextView serviceNameTextView;
    private EditText nameEditText, dateEditText, timeEditText;
    private Button confirmButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_booking);

        EdgeToEdgeHelper.applyRootWindowInsets(findViewById(R.id.bookingScrollView));

        sessionManager = SessionManager.getInstance(this);

        // Initialize views
        serviceNameTextView = findViewById(R.id.serviceNameTextView);
        nameEditText = findViewById(R.id.nameEditText);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        confirmButton = findViewById(R.id.confirmButton);

        // Get service name from the intent
        String serviceName = getIntent().getStringExtra("serviceName");
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = "Resort Service";
        }
        serviceNameTextView.setText(serviceName + " Booking");

        // Pre-fill user name from active session if available
        String savedUserName = sessionManager.getUserName();
        if (savedUserName != null && !savedUserName.isEmpty() && !savedUserName.equals("Valued Guest")) {
            nameEditText.setText(savedUserName);
        }

        // Date Picker Dialog
        final String finalServiceName = serviceName;
        dateEditText.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    BookingActivity.this,
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        dateEditText.setText(selectedDate);
                    },
                    year, month, day);

            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        // Time Picker Dialog
        timeEditText.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    BookingActivity.this,
                    (view1, selectedHour, selectedMinute) -> {
                        String selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                        timeEditText.setText(selectedTime);
                    },
                    hour, minute, true);
            timePickerDialog.show();
        });

        // Confirm Button Action with validation and anti-duplicate submission
        confirmButton.setOnClickListener(view -> {
            String customerName = nameEditText.getText().toString().trim();
            String date = dateEditText.getText().toString().trim();
            String time = timeEditText.getText().toString().trim();

            if (customerName.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(BookingActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!SecurityUtils.isDateNotPast(date)) {
                Toast.makeText(BookingActivity.this, "Booking date cannot be in the past", Toast.LENGTH_SHORT).show();
                return;
            }

            confirmButton.setEnabled(false);

            DBHelper dbHelper = DBHelper.getInstance(BookingActivity.this);
            String userEmail = sessionManager.getUserEmail();
            boolean isInserted = dbHelper.addServiceBooking(finalServiceName, customerName, date, time, userEmail);

            if (isInserted) {
                Toast.makeText(BookingActivity.this, "Booking Confirmed for " + customerName, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                confirmButton.setEnabled(true);
                Toast.makeText(BookingActivity.this, "Failed to save booking. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
