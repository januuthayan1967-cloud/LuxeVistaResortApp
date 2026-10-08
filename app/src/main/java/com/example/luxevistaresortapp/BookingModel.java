package com.example.luxevistaresortapp;

public class BookingModel {
    private final long id;
    private final String roomType;
    private final String checkIn;
    private final String checkOut;
    private final String price;
    private final String userEmail;
    private final String bookingReference;
    private final int nights;
    private final int adults;
    private final int children;
    private final String status;
    private final String createdAt;

    public BookingModel(long id, String roomType, String checkIn, String checkOut,
                        String price, String userEmail, String bookingReference,
                        int nights, int adults, int children, String status, String createdAt) {
        this.id = id;
        this.roomType = roomType;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.price = price;
        this.userEmail = userEmail;
        this.bookingReference = bookingReference;
        this.nights = nights;
        this.adults = adults;
        this.children = children;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getRoomType() { return roomType; }
    public String getCheckIn() { return checkIn; }
    public String getCheckOut() { return checkOut; }
    public String getPrice() { return price; }
    public String getUserEmail() { return userEmail; }
    public String getBookingReference() { return bookingReference; }
    public int getNights() { return nights; }
    public int getAdults() { return adults; }
    public int getChildren() { return children; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }
}
