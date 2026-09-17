# Problem Statement

## Context

Real-world transport reservation systems face two classes of hard problems simultaneously:

1. **Concurrency conflicts.** When hundreds of passengers attempt to book the last few seats on a flight or train at the same instant, naïve implementations either oversell seats (race condition) or silently deadlock. A correct engine must serialize seat mutations per transport while keeping overall throughput high.

2. **Graceful failure handling.** A booking request can fail for many reasons — no seats left, an invalid route ID, a malformed passenger record, a file-system error while persisting the itinerary. Each failure mode requires a distinct, informative response rather than an opaque crash, which demands a structured custom-exception hierarchy.

This project builds a **Multi-Threaded Flight & Rail Reservation Engine** that addresses both problems in a single, fully functional command-line application.

---

## Project Scope

| Capability | Description |
|---|---|
| **Transport Inventory** | Manages a catalogue of `Flight` and `Train` routes, each with finite seat counts, fares, and transport-specific metadata (airline/aircraft for flights; class/stops for trains). |
| **Interactive Booking** | Lets an operator enter passenger details and book a seat on a chosen route, with immediate confirmation or a descriptive error. |
| **Multi-Threaded Stress Test** | Launches N concurrent booking threads (more than available seats) against a single route via `ExecutorService`, demonstrating that the locking strategy prevents overselling. |
| **Cancellation & Refund** | Cancels a confirmed booking by ID and atomically returns the seat to inventory. Rejects double-cancellation attempts with a clear exception. |
| **Custom Exception Hierarchy** | `SeatUnavailableException` (checked) and `InvalidBookingException` (unchecked) propagate through the service and thread layers, caught and reported at each boundary. |
| **File Persistence** | Booking records are saved to CSV via character streams (`BufferedWriter`); itineraries are exported via byte streams (`BufferedOutputStream`). Both use Java standard I/O with proper resource management. |

---

## Target Users

- **System administrators** setting up and validating a transport booking backend.
- **Travel operators** managing daily seat inventory and passenger manifests.
- **Students and evaluators** studying thread-safe design, exception hierarchies, and OOP architecture in a non-trivial Java application.

---

## High-Level Features Mapped to Implementation

| Feature | Primary Class(es) |
|---|---|
| Custom checked exception for sold-out routes | `SeatUnavailableException` |
| Custom unchecked exception for invalid input | `InvalidBookingException` |
| Abstract transport model with polymorphic subtypes | `Transport` → `Flight`, `Train` |
| Passenger and booking transaction models | `Passenger`, `Booking` |
| Thread-safe seat allocation with `ReentrantLock` | `ReservationService` |
| Concurrent booking simulation via `ExecutorService` | `BookingTask` |
| CSV persistence (character I/O) and itinerary export (byte I/O) | `FileStorageService` |
| Looped CLI menu with full error handling | `App` |
