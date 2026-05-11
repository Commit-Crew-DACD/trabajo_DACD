package org.ulpgc.dacd.model;

public class Recommendation {
    private final String eventId;
    private final String eventName;
    private final String eventCity;
    private final String eventDate;
    private final String eventStartTime;
    private final String eventEndTime;
    private final String outboundFlightNumber;
    private final String outboundAirline;
    private final String outboundOrigin;
    private final String outboundDestination;
    private final String outboundDepartureTime;
    private final String outboundArrivalTime;
    private final String returnFlightNumber;
    private final String returnAirline;
    private final String returnOrigin;
    private final String returnDestination;
    private final String returnDepartureTime;
    private final String capturedAt;

    public Recommendation(String eventId, String eventName, String eventCity,
                          String eventDate, String eventStartTime, String eventEndTime,
                          String outboundFlightNumber, String outboundAirline,
                          String outboundOrigin, String outboundDestination,
                          String outboundDepartureTime, String outboundArrivalTime,
                          String returnFlightNumber, String returnAirline,
                          String returnOrigin, String returnDestination,
                          String returnDepartureTime, String capturedAt) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventCity = eventCity;
        this.eventDate = eventDate;
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
        this.outboundFlightNumber = outboundFlightNumber;
        this.outboundAirline = outboundAirline;
        this.outboundOrigin = outboundOrigin;
        this.outboundDestination = outboundDestination;
        this.outboundDepartureTime = outboundDepartureTime;
        this.outboundArrivalTime = outboundArrivalTime;
        this.returnFlightNumber = returnFlightNumber;
        this.returnAirline = returnAirline;
        this.returnOrigin = returnOrigin;
        this.returnDestination = returnDestination;
        this.returnDepartureTime = returnDepartureTime;
        this.capturedAt = capturedAt;
    }

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getEventCity() { return eventCity; }
    public String getEventDate() { return eventDate; }
    public String getEventStartTime() { return eventStartTime; }
    public String getEventEndTime() { return eventEndTime; }
    public String getOutboundFlightNumber() { return outboundFlightNumber; }
    public String getOutboundAirline() { return outboundAirline; }
    public String getOutboundOrigin() { return outboundOrigin; }
    public String getOutboundDestination() { return outboundDestination; }
    public String getOutboundDepartureTime() { return outboundDepartureTime; }
    public String getOutboundArrivalTime() { return outboundArrivalTime; }
    public String getReturnFlightNumber() { return returnFlightNumber; }
    public String getReturnAirline() { return returnAirline; }
    public String getReturnOrigin() { return returnOrigin; }
    public String getReturnDestination() { return returnDestination; }
    public String getReturnDepartureTime() { return returnDepartureTime; }
    public String getCapturedAt() { return capturedAt; }
}
