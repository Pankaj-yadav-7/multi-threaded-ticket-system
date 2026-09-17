package com.engine;

import com.engine.dao.FileStorageService;
import com.engine.exception.InvalidBookingException;
import com.engine.exception.SeatUnavailableException;
import com.engine.model.*;
import com.engine.service.ReservationService;
import com.engine.thread.BookingTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CLI entry point for the Multi-Threaded Flight & Rail Reservation Engine.
 *
 * <p>Presents a looped menu that lets an operator view routes, book tickets
 * interactively, run a concurrent stress test, cancel bookings, persist
 * records, and export itineraries.</p>
 */
public class App {

    private static final ReservationService service = new ReservationService();
    private static final FileStorageService storage = new FileStorageService();
    private static final Scanner scanner = new Scanner(System.in);

    // Passenger ID counter for interactive booking
    private static int passengerSeq = 1;


    // Bootstrap


    public static void main(String[] args) {

        seedRoutes();

        System.out.println("=========================================================");
        System.out.println("   Multi-Threaded Flight & Rail Reservation Engine");
        System.out.println("=========================================================");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewRoutes();
                case "2" -> bookInteractive();
                case "3" -> runStressTest();
                case "4" -> cancelBooking();
                case "5" -> exportItinerary();
                case "6" -> viewBookings();
                case "7" -> saveRecords();
                case "8" -> {
                    System.out.println("\n  Exiting. Thank you!");
                    running = false;
                }
                default -> System.out.println("\n  Invalid option. Please choose 1-8.");
            }
        }

        scanner.close();
    }


    // Menu  


    private static void printMenu() {
        System.out.println("\n---------------------------------------------------------");
        System.out.println("  1. View Available Routes");
        System.out.println("  2. Book Ticket (Interactive)");
        System.out.println("  3. Run Multi-Threaded Stress Test");
        System.out.println("  4. Cancel / Refund Booking");
        System.out.println("  5. Export Itinerary");
        System.out.println("  6. View All Bookings");
        System.out.println("  7. Save Records to File");
        System.out.println("  8. Exit");
        System.out.println("---------------------------------------------------------");
        System.out.print("  Select option: ");
    }

    //  1. View routes   


    private static void viewRoutes() {
        System.out.println("\n  === Available Transport Routes ===\n");
        List<Transport> routes = service.getAllRoutes();
        if (routes.isEmpty()) {
            System.out.println("  No routes loaded.");
            return;
        }
        for (Transport t : routes) {
            System.out.println("  " + t);
        }
    }

  
    // 2. Interactive booking 
   

    private static void bookInteractive() {
        System.out.println("\n  === Book a Ticket ===\n");

        try {
            viewRoutes();
            System.out.print("\n  Enter Transport ID: ");
            String transportId = scanner.nextLine().trim();

            System.out.print("  Enter your name: ");
            String name = scanner.nextLine().trim();
            if (name.isBlank()) {
                throw new InvalidBookingException("name", "Passenger name cannot be blank");
            }

            System.out.print("  Enter email: ");
            String email = scanner.nextLine().trim();

            System.out.print("  Enter phone: ");
            String phone = scanner.nextLine().trim();

            String passengerId = "PAX" + (passengerSeq++);
            Passenger passenger = new Passenger(passengerId, name, email, phone);

            Booking booking = service.bookSeat(passenger, transportId);

            System.out.println("\n  BOOKING CONFIRMED!");
            System.out.println("  " + booking);

        } catch (SeatUnavailableException e) {
            System.out.println("\n  BOOKING FAILED: " + e.getMessage());
        } catch (InvalidBookingException e) {
            System.out.println("\n  INVALID INPUT: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n  UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    
    //  3. Multi-threaded stress test  
   

    private static void runStressTest() {
        System.out.println("\n  === Multi-Threaded Booking Stress Test ===\n");

        System.out.print("  Enter Transport ID to stress-test: ");
        String transportId = scanner.nextLine().trim();

        try {
            Transport transport = service.findRoute(transportId);
            int available = transport.getAvailableSeats();
            // Launch more threads than seats to guarantee contention.
            int threadCount = available + 5;
            System.out.printf("  Launching %d concurrent booking threads for %s (%d seats left)...%n%n",
                    threadCount, transport.getName(), available);

            ExecutorService executor = Executors.newFixedThreadPool(
                    Math.min(threadCount, 20));

            for (int i = 1; i <= threadCount; i++) {
                Passenger p = new Passenger(
                        "ST" + i,
                        "StressUser-" + i,
                        "stress" + i + "@test.com",
                        "555-00" + String.format("%02d", i));
                executor.submit(new BookingTask(service, p, transportId));
            }

            executor.shutdown();
            boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);
            if (!finished) {
                System.out.println("\n  WARNING: Stress test timed out after 30 seconds.");
                executor.shutdownNow();
            }

            System.out.printf("%n  Stress test complete. Seats remaining on %s: %d%n",
                    transport.getName(), transport.getAvailableSeats());

        } catch (InvalidBookingException e) {
            System.out.println("\n  Invalid transport ID: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("\n  Stress test interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("\n  Unexpected error during stress test: " + e.getMessage());
        }
    }


    // 4. Cancel booking
 

    private static void cancelBooking() {
        System.out.println("\n  === Cancel / Refund a Booking ===\n");

        System.out.print("  Enter Booking ID to cancel: ");
        String bookingId = scanner.nextLine().trim();

        try {
            Booking cancelled = service.cancelBooking(bookingId);
            System.out.println("\n  BOOKING CANCELLED & REFUNDED:");
            System.out.println("  " + cancelled);
        } catch (InvalidBookingException e) {
            System.out.println("\n  CANCELLATION FAILED: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n  UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    // 5. Export itinerary

    private static void exportItinerary() {
        System.out.println("\n  === Export Itinerary ===\n");

        System.out.print("  Enter Booking ID to export (or 'all' for bulk export): ");
        String input = scanner.nextLine().trim();

        try {
            if (input.equalsIgnoreCase("all")) {
                storage.exportAllItineraries(service.getAllBookings());
            } else {
                Booking booking = service.findBooking(input);
                String fileName = "itinerary_" + booking.getBookingId() + ".txt";
                storage.exportItinerary(booking, fileName);
                System.out.println("\n  --- Itinerary Preview ---");
                System.out.println(booking.toItinerary());
            }
        } catch (InvalidBookingException e) {
            System.out.println("\n  EXPORT FAILED: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("\n  FILE ERROR: Could not write itinerary — " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n  UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    // 6. View all bookings


    private static void viewBookings() {
        System.out.println("\n  === All Bookings ===\n");
        List<Booking> all = service.getAllBookings();
        if (all.isEmpty()) {
            System.out.println("  No bookings yet.");
            return;
        }
        for (Booking b : all) {
            System.out.println("  " + b);
        }
    }

    
    //  7. Save records to CSV 
   

    private static void saveRecords() {
        System.out.println("\n  === Save Booking Records ===\n");
        try {
            storage.saveAndDisplay(service.getAllBookings());
        } catch (IOException e) {
            System.out.println("\n  FILE ERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n  UNEXPECTED ERROR: " + e.getMessage());
        }
    }

        //  Seed data

    private static void seedRoutes() {
        List<Transport> routes = new ArrayList<>();

        routes.add(new Flight("FL101", "SkyBound 101",
                "New York (JFK)", "Los Angeles (LAX)", 5, 349.99,
                "SkyBound Airlines", "Boeing 737"));

        routes.add(new Flight("FL202", "AeroSwift 202",
                "Chicago (ORD)", "Miami (MIA)", 8, 279.50,
                "AeroSwift Corp", "Airbus A320"));

        routes.add(new Flight("FL303", "JetStream 303",
                "San Francisco (SFO)", "Seattle (SEA)", 3, 189.00,
                "JetStream Air", "Embraer E190"));

        routes.add(new Train("TR401", "RailExpress 401",
                "Boston", "Washington D.C.", 6, 89.99,
                "AC First", 4));

        routes.add(new Train("TR502", "MetroLink 502",
                "Denver", "Salt Lake City", 4, 59.50,
                "Sleeper", 3));

        routes.add(new Train("TR603", "CoastRider 603",
                "Los Angeles", "San Diego", 10, 35.00,
                "General", 2));

        service.loadRoutes(routes);
    }
}
