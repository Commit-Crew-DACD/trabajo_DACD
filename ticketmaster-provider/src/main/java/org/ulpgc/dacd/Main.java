package org.ulpgc.dacd;

import org.ulpgc.dacd.control.EventProvider;
import org.ulpgc.dacd.control.TicketmasterService;
import org.ulpgc.dacd.control.storage.EventDatabaseManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Error: Faltan argumentos. Uso: <Ciudad> <Intervalo_Horas>");
            return;
        }

        String city = args[0];
        int interval = Integer.parseInt(args[1]);

        EventProvider provider = new TicketmasterService();
        EventDatabaseManager db = new EventDatabaseManager();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        System.out.println("Iniciando Módulo de Eventos para: " + city);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                db.saveEvents(provider.fetchEvents(city));
                System.out.println("Eventos de " + city + " persistidos incrementalmente en la DB.");
            } catch (Exception e) {
                System.err.println("Error en la captura: " + e.getMessage());
            }
        }, 0, interval, TimeUnit.HOURS);
    }
}