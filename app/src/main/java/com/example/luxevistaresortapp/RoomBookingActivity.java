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

    private String[] roomTypes = {"Ocean View Suite", "Deluxe Room", "Single Room", "Family Room"};
    private String[] roomPrices = {"25000", "18000", "12000", "30000"};  // Clean numeric values

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_booking);

        // Get DBHelper instance (singleton)
        dbHelper = DBHelper.getInstance(this);

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

        // Set initial price and handle price change on selection
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

        // Book Now button action
        bookNowButton.setOnClickListener(v -> {
            String roomType = roomTypeSpinner.getSelectedItem().toString();
            String checkIn = checkInDate.getText().toString();
            String checkOut = checkOutDate.getText().toString();
            String price = extractNumericPrice(roomPrice.getText().toString());  // clean numeric price

            if (checkIn.isEmpty() || checkOut.isEmpty()) {
                Toast.makeText(RoomBookingActivity.this, "Please fill in all details", Toast.LENGTH_SHORT).show();
            } else {
                boolean isInserted = dbHelper.addBooking(roomType, checkIn, checkOut, price);

                if (isInserted) {
                    Toast.makeText(RoomBookingActivity.this,
                            "Room Booked!\nType: " + roomType +
                                    "\nCheck-In: " + checkIn +
                                    "\nCheck-Out: " + checkOut +
                                    "\nPrice: Rs. " + price,
                            Toast.LENGTH_LONG).show();
                    clearFields();
                } else {
                    Toast.makeText(RoomBookingActivity.this, "Room booking failed! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Date picker dialog
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

        datePickerDialog.show();
    }

    // Helper to clean numeric price (remove Rs. and text)
    private String extractNumericPrice(String priceText) {
        return priceText.replaceAll("[^0-9]", "");
    }

    // Clear fields after booking
    private void clearFields() {
        checkInDate.setText("");
        checkOutDate.setText("");
        roomTypeSpinner.setSelection(0);
        roomPrice.setText("Rs. " + roomPrices[0] + " per night");
    }
}
