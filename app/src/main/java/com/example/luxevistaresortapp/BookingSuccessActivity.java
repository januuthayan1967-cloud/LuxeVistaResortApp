package com.example.luxevistaresortapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

public class BookingSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "bookingId";

    private TextView tvVoucherReference;
    private TextView tvVoucherRoomType;
    private TextView tvVoucherNightlyRate;
    private TextView tvVoucherCheckIn;
    private TextView tvVoucherCheckOut;
    private TextView tvVoucherNights;
    private TextView tvVoucherGuests;
    private TextView tvVoucherTotal;
    private TextView tvVoucherStatus;

    private Button btnShareVoucher;
    private Button btnShareText;
    private Button btnViewBookings;
    private Button btnReturnHome;

    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private BookingModel currentBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_booking_success);

        EdgeToEdgeHelper.applyRootWindowInsets(findViewById(R.id.bookingSuccessScrollView));

        dbHelper = DBHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        loadVerifiedReservation();
        setupActionListeners();
    }

    private void initViews() {
        tvVoucherReference = findViewById(R.id.tvVoucherReference);
        tvVoucherRoomType = findViewById(R.id.tvVoucherRoomType);
        tvVoucherNightlyRate = findViewById(R.id.tvVoucherNightlyRate);
        tvVoucherCheckIn = findViewById(R.id.tvVoucherCheckIn);
        tvVoucherCheckOut = findViewById(R.id.tvVoucherCheckOut);
        tvVoucherNights = findViewById(R.id.tvVoucherNights);
        tvVoucherGuests = findViewById(R.id.tvVoucherGuests);
        tvVoucherTotal = findViewById(R.id.tvVoucherTotal);
        tvVoucherStatus = findViewById(R.id.tvVoucherStatus);

        btnShareVoucher = findViewById(R.id.btnShareVoucher);
        btnShareText = findViewById(R.id.btnShareText);
        btnViewBookings = findViewById(R.id.btnViewBookings);
        btnReturnHome = findViewById(R.id.btnReturnHome);
    }

    /**
     * Retrieves the persisted reservation strictly from SQLite and validates
     * row-level ownership against the currently authenticated user.
     */
    private void loadVerifiedReservation() {
        long bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1);
        String currentUserEmail = sessionManager.getUserEmail();

        if (bookingId <= 0 || currentUserEmail == null || currentUserEmail.trim().isEmpty()) {
            Toast.makeText(this, "Reservation not found or unauthorized access.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Row-level ownership verified fetch
        currentBooking = dbHelper.getBookingModelById(bookingId, currentUserEmail);

        if (currentBooking == null) {
            Toast.makeText(this, "Unable to load reservation details for this account.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayReservationDetails(currentBooking);
    }

    private void displayReservationDetails(BookingModel booking) {
        tvVoucherReference.setText(booking.getBookingReference() != null ? booking.getBookingReference() : "LVR-PENDING");
        tvVoucherRoomType.setText(booking.getRoomType());

        int nights = booking.getNights() > 0 ? booking.getNights() : 1;
        long totalAmount = parseNumericPrice(booking.getPrice());
        long nightlyRate = nights > 0 ? (totalAmount / nights) : totalAmount;

        tvVoucherNightlyRate.setText("Nightly Rate: " + formatCurrency(nightlyRate) + " / night");
        tvVoucherCheckIn.setText(booking.getCheckIn());
        tvVoucherCheckOut.setText(booking.getCheckOut());
        tvVoucherNights.setText(nights + (nights == 1 ? " Night" : " Nights"));

        int adults = booking.getAdults() > 0 ? booking.getAdults() : 1;
        int children = booking.getChildren();
        String guestsText = adults + " Adult" + (adults == 1 ? "" : "s") +
                (children > 0 ? ", " + children + " Child" + (children == 1 ? "" : "ren") : "");
        tvVoucherGuests.setText(guestsText);

        tvVoucherTotal.setText(formatCurrency(totalAmount));

        if (booking.isCancelled()) {
            tvVoucherStatus.setText("CANCELLED • Saved Locally");
            tvVoucherStatus.setTextColor(0xFFD32F2F);
        } else {
            tvVoucherStatus.setText("RESERVED • Saved Locally");
            tvVoucherStatus.setTextColor(0xFF2E7D32);
        }
    }

    private void setupActionListeners() {
        btnShareVoucher.setOnClickListener(v -> shareReservationPdf());

        if (btnShareText != null) {
            btnShareText.setOnClickListener(v -> shareReservationSummary());
        }

        btnViewBookings.setOnClickListener(v -> {
            Intent intent = new Intent(BookingSuccessActivity.this, ViewBookingsActivity.class);
            startActivity(intent);
            finish();
        });

        btnReturnHome.setOnClickListener(v -> navigateToHome());
    }

    private void shareReservationPdf() {
        if (currentBooking == null) return;
        PdfVoucherGenerator.shareVerifiedBookingPdf(BookingSuccessActivity.this, currentBooking.getId());
    }

    private void shareReservationSummary() {
        if (currentBooking == null) return;

        String summary = generateShareableText(currentBooking);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "LuxeVista Resort Reservation Voucher - " + currentBooking.getBookingReference());
        shareIntent.putExtra(Intent.EXTRA_TEXT, summary);

        startActivity(Intent.createChooser(shareIntent, "Share Reservation Summary"));
    }

    public static String generateShareableText(BookingModel booking) {
        if (booking == null) return "";
        int nights = booking.getNights() > 0 ? booking.getNights() : 1;
        long totalAmount = parseNumericPrice(booking.getPrice());

        return "════════════════════════════════════════\n" +
                "       LUXEVISTA RESORT & SPA           \n" +
                "    Digital Reservation Voucher         \n" +
                "════════════════════════════════════════\n" +
                "Reference   : " + booking.getBookingReference() + "\n" +
                "Guest Email : " + booking.getUserEmail() + "\n" +
                "Suite Type  : " + booking.getRoomType() + "\n" +
                "Check-In    : " + booking.getCheckIn() + "\n" +
                "Check-Out   : " + booking.getCheckOut() + "\n" +
                "Duration    : " + nights + " Night(s)\n" +
                "Occupancy   : " + booking.getAdults() + " Adult(s), " + booking.getChildren() + " Child(ren)\n" +
                "Subtotal    : Rs. " + NumberFormat.getNumberInstance(Locale.US).format(totalAmount) + "\n" +
                "Status      : " + booking.getStatus() + " (Saved Locally)\n" +
                "════════════════════════════════════════\n" +
                "Notice: This reservation is saved locally on this device\n" +
                "and has not been confirmed by a centralized hotel system.\n" +
                "LuxeVista Resort • Coastal Elegance & Bespoke Luxury\n";
    }

    private void navigateToHome() {
        Intent intent = new Intent(BookingSuccessActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Return cleanly to Home without reopening the booking form
        navigateToHome();
    }

    public static long parseNumericPrice(String priceText) {
        if (priceText == null) return 0L;
        String digits = priceText.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0L;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String formatCurrency(long amount) {
        return "Rs. " + NumberFormat.getNumberInstance(Locale.US).format(amount);
    }
}
