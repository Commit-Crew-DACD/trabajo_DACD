package org.ulpgc.dacd.model;

public class Event {
    private final String id;
    private final String name;
    private final String city;
    private final String venue;
    private final String date;
    private final String time;
    private final String url;

    public Event(String id, String name, String city, String venue, String date, String time, String url) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.venue = venue;
        this.date = date;
        this.time = time;
        this.url = url;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getVenue() { return venue; }
    public String getDate() { return date; }
    public String getUrl() { return url; }
    public String getTime() { return time; }
}