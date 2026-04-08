package org.ulpgc.dacd;

import org.ulpgc.dacd.control.FlightService;
import org.ulpgc.dacd.control.storage.FlightDatabaseManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        FlightService service = new FlightService();
        FlightDatabaseManager db = new FlightDatabaseManager();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        System.out.println("Iniciando Módulo de Vuelos (LPA -> MAD/BCN)...");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                db.saveFlights(service.getFlights("LPA", "S"));
                System.out.println("Datos de vuelos actualizados en SQLite.");
            } catch (Exception e) {
                System.err.println("Error en la ejecución programada: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.HOURS); // Se ejecuta cada hora
    }
}