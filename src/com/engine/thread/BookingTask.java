package com.engine.thread;

import com.engine.exception.InvalidBookingException;
import com.engine.exception.SeatUnavailableException;
import com.engine.model.Booking;
import com.engine.model.Passenger;
import com.engine.service.ReservationService;

/**
 * A {@link Runnable} task that simulates a single passenger attempting to
 * book a seat. Multiple instances are submitted to an {@code ExecutorService}
 * to exercise the thread-safety of {@link ReservationService}.
 */
public class BookingTask implements Runnable {

    private final ReservationService service;
    private final Passenger passenger;
    private final String transportId;

    public BookingTask(ReservationService service, Passenger passenger,
                       String transportId) {
        this.service = service;
        this.passenger = passenger;
        this.transportId = transportId;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();

        try {
            Booking booking = service.bookSeat(passenger, transportId);

            System.out.printf("  [%s] SUCCESS  %s booked seat %d on %s  (Booking: %s)%n",
                    threadName,
                    passenger.getName(),
                    booking.getSeatNumber(),
                    booking.getTransport().getName(),
                    booking.getBookingId());

        } catch (SeatUnavailableException e) {
            // Expected when more threads compete than seats remain.
            System.out.printf("  [%s] DENIED   %s -> %s%n",
                    threadName, passenger.getName(), e.getMessage());

        } catch (InvalidBookingException e) {
            // Input validation failure — should not happen in a well-formed
            // stress test, but we handle it defensively.
            System.out.printf("  [%s] ERROR    %s -> Invalid booking: %s%n",
                    threadName, passenger.getName(), e.getMessage());

        } catch (Exception e) {
            // Catch-all for any unforeseen runtime problem.
            System.out.printf("  [%s] UNEXPECTED ERROR  %s -> %s%n",
                    threadName, passenger.getName(), e.getMessage());
        }
    }
}
