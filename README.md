# Multi-Threaded Flight & Rail Reservation Engine

A high-concurrency command-line booking engine built with standard Java SE. It models transport seat inventory for flights and trains, handles real-time ticket allocation under thread contention, resolves race conditions with explicit locking, implements a robust custom exception hierarchy, persists booking records to CSV, and exports passenger itineraries — all from a terminal menu.

---

## Objectives

- Demonstrate **thread-safe shared-state management** using `ReentrantLock` and thread-safe collections (`ConcurrentHashMap`, `CopyOnWriteArrayList`).
- Implement a **custom exception hierarchy** (checked + unchecked) that propagates meaningful errors across service, thread, and UI layers.
- Apply core **OOP principles**: encapsulation, inheritance (abstract `Transport` → `Flight`, `Train`), polymorphism, and clean package decomposition.
- Persist data with **Java character and byte I/O streams** (`BufferedWriter`, `BufferedOutputStream`, `BufferedReader`).
- Provide a **stress-test mode** that launches more concurrent threads than available seats, proving the locking strategy prevents overselling.

---

## Key Features

| Feature | Highlight |
|---|---|
| View Routes | Lists all flight and train routes with live seat availability. |
| Interactive Booking | Collects passenger details and books a seat with immediate confirmation. |
| Stress Test | Spawns N threads via `ExecutorService`; threads that lose the race receive `SeatUnavailableException`. |
| Cancel / Refund | Cancels a confirmed booking and atomically restores the seat; rejects invalid or duplicate cancellations. |
| Export Itinerary | Writes a formatted itinerary to a text file (byte stream) or exports all confirmed bookings at once. |
| Save Records | Persists the full booking log to `data/bookings.csv` (character stream) and reads it back to the console. |

### Exception Handling Highlights

| Exception | Type | Thrown When |
|---|---|---|
| `SeatUnavailableException` | Checked (`extends Exception`) | A transport has zero remaining seats at lock-acquisition time. |
| `InvalidBookingException` | Unchecked (`extends RuntimeException`) | Null/blank passenger data, unknown transport/booking IDs, double cancellation. |

Both are actively caught in `ReservationService`, `BookingTask`, and every `App` menu handler with specific recovery messages.

### Concurrency Highlights

- **Per-transport `ReentrantLock`** (fair mode) guards every seat decrement/increment — no global lock bottleneck.
- **`ConcurrentHashMap`** stores routes and locks for safe concurrent reads.
- **`CopyOnWriteArrayList`** stores bookings for snapshot-consistent iteration while writers append.
- **`AtomicInteger`** generates monotonically increasing booking IDs without synchronization.
- **`ExecutorService` with bounded thread pool** runs the stress test and waits via `awaitTermination`.

---

## Repository Directory Tree

```
multi-threaded-ticket-system/
├── src/
│   └── com/
│       └── engine/
│           ├── App.java                          # CLI entry point (menu loop)
│           ├── exception/
│           │   ├── SeatUnavailableException.java # Checked: no seats left
│           │   └── InvalidBookingException.java  # Unchecked: bad input
│           ├── model/
│           │   ├── Transport.java                # Abstract base (OOP root)
│           │   ├── Flight.java                   # Concrete: airline flights
│           │   ├── Train.java                    # Concrete: rail services
│           │   ├── Passenger.java                # Passenger value object
│           │   └── Booking.java                  # Booking transaction record
│           ├── service/
│           │   └── ReservationService.java       # Thread-safe booking engine
│           ├── thread/
│           │   └── BookingTask.java              # Runnable for concurrent booking
│           └── dao/
│               └── FileStorageService.java       # File I/O persistence
├── bin/                                          # Compiled .class output
├── data/                                         # Runtime: CSVs & itineraries
├── statement.md                                  # Problem statement
└── README.md                                     # This file
```

**10 Java source files** across 6 packages, producing 12 class files (including the `Booking.Status` enum).

---

## Compilation & Execution

### Prerequisites

- **JDK 17 or later** (`javac` and `java` on PATH).
- No external libraries or build tools required.

### Compile

```bash
cd multi-threaded-ticket-system

javac -d bin src/com/engine/exception/*.java \
              src/com/engine/model/*.java \
              src/com/engine/service/*.java \
              src/com/engine/thread/*.java \
              src/com/engine/dao/*.java \
              src/com/engine/App.java
```

### Run

```bash
java -cp bin com.engine.App
```

You will see:

```
=========================================================
   Multi-Threaded Flight & Rail Reservation Engine
=========================================================

---------------------------------------------------------
  1. View Available Routes
  2. Book Ticket (Interactive)
  3. Run Multi-Threaded Stress Test
  4. Cancel / Refund Booking
  5. Export Itinerary
  6. View All Bookings
  7. Save Records to File
  8. Exit
---------------------------------------------------------
  Select option:
```

---

## Interactive Menu Guide

| Option | What It Does |
|---|---|
| **1 — View Available Routes** | Prints every flight and train with ID, name, route, live seat count, fare, and transport-specific details. |
| **2 — Book Ticket** | Prompts for a transport ID, passenger name, email, and phone. On success, prints the confirmed booking with its unique ID and seat number. On failure, prints the specific exception message. |
| **3 — Stress Test** | Asks for a transport ID, then launches `(availableSeats + 5)` concurrent threads. Each thread attempts to book one seat. Watch the interleaved SUCCESS/DENIED output to verify that exactly `availableSeats` bookings succeed and the rest are denied — never more. |
| **4 — Cancel / Refund** | Prompts for a booking ID (e.g., `BK1001`). Cancels the booking, restores the seat to inventory, and confirms the refund. Rejects unknown IDs and double cancellations with a descriptive error. |
| **5 — Export Itinerary** | Enter a booking ID to export a single itinerary to `data/itinerary_BKxxxx.txt`, or type `all` to export every confirmed booking. The itinerary is also previewed on screen. |
| **6 — View All Bookings** | Lists every booking (confirmed and cancelled) with its current status. |
| **7 — Save Records** | Writes all bookings to `data/bookings.csv` and prints the CSV contents back to the console. |
| **8 — Exit** | Terminates the application. |

---

## Concurrency Test Verification

When you run option **3** against a route with, say, 5 remaining seats, the expected output looks like:

```
  Launching 10 concurrent booking threads for SkyBound 101 (5 seats left)...

  [pool-1-thread-2] SUCCESS  StressUser-2 booked seat 1 on SkyBound 101  (Booking: BK1001)
  [pool-1-thread-5] SUCCESS  StressUser-5 booked seat 2 on SkyBound 101  (Booking: BK1002)
  [pool-1-thread-1] SUCCESS  StressUser-1 booked seat 3 on SkyBound 101  (Booking: BK1003)
  [pool-1-thread-3] SUCCESS  StressUser-3 booked seat 4 on SkyBound 101  (Booking: BK1004)
  [pool-1-thread-4] SUCCESS  StressUser-4 booked seat 5 on SkyBound 101  (Booking: BK1005)
  [pool-1-thread-6] DENIED   StressUser-6 -> All 5 seats on SkyBound 101 are fully booked.
  [pool-1-thread-7] DENIED   StressUser-7 -> All 5 seats on SkyBound 101 are fully booked.
  [pool-1-thread-8] DENIED   StressUser-8 -> All 5 seats on SkyBound 101 are fully booked.
  [pool-1-thread-9] DENIED   StressUser-9 -> All 5 seats on SkyBound 101 are fully booked.
  [pool-1-thread-10] DENIED  StressUser-10 -> All 5 seats on SkyBound 101 are fully booked.

  Stress test complete. Seats remaining on SkyBound 101: 0
```

**Key invariants verified:**

1. Exactly 5 SUCCESS lines (never 6 or more — no overselling).
2. Exactly 5 DENIED lines (each receiving a `SeatUnavailableException`).
3. Final seat count is 0 (never negative).
4. Thread names in the output show true parallel execution.

---

## Syllabus Coverage Summary

| Topic | Where It Appears |
|---|---|
| Custom Checked Exception | `SeatUnavailableException` — thrown in `ReservationService.bookSeat()`, caught in `BookingTask.run()` and `App.bookInteractive()` |
| Custom Unchecked Exception | `InvalidBookingException` — thrown in `ReservationService` for validation, caught in every `App` menu handler |
| `try-catch-finally` | `ReservationService` (lock release in `finally`), `App` (multi-catch per menu option), `FileStorageService` (try-with-resources) |
| `throw` / `throws` | Service methods declare `throws SeatUnavailableException`; DAO methods declare `throws IOException` |
| Abstract Class | `Transport` — defines shared fields, getters, and abstract methods `getType()`, `getDetails()` |
| Inheritance | `Flight extends Transport`, `Train extends Transport` |
| Polymorphism | `getAllRoutes()` returns `List<Transport>`; `toString()` dispatches to subclass `getDetails()` |
| Encapsulation | All model fields are `private final` with getter access |
| `Runnable` / `ExecutorService` | `BookingTask implements Runnable`, submitted to a fixed thread pool in `App.runStressTest()` |
| `ReentrantLock` / synchronized | Per-transport fair lock in `ReservationService` guards seat allocation and cancellation |
| `ConcurrentHashMap` | Stores routes and per-route locks |
| `CopyOnWriteArrayList` | Stores booking history for concurrent-safe reads during stress tests |
| `AtomicInteger` | Thread-safe booking-ID generation |
| `List` / `ArrayList` / `Map` | Route seeding, query results, iteration throughout the application |
| Character I/O (`Writer/Reader`) | `FileStorageService.saveBookingsToCsv()` and `printFileContents()` |
| Byte I/O (`OutputStream`) | `FileStorageService.exportItinerary()` |
| Try-with-resources | Every I/O operation in `FileStorageService` |
