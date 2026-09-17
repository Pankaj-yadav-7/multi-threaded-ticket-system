package com.engine.model;

// Concrete transport representing a train service.

public class Train extends Transport {

    private final String trainClass;   // e.g. "Sleeper", "AC First", "General"
    private final int numberOfStops;

    public Train(String id, String name, String source, String destination,
                 int totalSeats, double fare, String trainClass, int numberOfStops) {
        super(id, name, source, destination, totalSeats, fare);
        this.trainClass = trainClass;
        this.numberOfStops = numberOfStops;
    }

    public String getTrainClass() { return trainClass; }

    public int getNumberOfStops() { return numberOfStops; }

    @Override
    public String getType() { return "TRAIN"; }

    @Override
    public String getDetails() {
        return String.format("Class: %s | Stops: %d", trainClass, numberOfStops);
    }
}
