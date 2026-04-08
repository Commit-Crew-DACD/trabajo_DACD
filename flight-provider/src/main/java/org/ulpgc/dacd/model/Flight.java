package org.ulpgc.dacd.model;

public class Flight {
    private String flightNumber;
    private String origin;
    private String destination;
    private String destinationCity;
    private String date;
    private String scheduledTime;
    private String estimatedTime;
    private String status;
    private String airline;
    private String terminal;
    private String flightType;

    public Flight(String flightNumber, String origin, String destination,
                  String destinationCity, String date, String scheduledTime,
                  String estimatedTime, String status, String airline,
                  String terminal, String flightType) {
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
}