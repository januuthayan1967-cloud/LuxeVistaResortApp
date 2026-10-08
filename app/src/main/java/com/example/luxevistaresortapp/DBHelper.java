package com.example.luxevistaresortapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";
    public static final String DATABASE_NAME = "luxevista.db";
    public static final int DATABASE_VERSION = 5; // Incremented for safe schema upgrade

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
            "email TEXT UNIQUE, " +
            "password TEXT, " +
            "salt TEXT)";

    private static final String CREATE_BOOKINGS_TABLE = "CREATE TABLE " + TABLE_BOOKINGS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "roomType TEXT, " +
            "checkIn TEXT, " +
            "checkOut TEXT, " +
            "price TEXT, " +
            "userEmail TEXT)";

    private static final String CREATE_SERVICE_BOOKINGS_TABLE = "CREATE TABLE " + TABLE_SERVICE_BOOKINGS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "serviceName TEXT, " +
            "customerName TEXT, " +
            "date TEXT, " +
            "time TEXT, " +
            "userEmail TEXT)";

    DBHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
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
        Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + " without data loss");
        
        // Non-destructive safe migration
        if (!hasColumn(db, TABLE_USERS, "salt")) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN salt TEXT");
            } catch (Exception e) {
                Log.e(TAG, "Error adding salt column to users", e);
            }
        }

        if (!hasColumn(db, TABLE_BOOKINGS, "userEmail")) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN userEmail TEXT");
            } catch (Exception e) {
                Log.e(TAG, "Error adding userEmail column to bookings", e);
            }
        }

        if (!hasColumn(db, TABLE_SERVICE_BOOKINGS, "userEmail")) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_SERVICE_BOOKINGS + " ADD COLUMN userEmail TEXT");
            } catch (Exception e) {
                Log.e(TAG, "Error adding userEmail column to service_bookings", e);
            }
        }
    }

    private boolean hasColumn(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    if (nameIndex != -1 && columnName.equalsIgnoreCase(cursor.getString(nameIndex))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking column " + columnName + " in table " + tableName, e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return false;
    }

    // Add User with salted hash
    public boolean addUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name != null ? name.trim() : "");
        values.put("email", email != null ? email.trim().toLowerCase() : "");

        String salt = SecurityUtils.generateSalt();
        String hashedPassword = SecurityUtils.hashPassword(password, salt);
        values.put("password", hashedPassword);
        values.put("salt", salt);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Authenticate user with automatic migration from plaintext
    public UserModel authenticateUser(String email, String password) {
        if (email == null || password == null) return null;
        String normalizedEmail = email.trim().toLowerCase();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT id, name, email, password, salt FROM " + TABLE_USERS + " WHERE LOWER(email) = ?",
                    new String[]{normalizedEmail});

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String storedEmail = cursor.getString(2);
                String storedPassword = cursor.getString(3);
                String salt = cursor.getColumnCount() > 4 ? cursor.getString(4) : null;

                boolean isValid = false;

                if (salt != null && !salt.isEmpty()) {
                    String hashedAttempt = SecurityUtils.hashPassword(password, salt);
                    if (hashedAttempt.equals(storedPassword)) {
                        isValid = true;
                    }
                } else {
                    // Check legacy plaintext password
                    if (password.equals(storedPassword)) {
                        isValid = true;
                        // Transparently migrate to salted hash
                        migrateUserPasswordToHash(id, password);
                    }
                }

                if (isValid) {
                    return new UserModel(id, name, storedEmail);
                }
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    private void migrateUserPasswordToHash(int userId, String plainPassword) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String salt = SecurityUtils.generateSalt();
            String hashed = SecurityUtils.hashPassword(plainPassword, salt);
            ContentValues cv = new ContentValues();
            cv.put("password", hashed);
            cv.put("salt", salt);
            db.update(TABLE_USERS, cv, "id = ?", new String[]{String.valueOf(userId)});
            Log.i(TAG, "Migrated user ID " + userId + " to salted password hash");
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading user password hash", e);
        }
    }

    public boolean isValidUser(String email, String password) {
        return authenticateUser(email, password) != null;
    }

    // Add Room Booking with userEmail
    public boolean addBooking(String roomType, String checkIn, String checkOut, String price, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("roomType", roomType);
        values.put("checkIn", checkIn);
        values.put("checkOut", checkOut);
        values.put("price", price);
        values.put("userEmail", userEmail != null ? userEmail.trim().toLowerCase() : "");

        long result = db.insert(TABLE_BOOKINGS, null, values);
        return result != -1;
    }

    public boolean addBooking(String roomType, String checkIn, String checkOut, String price) {
        return addBooking(roomType, checkIn, checkOut, price, "");
    }

    // Add Service Booking with userEmail
    public boolean addServiceBooking(String serviceName, String customerName, String date, String time, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("serviceName", serviceName);
        values.put("customerName", customerName);
        values.put("date", date);
        values.put("time", time);
        values.put("userEmail", userEmail != null ? userEmail.trim().toLowerCase() : "");

        long result = db.insert(TABLE_SERVICE_BOOKINGS, null, values);
        return result != -1;
    }

    public boolean addServiceBooking(String serviceName, String customerName, String date, String time) {
        return addServiceBooking(serviceName, customerName, date, time, "");
    }

    // Fetch Room Bookings for specific user (aliasing id AS _id for CursorAdapter)
    public Cursor getBookingsForUser(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            String normalizedEmail = userEmail.trim().toLowerCase();
            Cursor cursor = db.rawQuery(
                    "SELECT id AS _id, roomType, checkIn, checkOut, price, userEmail FROM " + TABLE_BOOKINGS +
                            " WHERE LOWER(userEmail) = ? OR userEmail IS NULL OR userEmail = '' ORDER BY id DESC",
                    new String[]{normalizedEmail});
            return cursor;
        }
        return getAllBookings();
    }

    // Fetch all Room Bookings (aliasing id AS _id for CursorAdapter)
    public Cursor getAllBookings() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id AS _id, roomType, checkIn, checkOut, price, userEmail FROM " + TABLE_BOOKINGS + " ORDER BY id DESC", null);
    }

    // Fetch Service Bookings for specific user (aliasing id AS _id for CursorAdapter)
    public Cursor getServiceBookingsForUser(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            String normalizedEmail = userEmail.trim().toLowerCase();
            Cursor cursor = db.rawQuery(
                    "SELECT id AS _id, serviceName, customerName, date, time, userEmail FROM " + TABLE_SERVICE_BOOKINGS +
                            " WHERE LOWER(userEmail) = ? OR userEmail IS NULL OR userEmail = '' ORDER BY id DESC",
                    new String[]{normalizedEmail});
            return cursor;
        }
        return getAllServiceBookings();
    }

    // Fetch all Service Bookings (aliasing id AS _id for CursorAdapter)
    public Cursor getAllServiceBookings() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id AS _id, serviceName, customerName, date, time, userEmail FROM " + TABLE_SERVICE_BOOKINGS + " ORDER BY id DESC", null);
    }

    public Cursor getRoomBookingById(int bookingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id AS _id, roomType, checkIn, checkOut, price FROM " + TABLE_BOOKINGS + " WHERE id = ?", new String[]{String.valueOf(bookingId)});
    }

    public Cursor getServiceBookingById(int bookingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id AS _id, serviceName, customerName, date, time FROM " + TABLE_SERVICE_BOOKINGS + " WHERE id = ?", new String[]{String.valueOf(bookingId)});
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
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name, email FROM " + TABLE_USERS + " WHERE id = ?", new String[]{userId});
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                String email = cursor.getString(1);
                return new String[]{name, email};
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    public String getUserNameByEmail(String email) {
        if (email == null) return "Guest";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM " + TABLE_USERS + " WHERE LOWER(email) = ?", new String[]{email.trim().toLowerCase()});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return "Valued Guest";
    }
}
