package com.commitcrew.model;

public class Event {
    private final String name;
    private final String city;
    private final String date;
    private final double entryPrice;

    public Event(String name, String city, String date, double entryPrice) {
        this.name = name;
        this.city = city;
        this.date = date;
        this.entryPrice = entryPrice;
    }

    public String getName() { return name; }
    public String getCity() { return city; }
    public String getDate() { return date; }
    public double getEntryPrice() { return entryPrice; }
}