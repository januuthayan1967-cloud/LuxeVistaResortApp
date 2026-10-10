package com.example.luxevistaresortapp;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = "app/src/main/AndroidManifest.xml")
public class ServiceHistoryAndPdfVoucherRobolectricTest {

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
    public void testServiceBookingModelPropertiesAndFormattedReference() {
        ServiceBookingModel model = new ServiceBookingModel(
                42,
                "Spa Treatments",
                "Alexander Wright",
                "15 Oct 2026",
                "02:00 PM",
                "alexander.wright@luxevista.com"
        );

        assertEquals(42, model.getId());
        assertEquals("Spa Treatments", model.getServiceName());
        assertEquals("Alexander Wright", model.getCustomerName());
        assertEquals("15 Oct 2026", model.getDate());
        assertEquals("02:00 PM", model.getTime());
        assertEquals("alexander.wright@luxevista.com", model.getUserEmail());
        assertEquals("SRV-000042", model.getFormattedReference());
    }

    @Test
    public void testServiceReservationRetrievalAndUserOwnershipIsolation() {
        String userA = "alice@luxevista.com";
        String userB = "bob@luxevista.com";

        // Insert service bookings for User A
        boolean insertedA1 = dbHelper.addServiceBooking("Spa Treatments", "Alice Smith", "12 Oct 2026", "10:00 AM", userA);
        boolean insertedA2 = dbHelper.addServiceBooking("Fine Dining", "Alice Smith", "13 Oct 2026", "08:00 PM", userA);
        assertTrue(insertedA1);
        assertTrue(insertedA2);

        // Insert service booking for User B
        boolean insertedB = dbHelper.addServiceBooking("Poolside Cabanas", "Bob Jones", "14 Oct 2026", "12:00 PM", userB);
        assertTrue(insertedB);

        // Fetch User A service bookings (typed list)
        List<ServiceBookingModel> listA = dbHelper.getServiceBookingListForUser(userA);
        assertNotNull(listA);
        assertEquals(2, listA.size());

        for (ServiceBookingModel item : listA) {
            assertEquals(userA, item.getUserEmail());
            assertNotEquals("Poolside Cabanas", item.getServiceName());
            assertNotEquals("Bob Jones", item.getCustomerName());
        }

        // Fetch User B service bookings (typed list)
        List<ServiceBookingModel> listB = dbHelper.getServiceBookingListForUser(userB);
        assertNotNull(listB);
        assertEquals(1, listB.size());
        assertEquals("Poolside Cabanas", listB.get(0).getServiceName());
        assertEquals("Bob Jones", listB.get(0).getCustomerName());
        assertEquals(userB, listB.get(0).getUserEmail());

        // Case-insensitivity check
        List<ServiceBookingModel> listAUpper = dbHelper.getServiceBookingListForUser("ALICE@LUXEVISTA.COM");
        assertNotNull(listAUpper);
        assertEquals(2, listAUpper.size());
    }

    @Test
    public void testEmptyServiceHistoryHandling() {
        String newUser = "empty_user@luxevista.com";
        List<ServiceBookingModel> emptyList = dbHelper.getServiceBookingListForUser(newUser);
        assertNotNull("List must not be null for user with no bookings", emptyList);
        assertTrue("List must be empty", emptyList.isEmpty());
    }

    @Test
    public void testPdfCurrencyAndPriceFormatting() {
        assertEquals("Rs. 125,000", PdfVoucherGenerator.formatCurrency(125000L));
        assertEquals("Rs. 0", PdfVoucherGenerator.formatCurrency(0L));
        assertEquals("Rs. 1,500,000", PdfVoucherGenerator.formatCurrency(1500000L));

        assertEquals(125000L, PdfVoucherGenerator.parseNumericPrice("Rs. 125,000"));
        assertEquals(45000L, PdfVoucherGenerator.parseNumericPrice("45000"));
        assertEquals(0L, PdfVoucherGenerator.parseNumericPrice(null));
        assertEquals(0L, PdfVoucherGenerator.parseNumericPrice("Free"));

        assertEquals("LVR-2026-0001", PdfVoucherGenerator.sanitizeFileName("LVR-2026-0001"));
        assertEquals("LVR_2026_0001", PdfVoucherGenerator.sanitizeFileName("LVR/2026:0001"));
        assertEquals("Voucher", PdfVoucherGenerator.sanitizeFileName(null));
    }

    @Test
    public void testPdfFileGenerationAndFileProviderUri() throws IOException {
        BookingModel testBooking = new BookingModel(
                101,
                "Ocean View Suite",
                "10/10/2026",
                "15/10/2026",
                "125000",
                "guest@luxevista.com",
                "LVR-2026-000101",
                5,
                2,
                1,
                "RESERVED",
                "2026-10-10 10:00:00"
        );

        File pdfFile = PdfVoucherGenerator.generateVoucherPdf(context, testBooking, "Alexander Wright");

        assertNotNull("Generated PDF file must not be null", pdfFile);
        assertTrue("Generated PDF file must exist", pdfFile.exists());
        assertTrue("Generated PDF file must not be empty", pdfFile.length() > 0);
        assertTrue("File name must end with .pdf", pdfFile.getName().endsWith(".pdf"));
        assertTrue("File name must contain booking reference", pdfFile.getName().contains("LVR-2026-000101"));

        // FileProvider URI verification
        Uri contentUri = PdfVoucherGenerator.getVoucherContentUri(context, pdfFile);
        assertNotNull("FileProvider content URI must not be null", contentUri);
        assertEquals("content", contentUri.getScheme());
        assertEquals("com.example.luxevistaresortapp.fileprovider", contentUri.getAuthority());
        assertTrue(contentUri.getPath().contains("LVR-2026-000101"));
    }

    @Test
    public void testPdfSecurityOwnershipIsolation() {
        String authorizedUser = "owner@luxevista.com";
        String unauthorizedUser = "intruder@luxevista.com";

        long bookingId = dbHelper.addBookingAtomic(
                "Royal Presidential Suite",
                "20/10/2026",
                "25/10/2026",
                "250000",
                authorizedUser,
                5,
                2,
                0
        );
        assertTrue(bookingId > 0);

        // Fetching with authorized user must succeed
        BookingModel authorizedBooking = dbHelper.getBookingModelById(bookingId, authorizedUser);
        assertNotNull(authorizedBooking);
        assertEquals("Royal Presidential Suite", authorizedBooking.getRoomType());
        assertTrue(authorizedBooking.getBookingReference().contains("LVR-"));

        // Fetching with unauthorized user must return null (row-level isolation prevents cross-account leak)
        BookingModel leakedBooking = dbHelper.getBookingModelById(bookingId, unauthorizedUser);
        assertNull("Cross-user access must be blocked strictly by SQLite query", leakedBooking);
    }
}
