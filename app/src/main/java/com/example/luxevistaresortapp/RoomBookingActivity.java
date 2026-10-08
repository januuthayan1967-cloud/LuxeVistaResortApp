package com.example.luxevistaresortapp;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import java.util.Calendar;

public class RoomBookingActivity extends AppCompatActivity {

    private Spinner roomTypeSpinner;
    private EditText checkInDate, checkOutDate;
    private TextView roomPrice;
    private Button bookNowButton;
    private DBHelper dbHelper;
    private SessionManager sessionManager;

    private final String[] roomTypes = {"Ocean View Suite", "Deluxe Room", "Single Room", "Family Room"};
    private final String[] roomPrices = {"25000", "18000", "12000", "30000"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_booking);

        dbHelper = DBHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        // Initialize views
        roomTypeSpinner = findViewById(R.id.roomTypeSpinner);
        checkInDate = findViewById(R.id.checkInDate);
        checkOutDate = findViewById(R.id.checkOutDate);
        roomPrice = findViewById(R.id.roomPrice);
        bookNowButton = findViewById(R.id.bookNowButton);

        // Setup Room Type Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roomTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomTypeSpinner.setAdapter(adapter);

        // Pre-select room if passed from ViewRoomsActivity
        String preSelectedRoom = getIntent().getStringExtra("roomType");
        int preSelectedPos = 0;
        if (preSelectedRoom != null) {
            for (int i = 0; i < roomTypes.length; i++) {
                if (roomTypes[i].equalsIgnoreCase(preSelectedRoom.trim())) {
                    preSelectedPos = i;
                    break;
                }
            }
        }
        roomTypeSpinner.setSelection(preSelectedPos);
        roomPrice.setText("Rs. " + roomPrices[preSelectedPos] + " per night");

        // Set price change on selection
        roomTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                roomPrice.setText("Rs. " + roomPrices[position] + " per night");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                roomPrice.setText("");
            }
        });

        // Date pickers for check-in and check-out
        checkInDate.setOnClickListener(v -> showDatePickerDialog(checkInDate));
        checkOutDate.setOnClickListener(v -> showDatePickerDialog(checkOutDate));

        // Book Now button action with validation and anti-duplicate submission
        bookNowButton.setOnClickListener(v -> {
            String roomType = roomTypeSpinner.getSelectedItem().toString();
            String checkIn = checkInDate.getText().toString().trim();
            String checkOut = checkOutDate.getText().toString().trim();
            String price = extractNumericPrice(roomPrice.getText().toString());

            if (checkIn.isEmpty() || checkOut.isEmpty()) {
                Toast.makeText(RoomBookingActivity.this, "Please select both Check-In and Check-Out dates", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!SecurityUtils.isDateNotPast(checkIn)) {
                Toast.makeText(RoomBookingActivity.this, "Check-In date cannot be in the past", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!SecurityUtils.isCheckOutAfterCheckIn(checkIn, checkOut)) {
                Toast.makeText(RoomBookingActivity.this, "Check-Out date must be after Check-In date", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prevent duplicate taps
            bookNowButton.setEnabled(false);

            String userEmail = sessionManager.getUserEmail();
            boolean isInserted = dbHelper.addBooking(roomType, checkIn, checkOut, price, userEmail);

            if (isInserted) {
                Toast.makeText(RoomBookingActivity.this,
                        "Reservation Confirmed!\n" + roomType +
                                "\nCheck-In: " + checkIn +
                                " | Check-Out: " + checkOut +
                                "\nTotal: Rs. " + price,
                        Toast.LENGTH_LONG).show();
                clearFields();
                finish();
            } else {
                bookNowButton.setEnabled(true);
                Toast.makeText(RoomBookingActivity.this, "Booking failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog(EditText dateField) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RoomBookingActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    dateField.setText(date);
                },
                year, month, day);

        // Prevent selecting dates in the past
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private String extractNumericPrice(String priceText) {
        return priceText.replaceAll("[^0-9]", "");
    }

    private void clearFields() {
        checkInDate.setText("");
        checkOutDate.setText("");
        roomTypeSpinner.setSelection(0);
        roomPrice.setText("Rs. " + roomPrices[0] + " per night");
        bookNowButton.setEnabled(true);
    }
}
