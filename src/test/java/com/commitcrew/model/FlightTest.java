package com.commitcrew.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FlightTest {
    @Test
    public void testFlightCreation() {
        Flight flight = new Flight("Madrid", "Barcelona", "2026-05-20", 45.50);
        assertEquals(45.50, flight.getPrice());
        assertEquals("Madrid", flight.getOrigin());
    }
}