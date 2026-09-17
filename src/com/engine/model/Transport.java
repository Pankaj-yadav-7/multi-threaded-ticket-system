package com.engine.model;

// Abstract base class for all transport types (flights, trains, etc.).
// Encapsulates common inventory and route information.

public abstract class Transport {

    private final String id;
    private final String name;
    private final String source;
    private final String destination;
    private final int totalSeats;
    private int availableSeats;
    private final double fare;

    protected Transport(String id, String name, String source, String destination,
                        int totalSeats, double fare) {
        this.id = id;
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.fare = fare;
    }

    // Getters

    public String getId() { return id; }

    public String getName() { return name; }

    public String getSource() { return source; }

    public String getDestination() { return destination; }

    public int getTotalSeats() { return totalSeats; }

    public int getAvailableSeats() { return availableSeats; }

    public double getFare() { return fare; }

    // Seat management (package-visible for service layer)

    public void decrementSeat() { availableSeats--; }

    public void incrementSeat() { availableSeats++; }

    // Polymorphic display

    //Returns a transport-type label used in listings and itineraries.
    
    public abstract String getType();

    //Returns a detail line specific to the concrete transport subclass.
    
    public abstract String getDetails();

    @Override
    public String toString() {
        return String.format("[%s] %s | %s -> %s | Seats: %d/%d | Fare: $%.2f | %s",
                id, name, source, destination, availableSeats, totalSeats, fare, getDetails());
    }
}
