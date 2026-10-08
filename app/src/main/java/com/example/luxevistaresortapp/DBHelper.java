package com.example.luxevistaresortapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "luxevista.db";
    public static final int DATABASE_VERSION = 4; // Incremented version for schema change

    // Singleton instance
    private static DBHelper instance;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String TABLE_SERVICE_BOOKINGS = "service_bookings";

    // Create tables SQL
    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "email TEXT, " +
            "password TEXT)";

    private static final String CREATE_BOOKINGS_TABLE = "CREATE TABLE " + TABLE_BOOKINGS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "roomType TEXT, " +
            "checkIn TEXT, " +
            "checkOut TEXT, " +
            "price TEXT)";

    private static final String CREATE_SERVICE_BOOKINGS_TABLE = "CREATE TABLE " + TABLE_SERVICE_BOOKINGS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "serviceName TEXT, " +
            "customerName TEXT, " +
            "date TEXT, " +
            "time TEXT)";

    // Private constructor
    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_BOOKINGS_TABLE);
        db.execSQL(CREATE_SERVICE_BOOKINGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICE_BOOKINGS);
        onCreate(db);
    }

    // Add Room Booking
    public boolean addBooking(String roomType, String checkIn, String checkOut, String price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("roomType", roomType);
        values.put("checkIn", checkIn);
        values.put("checkOut", checkOut);
        values.put("price", price);

        long result = db.insert(TABLE_BOOKINGS, null, values);
        return result != -1;
    }

    // Add Service Booking
    public boolean addServiceBooking(String serviceName, String customerName, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("serviceName", serviceName);
        values.put("customerName", customerName);
        values.put("date", date);
        values.put("time", time);

        long result = db.insert(TABLE_SERVICE_BOOKINGS, null, values);
        Log.d("DBHelper", "Insert Result: " + result);
        return result != -1;
    }





    // Fetch all Room Bookings
    public Cursor getAllBookings() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BOOKINGS, null);
    }

    // Fetch all Service Bookings
    public Cursor getAllServiceBookings() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SERVICE_BOOKINGS, null);
    }

    public Cursor getRoomBookingById(int bookingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BOOKINGS + " WHERE id = ?", new String[]{String.valueOf(bookingId)});
    }

    public Cursor getServiceBookingById(int bookingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SERVICE_BOOKINGS + " WHERE id = ?", new String[]{String.valueOf(bookingId)});
    }

    public boolean updateRoomBooking(int bookingId, String roomType, String checkIn, String checkOut, String price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("roomType", roomType);
        values.put("checkIn", checkIn);
        values.put("checkOut", checkOut);
        values.put("price", price);

        int result = db.update(TABLE_BOOKINGS, values, "id = ?", new String[]{String.valueOf(bookingId)});
        return result > 0;
    }
    // Add User method to insert user data into the 'users' table
    public boolean addUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);  // Insert the user's name
        values.put("email", email);  // Insert the user's email
        values.put("password", password);  // Insert the user's password

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;  // If result is -1, the insert failed; otherwise, it succeeded.
    }

    public boolean updateServiceBooking(int bookingId, String serviceName, String customerName, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("serviceName", serviceName);
        values.put("customerName", customerName);
        values.put("date", date);
        values.put("time", time);

        int result = db.update(TABLE_SERVICE_BOOKINGS, values, "id = ?", new String[]{String.valueOf(bookingId)});
        return result > 0;
    }

    public boolean deleteRoomBooking(int bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_BOOKINGS, "id = ?", new String[]{String.valueOf(bookingId)});
        return result > 0;
    }

    public boolean deleteServiceBooking(int bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_SERVICE_BOOKINGS, "id = ?", new String[]{String.valueOf(bookingId)});
        return result > 0;
    }

    public String[] getUserDetails(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, email FROM " + TABLE_USERS + " WHERE id = ?", new String[]{userId});

        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            String email = cursor.getString(1);
            cursor.close();
            return new String[]{name, email};
        } else {
            cursor.close();
            return null;
        }
    }

    public String getUserNameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM " + TABLE_USERS + " WHERE email = ?", new String[]{email});

        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            return name;
        } else {
            cursor.close();
            return "Unknown";
        }
    }
}
