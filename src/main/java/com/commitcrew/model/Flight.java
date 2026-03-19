package com.commitcrew.model;

public class Flight {
    private final String origin;
    private final String destination;
    private final String departureDate;
    private final double price;

    public Flight(String origin, String destination, String departureDate, double price) {
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.price = price;
    }

    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getDepartureDate() { return departureDate; }
    public double getPrice() { return price; }
}