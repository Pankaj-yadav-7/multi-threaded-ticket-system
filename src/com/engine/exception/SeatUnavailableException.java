package com.engine.exception;

/**
 * Checked exception thrown when a passenger attempts to book a seat
 * on a transport that has zero remaining available seats.
 */
public class SeatUnavailableException extends Exception {

    private final String transportId;

    public SeatUnavailableException(String transportId) {
        super("No seats available on transport " + transportId);
        this.transportId = transportId;
    }

    public SeatUnavailableException(String transportId, String message) {
        super(message);
        this.transportId = transportId;
    }

    public String getTransportId() {
        return transportId;
    }
}
