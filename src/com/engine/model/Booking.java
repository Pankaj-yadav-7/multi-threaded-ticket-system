package com.engine.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable record of a single booking transaction.
 */
public class Booking {

    /** Booking lifecycle states. */
    public enum Status { CONFIRMED, CANCELLED }

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String bookingId;
    private final Passenger passenger;
    private final Transport transport;
    private final int seatNumber;
    private Status status;
    private final LocalDateTime timestamp;

    public Booking(String bookingId, Passenger passenger, Transport transport,
                   int seatNumber) {
        this.bookingId = bookingId;
        this.passenger = passenger;
        this.transport = transport;
        this.seatNumber = seatNumber;
        this.status = Status.CONFIRMED;
        this.timestamp = LocalDateTime.now();
    }

    /* ---- Getters ---- */

    public String getBookingId() { return bookingId; }

    public Passenger getPassenger() { return passenger; }

    public Transport getTransport() { return transport; }

    public int getSeatNumber() { return seatNumber; }

    public Status getStatus() { return status; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public String getFormattedTimestamp() { return timestamp.format(FMT); }

    /* ---- State transition ---- */

    public void cancel() { this.status = Status.CANCELLED; }

    /* ---- Serialisation helpers ---- */

    /**
     * Returns a CSV-safe line for file persistence.
     */
    public String toCsvLine() {
        return String.join(",",
                bookingId,
                passenger.getId(),
                passenger.getName(),
                transport.getId(),
                transport.getType(),
                transport.getSource() + " -> " + transport.getDestination(),
                String.valueOf(seatNumber),
                status.name(),
                getFormattedTimestamp());
    }

    /**
     * Returns a human-readable itinerary block.
     */
    public String toItinerary() {
        return "===================================================\n" +
               "              BOOKING ITINERARY\n" +
               "===================================================\n" +
               " Booking ID   : " + bookingId + "\n" +
               " Status       : " + status + "\n" +
               " Timestamp    : " + getFormattedTimestamp() + "\n" +
               "---------------------------------------------------\n" +
               " Passenger    : " + passenger.getName() + "\n" +
               " Email        : " + passenger.getEmail() + "\n" +
               " Phone        : " + passenger.getPhone() + "\n" +
               "---------------------------------------------------\n" +
               " Transport    : " + transport.getType() + " - " + transport.getName() + "\n" +
               " Route        : " + transport.getSource() + " -> " + transport.getDestination() + "\n" +
               " Seat Number  : " + seatNumber + "\n" +
               " Fare         : $" + String.format("%.2f", transport.getFare()) + "\n" +
               " Details      : " + transport.getDetails() + "\n" +
               "===================================================\n";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s on %s (Seat %d) - %s @ %s",
                bookingId, passenger.getName(), transport.getId(),
                seatNumber, status, getFormattedTimestamp());
    }
}
