package com.example.luxevistaresortapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ViewBookingsActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    private ListView bookingsListView;
    private LinearLayout emptyStateContainer;
    private ImageView btnBackHistory;
    private Button btnEmptyBookNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_view_bookings);

        EdgeToEdgeHelper.applyTopInset(findViewById(R.id.bookingsHeaderBar));
        EdgeToEdgeHelper.applyBottomInset(findViewById(R.id.bookingsListView));

        dbHelper = DBHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();
    }

    private void initViews() {
        bookingsListView = findViewById(R.id.bookingsListView);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        btnBackHistory = findViewById(R.id.btnBackHistory);
        btnEmptyBookNow = findViewById(R.id.btnEmptyBookNow);
    }

    private void setupListeners() {
        if (btnBackHistory != null) {
            btnBackHistory.setOnClickListener(v -> finish());
        }

        if (btnEmptyBookNow != null) {
            btnEmptyBookNow.setOnClickListener(v -> {
                Intent intent = new Intent(ViewBookingsActivity.this, RoomBookingActivity.class);
                startActivity(intent);
            });
        }
    }

    private void loadBookings() {
        String userEmail = sessionManager.getUserEmail();
        if (userEmail == null || userEmail.trim().isEmpty()) {
            showEmptyState(true);
            return;
        }

        List<BookingModel> bookings = dbHelper.getBookingListForUser(userEmail);

        if (bookings == null || bookings.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
            BookingAdapter adapter = new BookingAdapter(this, bookings);
            bookingsListView.setAdapter(adapter);
        }
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
            if (bookingsListView != null) bookingsListView.setVisibility(View.GONE);
        } else {
            if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
            if (bookingsListView != null) bookingsListView.setVisibility(View.VISIBLE);
        }
    }

    private void openBookingDetails(long bookingId) {
        Intent intent = new Intent(ViewBookingsActivity.this, BookingSuccessActivity.class);
        intent.putExtra(BookingSuccessActivity.EXTRA_BOOKING_ID, bookingId);
        startActivity(intent);
    }

    private void modifyBooking(BookingModel booking) {
        if (booking == null) return;
        if (booking.isCancelled()) {
            Toast.makeText(this, "Cannot modify a cancelled reservation.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(ViewBookingsActivity.this, RoomBookingActivity.class);
        intent.putExtra(RoomBookingActivity.EXTRA_EDIT_BOOKING_ID, booking.getId());
        startActivity(intent);
    }

    private void promptCancelBooking(BookingModel booking) {
        if (booking == null || booking.isCancelled()) return;

        String ref = (booking.getBookingReference() != null && !booking.getBookingReference().trim().isEmpty())
                ? booking.getBookingReference()
                : ("ID #" + booking.getId());

        new AlertDialog.Builder(this)
                .setTitle("Cancel Reservation")
                .setMessage("Are you sure you want to cancel reservation " + ref + "?\n\n"
                        + "Notice: This action updates your local reservation record on this device only. "
                        + "It does not process or cancel any booking in a centralized resort system.")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    String userEmail = sessionManager.getUserEmail();
                    boolean cancelled = dbHelper.cancelBookingWithOwner(booking.getId(), userEmail);
                    if (cancelled) {
                        Toast.makeText(ViewBookingsActivity.this, "Reservation " + ref + " has been cancelled.", Toast.LENGTH_SHORT).show();
                        loadBookings();
                    } else {
                        Toast.makeText(ViewBookingsActivity.this, "Unable to cancel reservation. Please check authorization or try again.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Keep Reservation", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private class BookingAdapter extends ArrayAdapter<BookingModel> {

        public BookingAdapter(Context context, List<BookingModel> items) {
            super(context, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.booking_list_item, parent, false);
            }

            BookingModel booking = getItem(position);
            if (booking == null) return convertView;

            TextView tvItemReference = convertView.findViewById(R.id.tvItemReference);
            TextView tvItemStatus = convertView.findViewById(R.id.tvItemStatus);
            TextView roomTypeText = convertView.findViewById(R.id.roomTypeText);
            TextView priceText = convertView.findViewById(R.id.priceText);
            TextView checkInText = convertView.findViewById(R.id.checkInText);
            TextView checkOutText = convertView.findViewById(R.id.checkOutText);
            TextView tvItemDurationGuests = convertView.findViewById(R.id.tvItemDurationGuests);

            Button btnItemModify = convertView.findViewById(R.id.btnItemModify);
            Button btnItemCancel = convertView.findViewById(R.id.btnItemCancel);
            Button btnItemVoucher = convertView.findViewById(R.id.btnItemVoucher);

            // Reference code
            String ref = (booking.getBookingReference() != null && !booking.getBookingReference().trim().isEmpty())
                    ? booking.getBookingReference()
                    : ("LVR-LEGACY-" + booking.getId());
            tvItemReference.setText(ref);

            // Room Type
            String roomType = booking.getRoomType() != null && !booking.getRoomType().trim().isEmpty()
                    ? booking.getRoomType()
                    : "Suite Reservation";
            roomTypeText.setText(roomType);

            // Dates
            checkInText.setText("Check-In: " + (booking.getCheckIn() != null && !booking.getCheckIn().trim().isEmpty() ? booking.getCheckIn() : "—"));
            checkOutText.setText("Check-Out: " + (booking.getCheckOut() != null && !booking.getCheckOut().trim().isEmpty() ? booking.getCheckOut() : "—"));

            // Price formatting
            priceText.setText(formatPrice(booking.getPrice()));

            // Duration & Guests (Legacy compatibility)
            String nightsText;
            if (booking.getNights() > 0) {
                nightsText = booking.getNights() + (booking.getNights() == 1 ? " Night" : " Nights");
            } else {
                nightsText = "Duration unavailable";
            }

            String guestsText;
            if (booking.getAdults() > 0) {
                guestsText = booking.getAdults() + " Adult" + (booking.getAdults() == 1 ? "" : "s") +
                        (booking.getChildren() > 0 ? ", " + booking.getChildren() + " Child" + (booking.getChildren() == 1 ? "" : "ren") : "");
            } else {
                guestsText = "Guests unavailable";
            }

            tvItemDurationGuests.setText(nightsText + " • " + guestsText);

            // Status Badge and Action Buttons
            boolean isCancelled = booking.isCancelled();
            if (isCancelled) {
                tvItemStatus.setText("CANCELLED");
                tvItemStatus.setBackgroundResource(R.drawable.status_badge_cancelled);
                tvItemStatus.setTextColor(0xFFD32F2F);

                btnItemModify.setEnabled(false);
                btnItemModify.setAlpha(0.35f);

                btnItemCancel.setEnabled(false);
                btnItemCancel.setAlpha(0.35f);
            } else {
                tvItemStatus.setText("RESERVED");
                tvItemStatus.setBackgroundResource(R.drawable.status_badge_reserved);
                tvItemStatus.setTextColor(0xFF2E7D32);

                btnItemModify.setEnabled(true);
                btnItemModify.setAlpha(1.0f);

                btnItemCancel.setEnabled(true);
                btnItemCancel.setAlpha(1.0f);
            }

            // Click listeners
            btnItemVoucher.setOnClickListener(v -> openBookingDetails(booking.getId()));
            convertView.setOnClickListener(v -> openBookingDetails(booking.getId()));

            btnItemModify.setOnClickListener(v -> {
                if (!isCancelled) {
                    modifyBooking(booking);
                }
            });

            btnItemCancel.setOnClickListener(v -> {
                if (!isCancelled) {
                    promptCancelBooking(booking);
                }
            });

            return convertView;
        }
    }

    private String formatPrice(String rawPrice) {
        if (rawPrice == null || rawPrice.trim().isEmpty()) {
            return "Price on Request";
        }
        if (rawPrice.startsWith("Rs.")) {
            return rawPrice;
        }
        try {
            String digits = rawPrice.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                long amount = Long.parseLong(digits);
                return "Rs. " + NumberFormat.getNumberInstance(Locale.US).format(amount);
            }
        } catch (Exception ignored) {
        }
        return rawPrice;
    }
}
