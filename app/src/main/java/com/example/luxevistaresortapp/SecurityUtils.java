package com.example.luxevistaresortapp;

import android.util.Patterns;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SecurityUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d/M/yyyy", Locale.US);

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

    public static boolean isCheckOutAfterCheckIn(String checkInStr, String checkOutStr) {
        try {
            Date checkIn = DATE_FORMAT.parse(checkInStr);
            Date checkOut = DATE_FORMAT.parse(checkOutStr);
            if (checkIn == null || checkOut == null) return false;
            return checkOut.after(checkIn);
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isDateNotPast(String dateStr) {
        try {
            Date date = DATE_FORMAT.parse(dateStr);
            if (date == null) return false;
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            return !date.before(today.getTime());
        } catch (ParseException e) {
            return true; // if parsing varies, do not block
        }
    }
}
