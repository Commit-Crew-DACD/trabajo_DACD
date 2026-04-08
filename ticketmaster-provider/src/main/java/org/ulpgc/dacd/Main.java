package org.ulpgc.dacd;

import org.ulpgc.dacd.control.TicketmasterService;
import org.ulpgc.dacd.control.storage.EventDatabaseManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        TicketmasterService service = new TicketmasterService();
        EventDatabaseManager db = new EventDatabaseManager();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        System.out.println("Iniciando Módulo de Eventos (Ticketmaster)...");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                db.saveEvents(service.getEvents("Las Palmas"));
                System.out.println("Eventos actualizados en SQLite (events.db)");
            } catch (Exception e) {
                System.err.println("Error en Ticketmaster: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.HOURS);
    }
}