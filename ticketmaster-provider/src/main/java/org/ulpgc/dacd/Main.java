package org.ulpgc.dacd;

import org.ulpgc.dacd.control.EventProvider;
import org.ulpgc.dacd.control.JmsPublisher;
import org.ulpgc.dacd.control.TicketmasterController;
import org.ulpgc.dacd.control.TicketmasterService;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Error: Faltan argumentos. Uso: <Ciudad> <Intervalo_Horas>");
            return;
        }

        String city = args[0];
        int interval = Integer.parseInt(args[1]);
        String brokerUrl = "failover:(tcp://localhost:61616)?maxReconnectAttempts=10&initialReconnectDelay=1000&maxReconnectDelay=30000";

        EventProvider provider = new TicketmasterService();
        JmsPublisher publisher = new JmsPublisher(brokerUrl, "Ticketmaster");

        TicketmasterController controller = new TicketmasterController(provider, publisher, city);
        System.out.println("Iniciando Módulo de Eventos para: " + city);
        controller.execute(interval);
    }
}