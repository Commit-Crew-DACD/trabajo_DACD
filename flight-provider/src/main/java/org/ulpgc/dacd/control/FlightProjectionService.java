package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.Flight;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FlightProjectionService implements FlightProvider {
    private final FlightProvider baseProvider;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FlightProjectionService(FlightProvider baseProvider) {
        this.baseProvider = baseProvider;
    }

    @Override
    public List<Flight> getFlights() throws IOException {
        List<Flight> realFlights = baseProvider.getFlights();
        List<Flight> allFlights = new ArrayList<>(realFlights);

        for (Flight f : realFlights) {
            LocalDate originalDate = LocalDate.parse(f.getDate(), dateFormatter);
            for (int i = 1; i <= 4; i++) {
                LocalDate projectedDate = originalDate.plusWeeks(i);
                allFlights.add(new Flight(
                        f.getFlightNumber(),
                        f.getOrigin(),
                        f.getDestination(),
                        f.getDestinationCity(),
                        projectedDate.format(dateFormatter),
                        f.getScheduledTime(),
                        f.getEstimatedTime(),
                        f.getStatus(),
                        f.getAirline(),
                        f.getTerminal(),
                        f.getFlightType()
                ));
            }
        }
        return allFlights;
    }
}