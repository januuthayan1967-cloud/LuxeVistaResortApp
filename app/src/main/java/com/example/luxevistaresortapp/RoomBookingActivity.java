package com.example.luxevistaresortapp;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class RoomBookingActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_BOOKING_ID = "editBookingId";
    private long editBookingId = -1;
    private BookingModel originalBookingToEdit;
    private TextView tvBookingHeaderTitle, tvBookingHeaderSubtitle;

    private Spinner roomTypeSpinner;
    private EditText checkInDate, checkOutDate;
    private TextView roomPrice; // Retained for backward compatibility
    private Button bookNowButton;

    // Guest steppers
    private Button btnAdultsMinus, btnAdultsPlus;
    private TextView tvAdultsCount;
    private Button btnChildrenMinus, btnChildrenPlus;
    private TextView tvChildrenCount;

    // Stay breakdown summary views
    private TextView tvSummaryRoom, tvSummaryRate, tvSummaryNights, tvSummarySubtotal, tvDatePrompt;

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    private int adultsCount = 1;
    private int childrenCount = 0;
    private static final int MIN_ADULTS = 1;
    private static final int MAX_ADULTS = 10;
    private static final int MIN_CHILDREN = 0;
    private static final int MAX_CHILDREN = 10;

    private int calculatedNights = 0;
    private long calculatedSubtotal = 0L;

    // Room rates catalog (verified project prices)
    private static final Map<String, Long> ROOM_RATES = new LinkedHashMap<>();
    static {
        ROOM_RATES.put("Ocean View Suite", 25000L);
        ROOM_RATES.put("Deluxe Room", 18000L);
        ROOM_RATES.put("Single Room", 12000L);
        ROOM_RATES.put("Family Room", 30000L);
    }

    private final String[] roomTypes = ROOM_RATES.keySet().toArray(new String[0]);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_booking);

        dbHelper = DBHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupRoomSpinner();
        setupDatePickers();
        setupGuestSteppers();
        setupBookingSubmission();

        checkAndApplyEditMode();

        // Initial live pricing calculation
        updatePricingBreakdown();
    }

    private void initViews() {
        tvBookingHeaderTitle = findViewById(R.id.tvBookingHeaderTitle);
        tvBookingHeaderSubtitle = findViewById(R.id.tvBookingHeaderSubtitle);

        roomTypeSpinner = findViewById(R.id.roomTypeSpinner);
        checkInDate = findViewById(R.id.checkInDate);
        checkOutDate = findViewById(R.id.checkOutDate);
        roomPrice = findViewById(R.id.roomPrice);
        bookNowButton = findViewById(R.id.bookNowButton);

        btnAdultsMinus = findViewById(R.id.btnAdultsMinus);
        btnAdultsPlus = findViewById(R.id.btnAdultsPlus);
        tvAdultsCount = findViewById(R.id.tvAdultsCount);

        btnChildrenMinus = findViewById(R.id.btnChildrenMinus);
        btnChildrenPlus = findViewById(R.id.btnChildrenPlus);
        tvChildrenCount = findViewById(R.id.tvChildrenCount);

        tvSummaryRoom = findViewById(R.id.tvSummaryRoom);
        tvSummaryRate = findViewById(R.id.tvSummaryRate);
        tvSummaryNights = findViewById(R.id.tvSummaryNights);
        tvSummarySubtotal = findViewById(R.id.tvSummarySubtotal);
        tvDatePrompt = findViewById(R.id.tvDatePrompt);
    }

    private void checkAndApplyEditMode() {
        editBookingId = getIntent().getLongExtra(EXTRA_EDIT_BOOKING_ID, -1);
        if (editBookingId > 0) {
            String currentUserEmail = sessionManager.getUserEmail();
            originalBookingToEdit = dbHelper.getBookingModelById(editBookingId, currentUserEmail);

            if (originalBookingToEdit == null) {
                Toast.makeText(this, "Reservation not found or unauthorized access.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (originalBookingToEdit.isCancelled()) {
                Toast.makeText(this, "Cannot modify a cancelled reservation.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Apply Edit Mode UI styling
            if (tvBookingHeaderTitle != null) {
                tvBookingHeaderTitle.setText("Modify Reservation");
            }
            if (tvBookingHeaderSubtitle != null) {
                tvBookingHeaderSubtitle.setText("Updating " + originalBookingToEdit.getBookingReference());
            }
            bookNowButton.setText("Save Changes");

            // Pre-populate room spinner
            for (int i = 0; i < roomTypes.length; i++) {
                if (roomTypes[i].equalsIgnoreCase(originalBookingToEdit.getRoomType().trim())) {
                    roomTypeSpinner.setSelection(i);
                    break;
                }
            }

            // Pre-populate dates
            checkInDate.setText(originalBookingToEdit.getCheckIn());
            checkOutDate.setText(originalBookingToEdit.getCheckOut());

            // Pre-populate guest counts
            adultsCount = originalBookingToEdit.getAdults() > 0 ? originalBookingToEdit.getAdults() : 1;
            childrenCount = Math.max(0, originalBookingToEdit.getChildren());
            tvAdultsCount.setText(String.valueOf(adultsCount));
            tvChildrenCount.setText(String.valueOf(childrenCount));
            updateStepperButtonStates();
        }
    }

    private void setupRoomSpinner() {
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

        roomTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updatePricingBreakdown();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                updatePricingBreakdown();
            }
        });
    }

    private void setupDatePickers() {
        checkInDate.setOnClickListener(v -> showDatePickerDialog(checkInDate));
        checkOutDate.setOnClickListener(v -> showDatePickerDialog(checkOutDate));
    }

    private void setupGuestSteppers() {
        tvAdultsCount.setText(String.valueOf(adultsCount));
        tvChildrenCount.setText(String.valueOf(childrenCount));
        updateStepperButtonStates();

        btnAdultsMinus.setOnClickListener(v -> {
            if (adultsCount > MIN_ADULTS) {
                adultsCount--;
                tvAdultsCount.setText(String.valueOf(adultsCount));
                updateStepperButtonStates();
                updatePricingBreakdown();
            }
        });

        btnAdultsPlus.setOnClickListener(v -> {
            if (adultsCount < MAX_ADULTS) {
                adultsCount++;
                tvAdultsCount.setText(String.valueOf(adultsCount));
                updateStepperButtonStates();
                updatePricingBreakdown();
            }
        });

        btnChildrenMinus.setOnClickListener(v -> {
            if (childrenCount > MIN_CHILDREN) {
                childrenCount--;
                tvChildrenCount.setText(String.valueOf(childrenCount));
                updateStepperButtonStates();
                updatePricingBreakdown();
            }
        });

        btnChildrenPlus.setOnClickListener(v -> {
            if (childrenCount < MAX_CHILDREN) {
                childrenCount++;
                tvChildrenCount.setText(String.valueOf(childrenCount));
                updateStepperButtonStates();
                updatePricingBreakdown();
            }
        });
    }

    private void updateStepperButtonStates() {
        btnAdultsMinus.setEnabled(adultsCount > MIN_ADULTS);
        btnAdultsPlus.setEnabled(adultsCount < MAX_ADULTS);
        btnChildrenMinus.setEnabled(childrenCount > MIN_CHILDREN);
        btnChildrenPlus.setEnabled(childrenCount < MAX_CHILDREN);
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
                    updatePricingBreakdown();
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    /**
     * Performs live recalculation of stay duration and price breakdown
     * whenever room selection or dates change.
     */
    private void updatePricingBreakdown() {
        String selectedRoom = roomTypeSpinner.getSelectedItem() != null
                ? roomTypeSpinner.getSelectedItem().toString()
                : roomTypes[0];

        long nightlyRate = getNightlyRate(selectedRoom);
        tvSummaryRoom.setText(selectedRoom);
        tvSummaryRate.setText(formatCurrency(nightlyRate) + " / night");

        String checkIn = checkInDate.getText().toString().trim();
        String checkOut = checkOutDate.getText().toString().trim();

        if (checkIn.isEmpty() || checkOut.isEmpty()) {
            calculatedNights = 0;
            calculatedSubtotal = 0L;
            tvSummaryNights.setText("Select dates");
            tvSummarySubtotal.setText("—");
            tvDatePrompt.setText("Select check-in & check-out dates to calculate total");
            tvDatePrompt.setTextColor(0xFF9EABB8);
            if (roomPrice != null) {
                roomPrice.setText(formatCurrency(nightlyRate) + " per night");
            }
            return;
        }

        LocalDate in = SecurityUtils.parseLocalDate(checkIn);
        LocalDate out = SecurityUtils.parseLocalDate(checkOut);

        if (in == null || out == null) {
            calculatedNights = 0;
            calculatedSubtotal = 0L;
            tvSummaryNights.setText("Invalid date format");
            tvSummarySubtotal.setText("—");
            tvDatePrompt.setText("Please select valid dates");
            tvDatePrompt.setTextColor(0xFFE57373);
            return;
        }

        if (!SecurityUtils.isDateNotPast(checkIn)) {
            calculatedNights = 0;
            calculatedSubtotal = 0L;
            tvSummaryNights.setText("Past Date");
            tvSummarySubtotal.setText("—");
            tvDatePrompt.setText("Check-in date cannot be in the past");
            tvDatePrompt.setTextColor(0xFFE57373);
            return;
        }

        if (!out.isAfter(in)) {
            calculatedNights = 0;
            calculatedSubtotal = 0L;
            tvSummaryNights.setText("Invalid Range");
            tvSummarySubtotal.setText("—");
            tvDatePrompt.setText("Check-out date must be strictly after check-in date");
            tvDatePrompt.setTextColor(0xFFE57373);
            return;
        }

        // Calculate discrete calendar difference
        int nights = SecurityUtils.calculateStayNights(checkIn, checkOut);
        if (nights <= 0) {
            calculatedNights = 0;
            calculatedSubtotal = 0L;
            tvSummaryNights.setText("0 Nights");
            tvSummarySubtotal.setText("—");
            tvDatePrompt.setText("Stay duration must be at least 1 night");
            tvDatePrompt.setTextColor(0xFFE57373);
            return;
        }

        calculatedNights = nights;
        try {
            calculatedSubtotal = Math.multiplyExact((long) nights, nightlyRate);
        } catch (ArithmeticException e) {
            calculatedSubtotal = Long.MAX_VALUE;
        }

        tvSummaryNights.setText(nights + (nights == 1 ? " Night" : " Nights"));
        tvSummarySubtotal.setText(formatCurrency(calculatedSubtotal));
        tvDatePrompt.setText("✓ " + nights + "-night stay selected • " + adultsCount + " Adult(s), " + childrenCount + " Child(ren)");
        tvDatePrompt.setTextColor(0xFF81C784); // Soft success green

        if (roomPrice != null) {
            roomPrice.setText(formatCurrency(calculatedSubtotal));
        }
    }

    private void setupBookingSubmission() {
        bookNowButton.setOnClickListener(v -> {
            String roomType = roomTypeSpinner.getSelectedItem().toString();
            String checkIn = checkInDate.getText().toString().trim();
            String checkOut = checkOutDate.getText().toString().trim();

            if (checkIn.isEmpty() || checkOut.isEmpty()) {
                Toast.makeText(this, "Please select both Check-In and Check-Out dates", Toast.LENGTH_SHORT).show();
                return;
            }

            if (editBookingId > 0) {
                boolean checkInUnchanged = originalBookingToEdit != null && checkIn.equals(originalBookingToEdit.getCheckIn());
                if (!checkInUnchanged && !SecurityUtils.isDateNotPast(checkIn)) {
                    Toast.makeText(this, "Check-In date cannot be in the past", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (!SecurityUtils.isDateNotPast(checkIn)) {
                    Toast.makeText(this, "Check-In date cannot be in the past", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (!SecurityUtils.isCheckOutAfterCheckIn(checkIn, checkOut)) {
                Toast.makeText(this, "Check-Out date must be strictly after Check-In date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (calculatedNights <= 0 || calculatedSubtotal <= 0) {
                Toast.makeText(this, "Invalid stay duration. At least 1 night is required.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prevent rapid duplicate clicks
            bookNowButton.setEnabled(false);

            String userEmail = sessionManager.getUserEmail();
            String priceToPersist = String.valueOf(calculatedSubtotal);

            if (editBookingId > 0) {
                // Modification flow: update existing reservation
                boolean modified = dbHelper.modifyBookingWithOwner(
                        editBookingId,
                        userEmail,
                        roomType,
                        checkIn,
                        checkOut,
                        priceToPersist,
                        calculatedNights,
                        adultsCount,
                        childrenCount
                );

                if (modified) {
                    Toast.makeText(RoomBookingActivity.this, "Reservation updated successfully!", Toast.LENGTH_SHORT).show();
                    Intent successIntent = new Intent(RoomBookingActivity.this, BookingSuccessActivity.class);
                    successIntent.putExtra(BookingSuccessActivity.EXTRA_BOOKING_ID, editBookingId);
                    startActivity(successIntent);
                    finish();
                } else {
                    bookNowButton.setEnabled(true);
                    Toast.makeText(RoomBookingActivity.this,
                            "Failed to update reservation. Please ensure dates do not overlap another booking or contact reception.",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }

            // Execute atomic transaction from Step 1
            long bookingId = dbHelper.addBookingAtomic(
                    roomType,
                    checkIn,
                    checkOut,
                    priceToPersist,
                    userEmail,
                    calculatedNights,
                    adultsCount,
                    childrenCount
            );

            if (bookingId > 0) {
                Intent successIntent = new Intent(RoomBookingActivity.this, BookingSuccessActivity.class);
                successIntent.putExtra(BookingSuccessActivity.EXTRA_BOOKING_ID, bookingId);
                startActivity(successIntent);
                clearFields();
                finish();
            } else if (bookingId == -2) {
                bookNowButton.setEnabled(true);
                Toast.makeText(RoomBookingActivity.this,
                        "Duplicate Reservation Detected!\nYou already have an active booking for this suite and dates.",
                        Toast.LENGTH_LONG).show();
            } else {
                bookNowButton.setEnabled(true);
                Toast.makeText(RoomBookingActivity.this,
                        "Booking failed. Please try again or contact resort reception.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private long getNightlyRate(String roomType) {
        if (roomType == null) return 25000L;
        Long rate = ROOM_RATES.get(roomType.trim());
        return rate != null ? rate : 25000L;
    }

    private String formatCurrency(long amount) {
        return "Rs. " + NumberFormat.getNumberInstance(Locale.US).format(amount);
    }

    private void clearFields() {
        checkInDate.setText("");
        checkOutDate.setText("");
        roomTypeSpinner.setSelection(0);
        adultsCount = 1;
        childrenCount = 0;
        tvAdultsCount.setText("1");
        tvChildrenCount.setText("0");
        updateStepperButtonStates();
        updatePricingBreakdown();
        bookNowButton.setEnabled(true);
    }

    public String extractNumericPrice(String priceText) {
        if (priceText == null) return "0";
        return priceText.replaceAll("[^0-9]", "");
    }
}
