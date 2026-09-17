package com.engine.dao;

import com.engine.model.Booking;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Handles all file-based persistence for booking records and itineraries
 * using standard Java character and byte I/O streams.
 *
 * <p>CSV booking logs are written with a {@link BufferedWriter} (character stream).
 * Itinerary exports use a {@link BufferedOutputStream} (byte stream) to
 * demonstrate both I/O families.</p>
 */
public class FileStorageService {

    private static final String CSV_HEADER =
            "BookingID,PassengerID,PassengerName,TransportID,Type,Route,Seat,Status,Timestamp";

    private static final String DATA_DIR = "data";

    public FileStorageService() {
        // Ensure the data directory exists.
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /* ================================================================ */
    /*  CSV booking log — Character stream (Writer)                     */
    /* ================================================================ */

    /**
     * Persists all bookings to a CSV file using character I/O.
     *
     * @param bookings the complete booking list
     * @param fileName file name inside the data directory (e.g. "bookings.csv")
     * @throws IOException if the file cannot be written
     */
    public void saveBookingsToCsv(List<Booking> bookings, String fileName)
            throws IOException {

        File file = new File(DATA_DIR, fileName);

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(file, StandardCharsets.UTF_8))) {

            writer.write(CSV_HEADER);
            writer.newLine();

            for (Booking b : bookings) {
                writer.write(b.toCsvLine());
                writer.newLine();
            }
        }
        // IOException propagates to the caller for handling.

        System.out.println("  Bookings saved to " + file.getAbsolutePath());
    }

    /* ================================================================ */
    /*  Itinerary export — Byte stream (OutputStream)                   */
    /* ================================================================ */

    /**
     * Exports a single booking's itinerary to a text file using byte-level I/O.
     *
     * @param booking  the booking to export
     * @param fileName file name inside the data directory
     * @throws IOException if the file cannot be written
     */
    public void exportItinerary(Booking booking, String fileName)
            throws IOException {

        File file = new File(DATA_DIR, fileName);
        byte[] content = booking.toItinerary().getBytes(StandardCharsets.UTF_8);

        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(file))) {
            bos.write(content);
            bos.flush();
        }

        System.out.println("  Itinerary exported to " + file.getAbsolutePath());
    }

    /* ================================================================ */
    /*  Read back — Character stream (Reader)                           */
    /* ================================================================ */

    /**
     * Reads and prints the contents of a previously saved file.
     *
     * @param fileName file name inside the data directory
     * @throws IOException if the file cannot be read
     */
    public void printFileContents(String fileName) throws IOException {
        File file = new File(DATA_DIR, fileName);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }

        try (BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
        }
    }

    /* ================================================================ */
    /*  Bulk itinerary export                                           */
    /* ================================================================ */

    /**
     * Exports itineraries for all confirmed bookings.
     *
     * @param bookings the complete booking list (cancelled ones are skipped)
     * @throws IOException if any file operation fails
     */
    public void exportAllItineraries(List<Booking> bookings) throws IOException {
        int exported = 0;
        for (Booking b : bookings) {
            if (b.getStatus() == Booking.Status.CONFIRMED) {
                exportItinerary(b, "itinerary_" + b.getBookingId() + ".txt");
                exported++;
            }
        }
        System.out.println("  Total itineraries exported: " + exported);
    }

    /**
     * Persists all bookings and then reads back the CSV to the console.
     *
     * @param bookings the complete booking list
     * @throws IOException if any file operation fails
     */
    public void saveAndDisplay(List<Booking> bookings) throws IOException {
        String csvFile = "bookings.csv";
        saveBookingsToCsv(bookings, csvFile);
        System.out.println("\n  --- Saved Booking Records ---");
        printFileContents(csvFile);
    }
}
