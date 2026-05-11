package org.ulpgc.dacd.model;

public class Event {
    private final String id;
    private final String name;
    private final String city;
    private final String venue;
    private final String date;
    private final String startTime;
    private final String url;
    private final String capturedAt;

    public Event(String id, String name, String city, String venue,
                 String date, String startTime, String url, String capturedAt) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.venue = venue;
        this.date = date;
        this.startTime = startTime;
        this.url = url;
        this.capturedAt = capturedAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getVenue() { return venue; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getUrl() { return url; }
    public String getCapturedAt() { return capturedAt; }
}
