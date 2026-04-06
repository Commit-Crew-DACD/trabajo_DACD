package com.commitcrew;

import com.commitcrew.api.TicketmasterService;
import com.commitcrew.api.FlightService;
import com.commitcrew.storage.DatabaseManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        TicketmasterService eventsApi = new TicketmasterService();
        FlightService flightsApi = new FlightService();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable task = () -> {
            try {
                System.out.println("Ejecutando captura de datos...");
                db.saveEvents(eventsApi.getEvents("Madrid"));
                db.saveEvents(eventsApi.getEvents("Barcelona"));
                db.saveFlights(flightsApi.getFlights("LPA", "S"));
                db.saveFlights(flightsApi.getFlights("LPA", "L"));
                System.out.println("Captura completada con éxito.");
            } catch (Exception e) {
                System.err.println("Error en la captura programada: " + e.getMessage());
            }
        };

        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.HOURS);
    }
}