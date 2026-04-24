package org.ulpgc.dacd;

import org.ulpgc.dacd.control.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: <Origen> <Destino1> [Destino2...]");
            return;
        }

        String origin = args[0];
        List<String> destinations = new ArrayList<>(Arrays.asList(args).subList(1, args.length));
        String brokerUrl = "tcp://localhost:61616";

        FlightProvider scraper = new FlightService(origin, destinations);
        FlightProvider projector = new FlightProjectionService(scraper);
        JmsPublisher publisher = new JmsPublisher(brokerUrl, "Flight");

        FlightController controller = new FlightController(projector, publisher);
        controller.execute();
    }
}