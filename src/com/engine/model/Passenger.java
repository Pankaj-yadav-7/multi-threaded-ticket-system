package com.engine.model;

//Represents a passenger who may hold one or more bookings.
 
public class Passenger {

    private final String id;
    private final String name;
    private final String email;
    private final String phone;

    public Passenger(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return String.format("Passenger[%s] %s (%s, %s)", id, name, email, phone);
    }
}
