package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.Flight;
import java.io.IOException;
import java.util.List;

public interface FlightProvider {
    List<Flight> getFlights() throws IOException;
}