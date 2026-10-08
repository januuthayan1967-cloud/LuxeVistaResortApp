package com.example.luxevistaresortapp;

import android.util.Patterns;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class SecurityUtils {

    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("uuuu-MM-dd"),
            DateTimeFormatter.ofPattern("d-M-uuuu")
    };

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : saltBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String hashPassword(String password, String salt) {
        if (password == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String input = (salt != null ? salt : "") + password;
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(password.hashCode());
        }
    }

    public static boolean isValidEmail(CharSequence email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static LocalDate parseLocalDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        String trimmed = dateStr.trim();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    public static boolean isCheckOutAfterCheckIn(String checkInStr, String checkOutStr) {
        LocalDate in = parseLocalDate(checkInStr);
        LocalDate out = parseLocalDate(checkOutStr);
        if (in == null || out == null) return false;
        return out.isAfter(in);
    }

    public static boolean isDateNotPast(String dateStr) {
        LocalDate date = parseLocalDate(dateStr);
        if (date == null) return false;
        LocalDate today = LocalDate.now();
        return !date.isBefore(today);
    }

    /**
     * Calculates reservation stay nights using discrete LocalDate calendar differences
     * via ChronoUnit.DAYS.between(). Handles leap years, month lengths, and avoids DST drift.
     */
    public static int calculateStayNights(String checkInStr, String checkOutStr) {
        LocalDate in = parseLocalDate(checkInStr);
        LocalDate out = parseLocalDate(checkOutStr);
        if (in == null || out == null || !out.isAfter(in)) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(in, out);
        return (int) Math.max(1, days);
    }
}
