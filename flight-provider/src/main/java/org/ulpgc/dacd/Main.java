package org.ulpgc.dacd;

import org.ulpgc.dacd.control.*;
import org.ulpgc.dacd.control.storage.FlightDatabaseManager;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Error: Faltan argumentos (Origen Destinos...)");
            return;
        }

        String origin = args[0];
        List<String> destinations = new ArrayList<>(Arrays.asList(args).subList(1, args.length));

        System.out.println("Iniciando captura de vuelos desde: " + origin);

        FlightProvider scraper = new FlightService(origin, destinations);
        FlightProvider projector = new FlightProjectionService(scraper);
        FlightDatabaseManager storage = new FlightDatabaseManager();

        FlightController controller = new FlightController(projector, storage);
        controller.execute();
    }
}