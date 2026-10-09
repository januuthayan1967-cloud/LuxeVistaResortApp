package com.example.luxevistaresortapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private ImageButton btnBackBooking;
    private TextView bookingHeaderTitle;
    private ImageView bookingHeroImage;
    private TextView serviceNameTextView, servicePriceTextView, serviceSubtitleTextView;
    private TextView tvServiceDuration, tvServiceBenefit;

    private EditText nameEditText, dateEditText, timeEditText, specialRequestsEditText;
    private View datePickerContainer;

    // Time Slot Chips
    private TextView chipTime10AM, chipTime12PM, chipTime02PM;
    private TextView chipTime04PM, chipTime06PM, chipTime08PM;
    private String selectedTimeSlot = "12:00 PM";

    private Button confirmButton;
    private SessionManager sessionManager;
    private SimpleDateFormat displayDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_booking);

        displayDateFormat = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);

        View headerBar = findViewById(R.id.bookingHeaderBar);
        if (headerBar != null) {
            EdgeToEdgeHelper.applyTopInset(headerBar);
        }
        EdgeToEdgeHelper.applyRootWindowInsets(findViewById(R.id.bookingScrollView));

        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupServiceData();
        setupDatePicker();
        setupTimeSlotChips();
        setupConfirmationAction();
    }

    private void initViews() {
        btnBackBooking = findViewById(R.id.btnBackBooking);
        bookingHeaderTitle = findViewById(R.id.bookingHeaderTitle);
        bookingHeroImage = findViewById(R.id.bookingHeroImage);

        serviceNameTextView = findViewById(R.id.serviceNameTextView);
        servicePriceTextView = findViewById(R.id.servicePriceTextView);
        serviceSubtitleTextView = findViewById(R.id.serviceSubtitleTextView);
        tvServiceDuration = findViewById(R.id.tvServiceDuration);
        tvServiceBenefit = findViewById(R.id.tvServiceBenefit);

        nameEditText = findViewById(R.id.nameEditText);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        datePickerContainer = findViewById(R.id.datePickerContainer);
        specialRequestsEditText = findViewById(R.id.specialRequestsEditText);

        chipTime10AM = findViewById(R.id.chipTime10AM);
        chipTime12PM = findViewById(R.id.chipTime12PM);
        chipTime02PM = findViewById(R.id.chipTime02PM);
        chipTime04PM = findViewById(R.id.chipTime04PM);
        chipTime06PM = findViewById(R.id.chipTime06PM);
        chipTime08PM = findViewById(R.id.chipTime08PM);

        confirmButton = findViewById(R.id.confirmButton);

        if (btnBackBooking != null) {
            btnBackBooking.setOnClickListener(v -> finish());
        }
    }

    private void setupServiceData() {
        String serviceName = getIntent().getStringExtra("serviceName");
        if (serviceName == null || serviceName.trim().isEmpty()) {
            serviceName = ServicesActivity.SERVICE_SPA;
        }

        // Configure authentic details based on selected service
        if (serviceName.equalsIgnoreCase(ServicesActivity.SERVICE_SPA) || serviceName.toLowerCase().contains("spa")) {
            bookingHeaderTitle.setText("Book Spa Service");
            serviceNameTextView.setText("LuxeVista Spa");
            servicePriceTextView.setText("Rs. 5,000");
            serviceSubtitleTextView.setText("Signature Massage");
            tvServiceDuration.setText("60 Minutes");
            tvServiceBenefit.setText("Full Body Relaxation");
            bookingHeroImage.setImageResource(R.drawable.spa_treatments);
        } else if (serviceName.equalsIgnoreCase(ServicesActivity.SERVICE_DINING) || serviceName.toLowerCase().contains("dining")) {
            bookingHeaderTitle.setText("Reserve Dining Table");
            serviceNameTextView.setText("Ocean Breeze Restaurant");
            servicePriceTextView.setText("Rs. 8,500");
            serviceSubtitleTextView.setText("Chef's Five-Course Tasting Menu");
            tvServiceDuration.setText("90 Minutes");
            tvServiceBenefit.setText("Michelin-Star Vistas");
            bookingHeroImage.setImageResource(R.drawable.fine_dining);
        } else if (serviceName.equalsIgnoreCase(ServicesActivity.SERVICE_CABANAS) || serviceName.toLowerCase().contains("cabana")) {
            bookingHeaderTitle.setText("Book Private Cabana");
            serviceNameTextView.setText("Poolside Cabana Experience");
            servicePriceTextView.setText("Rs. 15,000");
            serviceSubtitleTextView.setText("VIP Butler & Oceanfront Daybed");
            tvServiceDuration.setText("Full Day");
            tvServiceBenefit.setText("Private Poolside Bar");
            bookingHeroImage.setImageResource(R.drawable.poolside_cabanas);
        } else {
            bookingHeaderTitle.setText("Book Island Tour");
            serviceNameTextView.setText("Guided Beach & Island Tour");
            servicePriceTextView.setText("Rs. 6,000");
            serviceSubtitleTextView.setText("Private Coastal Excursion");
            tvServiceDuration.setText("120 Minutes");
            tvServiceBenefit.setText("Pristine Reef Snorkeling");
            bookingHeroImage.setImageResource(R.drawable.beach_tours);
        }

        // Pre-fill user name from active session if available
        String savedUserName = sessionManager.getUserName();
        if (savedUserName != null && !savedUserName.isEmpty() && !savedUserName.equals("Valued Guest")) {
            nameEditText.setText(savedUserName);
        }

        // Set default date to today or tomorrow
        Calendar cal = Calendar.getInstance();
        dateEditText.setText(displayDateFormat.format(cal.getTime()));
    }

    private void setupDatePicker() {
        View.OnClickListener dateClick = v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    BookingActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(selectedYear, selectedMonth, selectedDay);
                        dateEditText.setText(displayDateFormat.format(chosen.getTime()));
                    },
                    year, month, day);

            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        };

        if (datePickerContainer != null) {
            datePickerContainer.setOnClickListener(dateClick);
        }
        if (dateEditText != null) {
            dateEditText.setOnClickListener(dateClick);
        }
    }

    private void setupTimeSlotChips() {
        chipTime10AM.setOnClickListener(v -> selectTimeSlot("10:00 AM", chipTime10AM));
        chipTime12PM.setOnClickListener(v -> selectTimeSlot("12:00 PM", chipTime12PM));
        chipTime02PM.setOnClickListener(v -> selectTimeSlot("02:00 PM", chipTime02PM));
        chipTime04PM.setOnClickListener(v -> selectTimeSlot("04:00 PM", chipTime04PM));
        chipTime06PM.setOnClickListener(v -> selectTimeSlot("06:00 PM", chipTime06PM));
        chipTime08PM.setOnClickListener(v -> selectTimeSlot("08:00 PM", chipTime08PM));

        // Default selection matching mockup (12:00 PM)
        selectTimeSlot("12:00 PM", chipTime12PM);
    }

    private void selectTimeSlot(String timeStr, TextView selectedChip) {
        selectedTimeSlot = timeStr;
        if (timeEditText != null) {
            timeEditText.setText(timeStr);
        }

        int unselectedTextColor = ContextCompat.getColor(this, R.color.text_primary);
        int selectedTextColor = ContextCompat.getColor(this, R.color.resort_navy_primary);

        resetChip(chipTime10AM, unselectedTextColor);
        resetChip(chipTime12PM, unselectedTextColor);
        resetChip(chipTime02PM, unselectedTextColor);
        resetChip(chipTime04PM, unselectedTextColor);
        resetChip(chipTime06PM, unselectedTextColor);
        resetChip(chipTime08PM, unselectedTextColor);

        if (selectedChip != null) {
            selectedChip.setBackgroundResource(R.drawable.chip_time_selected);
            selectedChip.setTextColor(selectedTextColor);
        }
    }

    private void resetChip(TextView chip, int textColor) {
        if (chip != null) {
            chip.setBackgroundResource(R.drawable.chip_time_unselected);
            chip.setTextColor(textColor);
        }
    }

    private void setupConfirmationAction() {
        confirmButton.setOnClickListener(view -> {
            String customerName = nameEditText.getText().toString().trim();
            String date = dateEditText.getText().toString().trim();
            String time = selectedTimeSlot;

            if (customerName.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(BookingActivity.this, "Please fill in all booking details", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!SecurityUtils.isDateNotPast(date)) {
                Toast.makeText(BookingActivity.this, "Booking date cannot be in the past", Toast.LENGTH_SHORT).show();
                return;
            }

            confirmButton.setEnabled(false);

            String resolvedServiceName = serviceNameTextView.getText().toString().trim();
            DBHelper dbHelper = DBHelper.getInstance(BookingActivity.this);
            String userEmail = sessionManager.getUserEmail();

            boolean isInserted = dbHelper.addServiceBooking(resolvedServiceName, customerName, date, time, userEmail);

            if (isInserted) {
                Toast.makeText(BookingActivity.this, "Service Reservation Confirmed for " + customerName, Toast.LENGTH_LONG).show();
                finish();
            } else {
                confirmButton.setEnabled(true);
                Toast.makeText(BookingActivity.this, "Failed to save reservation. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
