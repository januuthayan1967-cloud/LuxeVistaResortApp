package com.example.luxevistaresortapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.time.LocalDate;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class DatabaseMigrationRobolectricTest {

    private Context context;
    private DBHelper dbHelper;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        DBHelper.resetInstanceForTest();
        context.deleteDatabase(DBHelper.DATABASE_NAME);
        dbHelper = DBHelper.getInstance(context);
    }

    @After
    public void tearDown() {
        DBHelper.resetInstanceForTest();
        context.deleteDatabase(DBHelper.DATABASE_NAME);
    }

    @Test
    public void testDatabaseMigrationV5toV6AndPreservation() {
        String testDbName = "test_robolectric_migration.db";
        context.deleteDatabase(testDbName);

        SQLiteDatabase testDb = context.openOrCreateDatabase(testDbName, Context.MODE_PRIVATE, null);

        // 1. Create simulated Version 5 bookings table (without v6 columns)
        testDb.execSQL("CREATE TABLE bookings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "roomType TEXT, " +
                "checkIn TEXT, " +
                "checkOut TEXT, " +
                "price TEXT, " +
                "userEmail TEXT)");

        // 2. Insert legacy records without v6 columns
        ContentValues v5Record1 = new ContentValues();
        v5Record1.put("roomType", "Ocean View Suite");
        v5Record1.put("checkIn", "10/10/2026");
        v5Record1.put("checkOut", "15/10/2026");
        v5Record1.put("price", "125000");
        v5Record1.put("userEmail", "legacy_guest@luxevista.com");
        long legacyId1 = testDb.insert("bookings", null, v5Record1);
        assertTrue(legacyId1 > 0);

        ContentValues v5Record2 = new ContentValues();
        v5Record2.put("roomType", "Deluxe Room");
        v5Record2.put("checkIn", "20/10/2026");
        v5Record2.put("checkOut", "22/10/2026");
        v5Record2.put("price", "36000");
        v5Record2.put("userEmail", "legacy_guest2@luxevista.com");
        long legacyId2 = testDb.insert("bookings", null, v5Record2);
        assertTrue(legacyId2 > 0);

        // 3. Execute Version 5 to 6 migration using DBHelper.onUpgrade
        dbHelper.onUpgrade(testDb, 5, 6);

        // 4. Verify new columns exist
        assertTrue(dbHelper.hasColumn(testDb, "bookings", "bookingReference"));
        assertTrue(dbHelper.hasColumn(testDb, "bookings", "nights"));
        assertTrue(dbHelper.hasColumn(testDb, "bookings", "adults"));
        assertTrue(dbHelper.hasColumn(testDb, "bookings", "children"));
        assertTrue(dbHelper.hasColumn(testDb, "bookings", "status"));
        assertTrue(dbHelper.hasColumn(testDb, "bookings", "createdAt"));

        // 5. Verify legacy data was fully preserved and retrofitted
        Cursor cursor = testDb.rawQuery("SELECT id, roomType, price, userEmail, bookingReference, status FROM bookings WHERE id = ?",
                new String[]{String.valueOf(legacyId1)});
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        assertEquals("Ocean View Suite", cursor.getString(1));
        assertEquals("125000", cursor.getString(2));
        assertEquals("legacy_guest@luxevista.com", cursor.getString(3));
        assertEquals("LVR-LEGACY-" + legacyId1, cursor.getString(4));
        assertEquals("RESERVED", cursor.getString(5));
        cursor.close();

        testDb.close();
        context.deleteDatabase(testDbName);
    }

    @Test
    public void testAtomicBookingInsertionAndReferenceGeneration() {
        String userEmail = "test_robo_atomic@luxevista.com";
        String roomType = "Family Grand Room";
        String checkIn = "01/11/2026";
        String checkOut = "05/11/2026";
        String price = "120000";

        long bookingId = dbHelper.addBookingAtomic(roomType, checkIn, checkOut, price, userEmail, 4, 2, 2);
        assertTrue("Booking insertion should return valid positive ID", bookingId > 0);

        // Verify record retrieved with dynamic year reference
        BookingModel model = dbHelper.getBookingModelById(bookingId, userEmail);
        assertNotNull(model);
        assertEquals(roomType, model.getRoomType());
        assertEquals(4, model.getNights());
        assertEquals(2, model.getAdults());
        assertEquals(2, model.getChildren());
        assertEquals("RESERVED", model.getStatus());

        int currentYear = LocalDate.now().getYear();
        String expectedRef = String.format("LVR-%d-%06d", currentYear, bookingId);
        assertEquals(expectedRef, model.getBookingReference());
    }

    @Test
    public void testAccidentalDuplicateSubmissionDetection() {
        String userEmail = "test_robo_duplicate@luxevista.com";
        String roomType = "Single Room";
        String checkIn = "10/12/2026";
        String checkOut = "12/12/2026";
        String price = "24000";

        // First booking should succeed
        long firstId = dbHelper.addBookingAtomic(roomType, checkIn, checkOut, price, userEmail, 2, 1, 0);
        assertTrue(firstId > 0);

        // Immediate identical submission by same user should be detected and rejected (-2)
        long duplicateId = dbHelper.addBookingAtomic(roomType, checkIn, checkOut, price, userEmail, 2, 1, 0);
        assertEquals(-2, duplicateId);

        // Different user booking same room & dates must NOT be rejected (inventory is not artificially constrained)
        String otherUser = "other_guest@luxevista.com";
        long otherUserId = dbHelper.addBookingAtomic(roomType, checkIn, checkOut, price, otherUser, 2, 1, 0);
        assertTrue(otherUserId > 0);
    }

    @Test
    public void testOwnershipVerifiedCancellation() {
        String ownerEmail = "test_robo_owner@luxevista.com";
        String attackerEmail = "unauthorized_robo_user@luxevista.com";

        long bookingId = dbHelper.addBookingAtomic("Deluxe Room", "01/12/2026", "03/12/2026", "36000", ownerEmail, 2, 2, 0);
        assertTrue(bookingId > 0);

        // Attacker attempting cancellation should be rejected
        boolean attackCancelled = dbHelper.cancelBookingWithOwner(bookingId, attackerEmail);
        assertFalse("Unauthorized user must NOT be allowed to cancel another user's booking", attackCancelled);

        // Genuine owner cancellation should succeed
        boolean ownerCancelled = dbHelper.cancelBookingWithOwner(bookingId, ownerEmail);
        assertTrue("Legitimate owner should successfully cancel", ownerCancelled);

        // Second cancellation attempt should fail (already cancelled)
        boolean repeatCancelled = dbHelper.cancelBookingWithOwner(bookingId, ownerEmail);
        assertFalse("Cannot cancel an already cancelled reservation", repeatCancelled);

        // Status must be CANCELLED
        BookingModel model = dbHelper.getBookingModelById(bookingId, ownerEmail);
        assertNotNull(model);
        assertEquals("CANCELLED", model.getStatus());
        assertTrue(model.isCancelled());
    }

    @Test
    public void testOwnershipVerifiedModification() {
        String ownerEmail = "test_robo_modify@luxevista.com";
        String attackerEmail = "unauthorized_robo_modify@luxevista.com";

        long bookingId = dbHelper.addBookingAtomic("Single Room", "15/12/2026", "17/12/2026", "24000", ownerEmail, 2, 1, 0);
        assertTrue(bookingId > 0);

        // Unauthorized user cannot modify
        boolean unauthorizedMod = dbHelper.modifyBookingWithOwner(bookingId, attackerEmail, "Deluxe Room", "16/12/2026", "18/12/2026", "36000", 2, 2, 0);
        assertFalse(unauthorizedMod);

        // Genuine owner can modify (and keeping same bookingId does not trigger duplicate error on itself)
        boolean ownerMod = dbHelper.modifyBookingWithOwner(bookingId, ownerEmail, "Deluxe Room", "16/12/2026", "19/12/2026", "54000", 3, 2, 1);
        assertTrue(ownerMod);

        BookingModel updated = dbHelper.getBookingModelById(bookingId, ownerEmail);
        assertNotNull(updated);
        assertEquals("Deluxe Room", updated.getRoomType());
        assertEquals("16/12/2026", updated.getCheckIn());
        assertEquals(3, updated.getNights());
        assertEquals("54000", updated.getPrice());

        // Cancel the booking
        assertTrue(dbHelper.cancelBookingWithOwner(bookingId, ownerEmail));

        // Attempting to modify a cancelled booking must fail
        boolean cancelledMod = dbHelper.modifyBookingWithOwner(bookingId, ownerEmail, "Ocean View Suite", "20/12/2026", "25/12/2026", "125000", 5, 2, 0);
        assertFalse("Must NOT allow modifying a cancelled reservation", cancelledMod);
    }
}
