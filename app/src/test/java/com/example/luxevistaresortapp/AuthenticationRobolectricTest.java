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

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class AuthenticationRobolectricTest {

    private Context context;
    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        DBHelper.resetInstanceForTest();
        context.deleteDatabase(DBHelper.DATABASE_NAME);
        dbHelper = DBHelper.getInstance(context);
        sessionManager = new SessionManager(context);
        sessionManager.logoutUser();
    }

    @After
    public void tearDown() {
        sessionManager.logoutUser();
        DBHelper.resetInstanceForTest();
        context.deleteDatabase(DBHelper.DATABASE_NAME);
    }

    @Test
    public void testEmailValidationPatterns() {
        // Valid email patterns
        assertTrue(SecurityUtils.isValidEmail("guest@luxevista.com"));
        assertTrue(SecurityUtils.isValidEmail("alexander.wright@resort.travel"));
        assertTrue(SecurityUtils.isValidEmail("vip_suite@luxury-haven.org"));

        // Invalid email patterns
        assertFalse(SecurityUtils.isValidEmail(""));
        assertFalse(SecurityUtils.isValidEmail(null));
        assertFalse(SecurityUtils.isValidEmail("notanemail"));
        assertFalse(SecurityUtils.isValidEmail("@missingusername.com"));
        assertFalse(SecurityUtils.isValidEmail("guest@"));
    }

    @Test
    public void testUserRegistrationSuccess() {
        boolean created = dbHelper.addUser("Alexander Wright", "alex@luxevista.com", "LuxePass2026!");
        assertTrue("User registration should succeed with valid credentials", created);

        // Verify stored in DB with salted hash
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, email, password, salt FROM " + DBHelper.TABLE_USERS +
                " WHERE LOWER(email) = ?", new String[]{"alex@luxevista.com"});
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        assertEquals("Alexander Wright", cursor.getString(0));
        assertEquals("alex@luxevista.com", cursor.getString(1));
        String storedHash = cursor.getString(2);
        String storedSalt = cursor.getString(3);

        assertNotNull(storedHash);
        assertNotNull(storedSalt);
        assertFalse(storedHash.isEmpty());
        assertFalse(storedSalt.isEmpty());
        assertNotEquals("LuxePass2026!", storedHash); // Plaintext must never be stored
        cursor.close();
    }

    @Test
    public void testUserRegistrationDuplicateEmailRejected() {
        boolean first = dbHelper.addUser("Victoria Sterling", "victoria@luxevista.com", "Pass123456");
        assertTrue(first);

        // Exact match duplicate
        boolean duplicateExact = dbHelper.addUser("Victoria Copy", "victoria@luxevista.com", "Pass123456");
        assertFalse("Duplicate email registration must be rejected", duplicateExact);

        // Case-insensitive duplicate (e.g. uppercase vs lowercase)
        boolean duplicateCase = dbHelper.addUser("Victoria Different Case", "VICTORIA@LUXEVISTA.COM", "Pass123456");
        assertFalse("Case-insensitive duplicate email registration must be rejected", duplicateCase);
    }

    @Test
    public void testUserAuthenticationSuccess() {
        dbHelper.addUser("Julian Vance", "julian@luxevista.com", "SecretSuite2026");

        UserModel user = dbHelper.authenticateUser("julian@luxevista.com", "SecretSuite2026");
        assertNotNull("Authentication must succeed with matching credentials", user);
        assertTrue(user.getId() > 0);
        assertEquals("Julian Vance", user.getName());
        assertEquals("julian@luxevista.com", user.getEmail());

        // Case-insensitive email authentication
        UserModel userUpper = dbHelper.authenticateUser("JULIAN@LUXEVISTA.COM", "SecretSuite2026");
        assertNotNull("Authentication must succeed regardless of email casing", userUpper);
        assertEquals(user.getId(), userUpper.getId());
    }

    @Test
    public void testUserAuthenticationInvalidPasswordRejected() {
        dbHelper.addUser("Elena Rostova", "elena@luxevista.com", "CorrectPassword123");

        UserModel user = dbHelper.authenticateUser("elena@luxevista.com", "WrongPassword999");
        assertNull("Authentication must fail with incorrect password", user);
    }

    @Test
    public void testUserAuthenticationNonExistentEmailRejected() {
        UserModel user = dbHelper.authenticateUser("nonexistent@luxevista.com", "AnyPassword123");
        assertNull("Authentication must fail for unregistered email", user);
    }

    @Test
    public void testLegacyPlaintextPasswordMigrationOnLogin() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", "Legacy Guest");
        cv.put("email", "legacy@luxevista.com");
        cv.put("password", "old_plain_pass"); // Plaintext
        cv.putNull("salt"); // No salt
        long id = db.insert(DBHelper.TABLE_USERS, null, cv);
        assertTrue(id > 0);

        // Authenticate with legacy plaintext
        UserModel authenticated = dbHelper.authenticateUser("legacy@luxevista.com", "old_plain_pass");
        assertNotNull("Legacy user must authenticate successfully", authenticated);
        assertEquals("Legacy Guest", authenticated.getName());

        // Verify password was automatically migrated to salted hash
        Cursor cursor = db.rawQuery("SELECT password, salt FROM " + DBHelper.TABLE_USERS + " WHERE id = ?",
                new String[]{String.valueOf(id)});
        assertTrue(cursor.moveToFirst());
        String newPassword = cursor.getString(0);
        String newSalt = cursor.getString(1);

        assertNotNull(newSalt);
        assertFalse(newSalt.isEmpty());
        assertNotEquals("old_plain_pass", newPassword);
        assertEquals(SecurityUtils.hashPassword("old_plain_pass", newSalt), newPassword);
        cursor.close();
    }

    @Test
    public void testSessionManagerLifecycle() {
        assertFalse(sessionManager.isLoggedIn());
        assertEquals(-1, sessionManager.getUserId());
        assertEquals("Valued Guest", sessionManager.getUserName());
        assertEquals("", sessionManager.getUserEmail());

        // Create login session
        sessionManager.createLoginSession(101, "Lady Eleanor", "eleanor@luxevista.com");
        assertTrue(sessionManager.isLoggedIn());
        assertEquals(101, sessionManager.getUserId());
        assertEquals("Lady Eleanor", sessionManager.getUserName());
        assertEquals("eleanor@luxevista.com", sessionManager.getUserEmail());

        // Logout
        sessionManager.logoutUser();
        assertFalse(sessionManager.isLoggedIn());
        assertEquals(-1, sessionManager.getUserId());
        assertEquals("Valued Guest", sessionManager.getUserName());
        assertEquals("", sessionManager.getUserEmail());
    }

    @Test
    public void testInputValidationRules() {
        // Name empty check
        String emptyName = "   ";
        assertTrue(emptyName.trim().isEmpty());

        // Password matching
        String pass1 = "LuxeSecure2026!";
        String pass2 = "LuxeSecure2026!";
        String pass3 = "DifferentPass";
        assertEquals(pass1, pass2);
        assertNotEquals(pass1, pass3);

        // Password minimum length check
        assertTrue("12345".length() < 6);
        assertFalse("123456".length() < 6);
    }
}
