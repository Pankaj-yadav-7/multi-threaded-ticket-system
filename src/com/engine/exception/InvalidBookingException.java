package com.engine.exception;

/**
 * Unchecked exception thrown when a booking operation receives invalid input,
 * such as a null passenger, a malformed booking ID during cancellation,
 * or a reference to a non-existent transport route.
 */
public class InvalidBookingException extends RuntimeException {

    private final String field;

    public InvalidBookingException(String message) {
        super(message);
        this.field = null;
    }

    public InvalidBookingException(String field, String message) {
        super(message);
        this.field = field;
    }

    public InvalidBookingException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
    }

    /**
     * Returns the name of the field or parameter that caused the validation
     * failure, or null if the error is not field-specific.
     */
    public String getField() {
        return field;
    }
}
