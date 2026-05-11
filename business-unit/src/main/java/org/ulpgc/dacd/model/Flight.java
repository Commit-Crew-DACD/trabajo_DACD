package org.ulpgc.dacd.model;

public class Flight {
    private final String flightNumber;
    private final String origin;
    private final String destination;
    private final String destinationCity;
    private final String date;
    private final String scheduledTime;
    private final String estimatedTime;
    private final String status;
    private final String airline;
    private final String terminal;
    private final String flightType;
    private final String capturedAt;

    public Flight(String flightNumber, String origin, String destination,
                  String destinationCity, String date, String scheduledTime,
                  String estimatedTime, String status, String airline,
                  String terminal, String flightType, String capturedAt) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.destinationCity = destinationCity;
        this.date = date;
        this.scheduledTime = scheduledTime;
        this.estimatedTime = estimatedTime;
        this.status = status;
        this.airline = airline;
        this.terminal = terminal;
        this.flightType = flightType;
        this.capturedAt = capturedAt;
    }

    public String getFlightNumber() { return flightNumber; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getDestinationCity() { return destinationCity; }
    public String getDate() { return date; }
    public String getScheduledTime() { return scheduledTime; }
    public String getEstimatedTime() { return estimatedTime; }
    public String getStatus() { return status; }
    public String getAirline() { return airline; }
    public String getTerminal() { return terminal; }
    public String getFlightType() { return flightType; }
    public String getCapturedAt() { return capturedAt; }
}
