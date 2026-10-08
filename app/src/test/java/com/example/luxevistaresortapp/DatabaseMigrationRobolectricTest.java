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

    @Test
    public void testAtomicBookingWithCustomGuestsAndSubtotal() {
        String ownerEmail = "test_step2_guest@luxevista.com";
        String roomType = "Ocean View Suite";
        String checkIn = "05/11/2026";
        String checkOut = "08/11/2026"; // 3 nights
        int nights = 3;
        long nightlyRate = 25000L;
        long subtotal = nights * nightlyRate; // 75,000
        int adults = 2;
        int children = 1;

        long bookingId = dbHelper.addBookingAtomic(
                roomType,
                checkIn,
                checkOut,
                String.valueOf(subtotal),
                ownerEmail,
                nights,
                adults,
                children
        );

        assertTrue("Booking insertion with custom guests and subtotal must succeed", bookingId > 0);

        BookingModel model = dbHelper.getBookingModelById(bookingId, ownerEmail);
        assertNotNull(model);
        assertEquals("Ocean View Suite", model.getRoomType());
        assertEquals("05/11/2026", model.getCheckIn());
        assertEquals("08/11/2026", model.getCheckOut());
        assertEquals("75000", model.getPrice());
        assertEquals(3, model.getNights());
        assertEquals(2, model.getAdults());
        assertEquals(1, model.getChildren());
        assertEquals("RESERVED", model.getStatus());
        assertFalse(model.isCancelled());
    }

    @Test
    public void testBookingVoucherRetrievalAndOwnershipValidation() {
        String owner = "voucher_owner@luxevista.com";
        String attacker = "voucher_intruder@luxevista.com";

        long bookingId = dbHelper.addBookingAtomic(
                "Deluxe Room",
                "12/11/2026",
                "15/11/2026",
                "54000",
                owner,
                3,
                2,
                0
        );
        assertTrue(bookingId > 0);

        // 1. Genuine owner can access voucher
        BookingModel legitimate = dbHelper.getBookingModelById(bookingId, owner);
        assertNotNull("Owner must be able to load their own reservation", legitimate);
        assertTrue(legitimate.getBookingReference().startsWith("LVR-"));
        assertEquals("Deluxe Room", legitimate.getRoomType());
        assertEquals("54000", legitimate.getPrice());
        assertEquals(3, legitimate.getNights());
        assertEquals(2, legitimate.getAdults());
        assertEquals(0, legitimate.getChildren());
        assertEquals("RESERVED", legitimate.getStatus());

        // 2. Intruder is denied (returns null)
        BookingModel unauthorized = dbHelper.getBookingModelById(bookingId, attacker);
        assertNull("Intruder must NOT be able to view another user's reservation", unauthorized);

        // 3. Non-existent booking ID returns null
        BookingModel nonExistent = dbHelper.getBookingModelById(999999L, owner);
        assertNull("Non-existent booking must return null", nonExistent);

        // 4. Null or empty user email returns null
        BookingModel nullUser = dbHelper.getBookingModelById(bookingId, null);
        assertNull("Null user email must return null", nullUser);
        BookingModel emptyUser = dbHelper.getBookingModelById(bookingId, "   ");
        assertNull("Empty user email must return null", emptyUser);
    }

    @Test
    public void testBookingHistoryRetrievalAndIsolation() {
        String userA = "alice@luxevista.com";
        String userB = "bob@luxevista.com";

        // User A has 2 bookings
        long idA1 = dbHelper.addBookingAtomic("Ocean View Suite", "01/10/2026", "05/10/2026", "100000", userA, 4, 2, 0);
        long idA2 = dbHelper.addBookingAtomic("Deluxe Room", "10/10/2026", "12/10/2026", "36000", userA, 2, 1, 0);
        assertTrue(idA1 > 0);
        assertTrue(idA2 > 0);

        // User B has 1 booking
        long idB1 = dbHelper.addBookingAtomic("Single Room", "15/10/2026", "17/10/2026", "24000", userB, 2, 1, 0);
        assertTrue(idB1 > 0);

        // Fetch bookings for User A
        java.util.List<BookingModel> listA = dbHelper.getBookingListForUser(userA);
        assertNotNull(listA);
        assertEquals(2, listA.size());
        // Ordered by id DESC
        assertEquals(idA2, listA.get(0).getId());
        assertEquals(idA1, listA.get(1).getId());

        // Fetch bookings for User B
        java.util.List<BookingModel> listB = dbHelper.getBookingListForUser(userB);
        assertNotNull(listB);
        assertEquals(1, listB.size());
        assertEquals(idB1, listB.get(0).getId());

        // Non-existent user has empty list
        java.util.List<BookingModel> listEmpty = dbHelper.getBookingListForUser("nobody@luxevista.com");
        assertNotNull(listEmpty);
        assertTrue(listEmpty.isEmpty());
    }

    @Test
    public void testOriginalReferenceAndTimestampPreservedOnModification() {
        String owner = "charlie@luxevista.com";
        long bookingId = dbHelper.addBookingAtomic("Single Room", "01/11/2026", "03/11/2026", "24000", owner, 2, 1, 0);
        assertTrue(bookingId > 0);

        BookingModel original = dbHelper.getBookingModelById(bookingId, owner);
        assertNotNull(original);
        String originalRef = original.getBookingReference();
        String originalCreatedAt = original.getCreatedAt();
        assertNotNull(originalRef);
        assertNotNull(originalCreatedAt);

        // Check total count before modification
        assertEquals(1, dbHelper.getBookingListForUser(owner).size());

        // Modify booking details
        boolean modified = dbHelper.modifyBookingWithOwner(
                bookingId,
                owner,
                "Ocean View Suite",
                "02/11/2026",
                "06/11/2026",
                "100000",
                4,
                2,
                1
        );
        assertTrue("Modification must succeed", modified);

        // Verify count remains exactly 1 (no duplicate record inserted)
        assertEquals(1, dbHelper.getBookingListForUser(owner).size());

        BookingModel updated = dbHelper.getBookingModelById(bookingId, owner);
        assertNotNull(updated);
        assertEquals(originalRef, updated.getBookingReference());
        assertEquals(originalCreatedAt, updated.getCreatedAt());
        assertEquals("Ocean View Suite", updated.getRoomType());
        assertEquals("02/11/2026", updated.getCheckIn());
        assertEquals("06/11/2026", updated.getCheckOut());
        assertEquals("100000", updated.getPrice());
        assertEquals(4, updated.getNights());
        assertEquals(2, updated.getAdults());
        assertEquals(1, updated.getChildren());
    }

    @Test
    public void testDuplicateCollisionDetectionDuringModification() {
        String owner = "dana@luxevista.com";
        // Booking 1: Single Room, 01/12/2026 to 05/12/2026
        long id1 = dbHelper.addBookingAtomic("Single Room", "01/12/2026", "05/12/2026", "48000", owner, 4, 1, 0);
        assertTrue(id1 > 0);

        // Booking 2: Deluxe Room, 10/12/2026 to 15/12/2026
        long id2 = dbHelper.addBookingAtomic("Deluxe Room", "10/12/2026", "15/12/2026", "90000", owner, 5, 2, 0);
        assertTrue(id2 > 0);

        // Attempting to modify Booking 2 to match Booking 1's room & dates must fail (duplicate detected)
        boolean collisionMod = dbHelper.modifyBookingWithOwner(
                id2,
                owner,
                "Single Room",
                "01/12/2026",
                "05/12/2026",
                "48000",
                4,
                1,
                0
        );
        assertFalse("Modifying a booking into a conflicting duplicate must be rejected", collisionMod);

        // Modifying Booking 2 to new non-colliding dates must succeed
        boolean safeMod = dbHelper.modifyBookingWithOwner(
                id2,
                owner,
                "Deluxe Room",
                "11/12/2026",
                "16/12/2026",
                "90000",
                5,
                2,
                0
        );
        assertTrue("Modifying to non-colliding dates must succeed", safeMod);
    }

    @Test
    public void testLegacyBookingCompatibilityInListRetrieval() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues legacy = new ContentValues();
        legacy.put("roomType", "Vintage Penthouse");
        legacy.put("checkIn", "05/05/2024");
        legacy.put("checkOut", "10/05/2024");
        legacy.put("price", "Rs. 75,000");
        legacy.put("userEmail", "legacy_user@luxevista.com");
        // Leave nights, adults, children null / 0
        long legacyId = db.insert(DBHelper.TABLE_BOOKINGS, null, legacy);
        assertTrue(legacyId > 0);

        java.util.List<BookingModel> list = dbHelper.getBookingListForUser("legacy_user@luxevista.com");
        assertNotNull(list);
        assertEquals(1, list.size());

        BookingModel item = list.get(0);
        assertEquals(legacyId, item.getId());
        assertEquals("Vintage Penthouse", item.getRoomType());
        assertEquals("Rs. 75,000", item.getPrice());
        assertEquals("05/05/2024", item.getCheckIn());
        assertEquals("10/05/2024", item.getCheckOut());
        // Verify missing guest/night data is safely 0 without exceptions
        assertEquals(0, item.getNights());
        assertEquals(0, item.getAdults());
        assertEquals(0, item.getChildren());
    }
}
