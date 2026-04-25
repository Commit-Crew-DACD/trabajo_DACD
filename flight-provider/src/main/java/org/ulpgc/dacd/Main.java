package org.ulpgc.dacd;

import org.ulpgc.dacd.control.FlightController;
import org.ulpgc.dacd.control.FlightProvider;
import org.ulpgc.dacd.control.FlightService;
import org.ulpgc.dacd.control.storage.FlightDatabaseManager;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) return;

        String origin = args[0];
        List<String> destinations = Arrays.asList(args).subList(1, args.length);

        FlightProvider provider = new FlightService(origin, destinations);
        FlightDatabaseManager storage = new FlightDatabaseManager("flights.db");

        FlightController controller = new FlightController(provider, storage);
        controller.execute();
    }
}