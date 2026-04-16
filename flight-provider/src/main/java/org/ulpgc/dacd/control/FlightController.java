package org.ulpgc.dacd.control;

import org.ulpgc.dacd.control.storage.FlightDatabaseManager;
import org.ulpgc.dacd.model.Flight;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlightController {
    private final FlightProvider flightProvider;
    private final FlightDatabaseManager databaseManager;

    public FlightController(FlightProvider flightProvider, FlightDatabaseManager databaseManager) {
        this.flightProvider = flightProvider;
        this.databaseManager = databaseManager;
    }

    public void execute() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Flight> flights = flightProvider.getFlights();
                databaseManager.saveFlights(flights);
                System.out.println("Se han guardado/proyectado " + flights.size() + " vuelos.");
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());            }
        }, 0, 1, TimeUnit.HOURS);
    }
}