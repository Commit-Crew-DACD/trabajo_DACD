package org.ulpgc.dacd.control;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.ulpgc.dacd.model.Flight;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlightController {
    private final FlightProvider flightProvider;
    private final JmsPublisher publisher;
    private final Gson gson = new Gson();

    public FlightController(FlightProvider flightProvider, JmsPublisher publisher) {
        this.flightProvider = flightProvider;
        this.publisher = publisher;
    }

    public void execute() {
        try {
            publisher.connect();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Flight> flights = flightProvider.getFlights();
                for (Flight flight : flights) {
                    publisher.publish(toJson(flight));
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.HOURS);
    }

    private String toJson(Flight flight) {
        JsonObject obj = gson.toJsonTree(flight).getAsJsonObject();
        obj.addProperty("ts", Instant.now().toString());
        obj.addProperty("ss", "flight-provider");
        return obj.toString();
    }
}