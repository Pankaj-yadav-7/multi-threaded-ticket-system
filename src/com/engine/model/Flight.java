package com.engine.model;
//Concrete transport representing an airline flight.

public class Flight extends Transport {

    private final String airline;
    private final String aircraftModel;

    public Flight(String id, String name, String source, String destination,
                  int totalSeats, double fare, String airline, String aircraftModel) {
        super(id, name, source, destination, totalSeats, fare);
        this.airline = airline;
        this.aircraftModel = aircraftModel;
    }

    public String getAirline() { return airline; }

    public String getAircraftModel() { return aircraftModel; }

    @Override
    public String getType() { return "FLIGHT"; }

    @Override
    public String getDetails() {
        return String.format("Airline: %s | Aircraft: %s", airline, aircraftModel);
    }
}
