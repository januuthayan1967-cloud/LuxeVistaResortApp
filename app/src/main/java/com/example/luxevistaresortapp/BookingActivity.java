package com.example.luxevistaresortapp;

import android.os.Bundle;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Initialize views
        serviceNameTextView = findViewById(R.id.serviceNameTextView);
        nameEditText = findViewById(R.id.nameEditText);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        confirmButton = findViewById(R.id.confirmButton);

        // Get service name from the intent
        String serviceName = getIntent().getStringExtra("serviceName");
        serviceNameTextView.setText(serviceName + " Booking");

        // Date Picker Dialog
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
                        String selectedTime = selectedHour + ":" + (selectedMinute < 10 ? "0" + selectedMinute : selectedMinute);
                        timeEditText.setText(selectedTime);
                    },
                    hour, minute, true);
            timePickerDialog.show();
        });

        // Confirm Button Action
        confirmButton.setOnClickListener(view -> {
            String customerName = nameEditText.getText().toString();
            String date = dateEditText.getText().toString();
            String time = timeEditText.getText().toString();

            if (customerName.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(BookingActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            } else {
                // Proceed with service booking
                DBHelper dbHelper = DBHelper.getInstance(BookingActivity.this);
                boolean isInserted = dbHelper.addServiceBooking(serviceName, customerName, date, time);

                if (isInserted) {
                    Toast.makeText(BookingActivity.this, "Booking Confirmed for " + customerName, Toast.LENGTH_SHORT).show();
                    finish(); // Close activity after booking
                } else {
                    Toast.makeText(BookingActivity.this, "Failed to save booking", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
