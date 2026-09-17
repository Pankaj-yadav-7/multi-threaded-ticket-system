package com.engine.service;

import com.engine.exception.InvalidBookingException;
import com.engine.exception.SeatUnavailableException;
import com.engine.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe reservation service that manages transport inventory and bookings.
 *
 * <p>All seat-allocation operations acquire a per-transport {@link ReentrantLock}
 * so that concurrent {@code BookingTask} threads never oversell a route.
 * Bookings and routes are stored in thread-safe collections.</p>
 */
public class ReservationService {

    /* ---- Shared state ---- */

    /** All registered transport routes keyed by transport ID. */
    private final ConcurrentHashMap<String, Transport> routes = new ConcurrentHashMap<>();

    /** All bookings ever created, for listing and cancellation. */
    private final CopyOnWriteArrayList<Booking> bookings = new CopyOnWriteArrayList<>();

    /** Per-transport locks to serialise seat mutations. */
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /** Monotonic booking-ID counter (thread-safe). */
    private final AtomicInteger bookingCounter = new AtomicInteger(1000);

    /* ================================================================ */
    /*  Route management                                                */
    /* ================================================================ */

    /**
     * Seeds the system with a collection of transports.
     */
    public void loadRoutes(List<Transport> transports) {
        for (Transport t : transports) {
            routes.put(t.getId(), t);
            locks.put(t.getId(), new ReentrantLock(true)); // fair lock
        }
    }

    /**
     * Returns an unmodifiable snapshot of all routes.
     */
    public List<Transport> getAllRoutes() {
        return Collections.unmodifiableList(new ArrayList<>(routes.values()));
    }

    /**
     * Looks up a transport by ID.
     *
     * @throws InvalidBookingException if the ID does not match any route
     */
    public Transport findRoute(String transportId) {
        Transport t = routes.get(transportId);
        if (t == null) {
            throw new InvalidBookingException("transportId",
                    "No route found with ID: " + transportId);
        }
        return t;
    }

    /* ================================================================ */
    /*  Booking — thread-safe seat allocation                           */
    /* ================================================================ */

    /**
     * Books a seat for the given passenger on the specified transport.
     *
     * <p>This method acquires the transport-specific lock before checking and
     * decrementing the available-seat count so that two threads racing on the
     * last seat will never both succeed.</p>
     *
     * @param passenger   the passenger to book for
     * @param transportId the transport route identifier
     * @return the confirmed {@link Booking}
     * @throws SeatUnavailableException if the transport has no remaining seats
     * @throws InvalidBookingException  if the passenger or transport ID is invalid
     */
    public Booking bookSeat(Passenger passenger, String transportId)
            throws SeatUnavailableException {

        if (passenger == null) {
            throw new InvalidBookingException("passenger", "Passenger must not be null");
        }
        if (transportId == null || transportId.isBlank()) {
            throw new InvalidBookingException("transportId", "Transport ID must not be blank");
        }

        Transport transport = findRoute(transportId);          // may throw InvalidBookingException
        ReentrantLock lock = locks.get(transportId);

        lock.lock();
        try {
            if (transport.getAvailableSeats() <= 0) {
                throw new SeatUnavailableException(transportId,
                        "All " + transport.getTotalSeats() + " seats on "
                                + transport.getName() + " are fully booked.");
            }

            // Allocate the next seat number (total - available + 1).
            int seatNumber = transport.getTotalSeats() - transport.getAvailableSeats() + 1;
            transport.decrementSeat();

            String bookingId = "BK" + bookingCounter.incrementAndGet();
            Booking booking = new Booking(bookingId, passenger, transport, seatNumber);
            bookings.add(booking);

            return booking;

        } finally {
            lock.unlock();
        }
    }

    /* ================================================================ */
    /*  Cancellation                                                    */
    /* ================================================================ */

    /**
     * Cancels an existing booking by its ID, returning the released seat
     * back to the transport inventory.
     *
     * @param bookingId the booking identifier (e.g. "BK1001")
     * @return the cancelled {@link Booking}
     * @throws InvalidBookingException if the ID is blank, not found,
     *                                  or the booking is already cancelled
     */
    public Booking cancelBooking(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new InvalidBookingException("bookingId", "Booking ID must not be blank");
        }

        Booking target = null;
        for (Booking b : bookings) {
            if (b.getBookingId().equalsIgnoreCase(bookingId)) {
                target = b;
                break;
            }
        }

        if (target == null) {
            throw new InvalidBookingException("bookingId",
                    "No booking found with ID: " + bookingId);
        }
        if (target.getStatus() == Booking.Status.CANCELLED) {
            throw new InvalidBookingException("bookingId",
                    "Booking " + bookingId + " is already cancelled");
        }

        Transport transport = target.getTransport();
        ReentrantLock lock = locks.get(transport.getId());

        lock.lock();
        try {
            target.cancel();
            transport.incrementSeat();
        } finally {
            lock.unlock();
        }

        return target;
    }

    /* ================================================================ */
    /*  Query helpers                                                    */
    /* ================================================================ */

    /**
     * Returns all bookings (confirmed and cancelled).
     */
    public List<Booking> getAllBookings() {
        return Collections.unmodifiableList(new ArrayList<>(bookings));
    }

    /**
     * Finds a single booking by its ID.
     *
     * @throws InvalidBookingException if not found
     */
    public Booking findBooking(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new InvalidBookingException("bookingId", "Booking ID must not be blank");
        }
        for (Booking b : bookings) {
            if (b.getBookingId().equalsIgnoreCase(bookingId)) {
                return b;
            }
        }
        throw new InvalidBookingException("bookingId",
                "No booking found with ID: " + bookingId);
    }
}
