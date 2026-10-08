package com.example.luxevistaresortapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";
    public static final String DATABASE_NAME = "luxevista.db";
    public static final int DATABASE_VERSION = 6; // Upgraded for Phase 1 advanced booking & lifecycle metadata

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
            "userEmail TEXT, " +
            "bookingReference TEXT, " +
            "nights INTEGER, " +
            "adults INTEGER, " +
            "children INTEGER, " +
            "status TEXT DEFAULT 'RESERVED', " +
            "createdAt TEXT)";

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

    public static synchronized void resetInstanceForTest() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
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

        // Migration to Version 5 (if jumping from v4 or earlier)
        if (oldVersion < 5) {
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

        // Migration to Version 6 (Phase 1 Advanced Booking Columns)
        if (oldVersion < 6) {
            if (!hasColumn(db, TABLE_BOOKINGS, "bookingReference")) {
                try {
                    db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN bookingReference TEXT");
                } catch (Exception e) {
                    Log.e(TAG, "Error adding bookingReference column", e);
                }
            }

            if (!hasColumn(db, TABLE_BOOKINGS, "nights")) {
                try {
                    db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN nights INTEGER");
                } catch (Exception e) {
                    Log.e(TAG, "Error adding nights column", e);
                }
            }

            if (!hasColumn(db, TABLE_BOOKINGS, "adults")) {
                try {
                    db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN adults INTEGER");
                } catch (Exception e) {
                    Log.e(TAG, "Error adding adults column", e);
                }
            }

            if (!hasColumn(db, TABLE_BOOKINGS, "children")) {
                try {
                    db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN children INTEGER");
                } catch (Exception e) {
                    Log.e(TAG, "Error adding children column", e);
                }
            }

            if (!hasColumn(db, TABLE_BOOKINGS, "status")) {
                try {
                    db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN status TEXT DEFAULT 'RESERVED'");
                } catch (Exception e) {
                    Log.e(TAG, "Error adding status column", e);
                }
            }

            if (!hasColumn(db, TABLE_BOOKINGS, "createdAt")) {
                try {
                    db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN createdAt TEXT");
                } catch (Exception e) {
                    Log.e(TAG, "Error adding createdAt column", e);
                }
            }

            // Retrofit existing legacy bookings with reference numbers without altering prices or historical data
            try {
                db.execSQL("UPDATE " + TABLE_BOOKINGS + " SET bookingReference = 'LVR-LEGACY-' || id WHERE bookingReference IS NULL");
                db.execSQL("UPDATE " + TABLE_BOOKINGS + " SET status = 'RESERVED' WHERE status IS NULL");
            } catch (Exception e) {
                Log.e(TAG, "Error retrofitting legacy booking references", e);
            }
        }
    }

    public boolean hasColumn(SQLiteDatabase db, String tableName, String columnName) {
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

    /**
     * Atomically creates a room reservation within a database transaction.
     * Prevents accidental duplicate submissions by the same user.
     * Generates a persistent, dynamic collision-free reference number using the current calendar year.
     *
     * @return newBookingId if successful, -2 if duplicate submission detected, -1 on failure.
     */
    public long addBookingAtomic(String roomType, String checkIn, String checkOut, String price,
                                 String userEmail, int nights, int adults, int children) {
        SQLiteDatabase db = this.getWritableDatabase();
        String normalizedEmail = userEmail != null ? userEmail.trim().toLowerCase() : "";

        db.beginTransaction();
        long newBookingId = -1;
        try {
            // Check for accidental duplicate submission by the same user for identical room & dates
            if (hasDuplicateUserBooking(db, normalizedEmail, roomType, checkIn, checkOut, -1)) {
                Log.w(TAG, "Duplicate submission rejected for user: " + normalizedEmail);
                return -2; // Collision / duplicate code
            }

            ContentValues values = new ContentValues();
            values.put("roomType", roomType);
            values.put("checkIn", checkIn);
            values.put("checkOut", checkOut);
            values.put("price", price);
            values.put("userEmail", normalizedEmail);
            values.put("nights", nights);
            values.put("adults", adults);
            values.put("children", children);
            values.put("status", "RESERVED");
            values.put("createdAt", String.valueOf(System.currentTimeMillis()));

            newBookingId = db.insert(TABLE_BOOKINGS, null, values);

            if (newBookingId != -1) {
                // Generate dynamic reference number using current calendar year (never hardcoded)
                int currentYear = LocalDate.now().getYear();
                String bookingRef = String.format(Locale.US, "LVR-%d-%06d", currentYear, newBookingId);

                ContentValues refUpdate = new ContentValues();
                refUpdate.put("bookingReference", bookingRef);

                int rowsUpdated = db.update(TABLE_BOOKINGS, refUpdate, "id = ?",
                        new String[]{String.valueOf(newBookingId)});

                if (rowsUpdated > 0) {
                    db.setTransactionSuccessful();
                } else {
                    Log.e(TAG, "Failed to update booking reference inside transaction. Aborting.");
                    newBookingId = -1;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during atomic booking insertion", e);
            newBookingId = -1;
        } finally {
            db.endTransaction();
        }

        return newBookingId;
    }

    // Helper: checks accidental duplicate booking by same user, excluding an optional bookingId (for modification)
    public boolean hasDuplicateUserBooking(SQLiteDatabase db, String userEmail, String roomType,
                                           String checkIn, String checkOut, long excludeBookingId) {
        Cursor cursor = null;
        try {
            String sql;
            String[] args;
            if (excludeBookingId > 0) {
                sql = "SELECT COUNT(*) FROM " + TABLE_BOOKINGS +
                        " WHERE id != ? AND LOWER(userEmail) = ? AND roomType = ? AND checkIn = ? AND checkOut = ? AND status != 'CANCELLED'";
                args = new String[]{String.valueOf(excludeBookingId), userEmail.trim().toLowerCase(), roomType, checkIn, checkOut};
            } else {
                sql = "SELECT COUNT(*) FROM " + TABLE_BOOKINGS +
                        " WHERE LOWER(userEmail) = ? AND roomType = ? AND checkIn = ? AND checkOut = ? AND status != 'CANCELLED'";
                args = new String[]{userEmail.trim().toLowerCase(), roomType, checkIn, checkOut};
            }
            cursor = db.rawQuery(sql, args);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return false;
    }

    // Backward-compatible overload
    public boolean addBooking(String roomType, String checkIn, String checkOut, String price, String userEmail) {
        int nights = SecurityUtils.calculateStayNights(checkIn, checkOut);
        long id = addBookingAtomic(roomType, checkIn, checkOut, price, userEmail, nights, 1, 0);
        return id > 0;
    }

    public boolean addBooking(String roomType, String checkIn, String checkOut, String price) {
        return addBooking(roomType, checkIn, checkOut, price, "");
    }

    /**
     * Cancels a reservation with strict row-level ownership verification.
     * Prevents cancelling an already cancelled reservation.
     */
    public boolean cancelBookingWithOwner(long bookingId, String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) return false;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("status", "CANCELLED");

        int rows = db.update(TABLE_BOOKINGS, values,
                "id = ? AND LOWER(userEmail) = ? AND status != 'CANCELLED'",
                new String[]{String.valueOf(bookingId), userEmail.trim().toLowerCase()});

        return rows > 0;
    }

    /**
     * Modifies an active reservation with strict row-level ownership verification.
     * Excludes current booking ID from duplicate check.
     * Prevents editing cancelled reservations.
     */
    public boolean modifyBookingWithOwner(long bookingId, String userEmail, String roomType,
                                          String checkIn, String checkOut, String newPrice,
                                          int nights, int adults, int children) {
        if (userEmail == null || userEmail.trim().isEmpty()) return false;
        String normalizedEmail = userEmail.trim().toLowerCase();
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        try {
            // 1. Verify reservation exists, belongs to user, and is NOT cancelled
            Cursor cursor = db.rawQuery("SELECT status FROM " + TABLE_BOOKINGS + " WHERE id = ? AND LOWER(userEmail) = ?",
                    new String[]{String.valueOf(bookingId), normalizedEmail});
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) cursor.close();
                return false;
            }
            String currentStatus = cursor.getString(0);
            cursor.close();

            if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
                Log.w(TAG, "Cannot modify a cancelled booking: " + bookingId);
                return false;
            }

            // 2. Check for duplicate reservation excluding this current booking ID
            if (hasDuplicateUserBooking(db, normalizedEmail, roomType, checkIn, checkOut, bookingId)) {
                Log.w(TAG, "Modification collision: duplicate booking exists for these dates.");
                return false;
            }

            // 3. Apply updates
            ContentValues values = new ContentValues();
            values.put("roomType", roomType);
            values.put("checkIn", checkIn);
            values.put("checkOut", checkOut);
            values.put("price", newPrice);
            values.put("nights", nights);
            values.put("adults", adults);
            values.put("children", children);

            int rows = db.update(TABLE_BOOKINGS, values, "id = ? AND LOWER(userEmail) = ?",
                    new String[]{String.valueOf(bookingId), normalizedEmail});

            if (rows > 0) {
                db.setTransactionSuccessful();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during booking modification", e);
        } finally {
            db.endTransaction();
        }

        return false;
    }

    /**
     * Retrieves full booking details with row-level ownership verification.
     */
    public BookingModel getBookingModelById(long bookingId, String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) return null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT id, roomType, checkIn, checkOut, price, userEmail, bookingReference, nights, adults, children, status, createdAt FROM "
                            + TABLE_BOOKINGS + " WHERE id = ? AND LOWER(userEmail) = ?",
                    new String[]{String.valueOf(bookingId), userEmail.trim().toLowerCase()});

            if (cursor != null && cursor.moveToFirst()) {
                return new BookingModel(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getInt(7),
                        cursor.getInt(8),
                        cursor.getInt(9),
                        cursor.getString(10),
                        cursor.getString(11)
                );
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
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
            return db.rawQuery(
                    "SELECT id AS _id, roomType, checkIn, checkOut, price, userEmail, bookingReference, nights, adults, children, status, createdAt FROM "
                            + TABLE_BOOKINGS +
                            " WHERE LOWER(userEmail) = ? OR userEmail IS NULL OR userEmail = '' ORDER BY id DESC",
                    new String[]{normalizedEmail});
        }
        return getAllBookings();
    }

    /**
     * Retrieves typed list of BookingModel objects for the user, safely managing cursor lifecycle.
     */
    public List<BookingModel> getBookingListForUser(String userEmail) {
        List<BookingModel> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getBookingsForUser(userEmail);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String roomType = cursor.getString(1);
                    String checkIn = cursor.getString(2);
                    String checkOut = cursor.getString(3);
                    String price = cursor.getString(4);
                    String email = cursor.getString(5);
                    String ref = cursor.getString(6);
                    int nights = cursor.getInt(7);
                    int adults = cursor.getInt(8);
                    int children = cursor.getInt(9);
                    String status = cursor.getString(10);
                    String createdAt = cursor.getString(11);

                    list.add(new BookingModel(id, roomType, checkIn, checkOut, price, email, ref, nights, adults, children, status, createdAt));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return list;
    }

    // Fetch all Room Bookings (aliasing id AS _id for CursorAdapter)
    public Cursor getAllBookings() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id AS _id, roomType, checkIn, checkOut, price, userEmail, bookingReference, nights, adults, children, status, createdAt FROM "
                + TABLE_BOOKINGS + " ORDER BY id DESC", null);
    }

    // Fetch Service Bookings for specific user (aliasing id AS _id for CursorAdapter)
    public Cursor getServiceBookingsForUser(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            String normalizedEmail = userEmail.trim().toLowerCase();
            return db.rawQuery(
                    "SELECT id AS _id, serviceName, customerName, date, time, userEmail FROM " + TABLE_SERVICE_BOOKINGS +
                            " WHERE LOWER(userEmail) = ? OR userEmail IS NULL OR userEmail = '' ORDER BY id DESC",
                    new String[]{normalizedEmail});
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
