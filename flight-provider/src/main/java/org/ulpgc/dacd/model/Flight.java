package org.ulpgc.dacd.model;

public record Flight(
        String flightNumber,
        String origin,
        String destination,
        String destinationCity,
        String date,
        String scheduledTime,
        String estimatedTime,
        String status,
        String airline,
        String terminal,
        String flightType
) {}