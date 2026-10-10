package com.example.luxevistaresortapp;

import java.util.Locale;

/**
 * Immutable typed data model representing a persisted service appointment
 * in the local SQLite database.
 */
public class ServiceBookingModel {

    private final long id;
    private final String serviceName;
    private final String customerName;
    private final String date;
    private final String time;
    private final String userEmail;

    public ServiceBookingModel(long id, String serviceName, String customerName,
                               String date, String time, String userEmail) {
        this.id = id;
        this.serviceName = serviceName != null ? serviceName.trim() : "";
        this.customerName = customerName != null ? customerName.trim() : "";
        this.date = date != null ? date.trim() : "";
        this.time = time != null ? time.trim() : "";
        this.userEmail = userEmail != null ? userEmail.trim().toLowerCase(Locale.US) : "";
    }

    public long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Formats a standardized reference code derived strictly from the SQLite row ID.
     */
    public String getFormattedReference() {
        return String.format(Locale.US, "SRV-%06d", id);
    }
}
