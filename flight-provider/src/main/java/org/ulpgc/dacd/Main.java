package org.ulpgc.dacd;

import org.ulpgc.dacd.control.FlightController;
import org.ulpgc.dacd.control.FlightProvider;
import org.ulpgc.dacd.control.FlightService;
import org.ulpgc.dacd.control.JmsPublisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: <Origen> <Destino1> [Destino2...]");
            return;
        }

        String origin = args[0];
        List<String> destinations = new ArrayList<>(Arrays.asList(args).subList(1, args.length));

        String brokerUrl = "failover:(tcp://localhost:61616)?maxReconnectAttempts=10&initialReconnectDelay=1000&maxReconnectDelay=30000";

        FlightProvider provider = new FlightService(origin, destinations);
        JmsPublisher publisher = new JmsPublisher(brokerUrl, "Flight");

        FlightController controller = new FlightController(provider, publisher);
        controller.execute();
    }
}
