package org.ulpgc.dacd;

import org.ulpgc.dacd.control.EventProvider;
import org.ulpgc.dacd.control.JmsPublisher;
import org.ulpgc.dacd.control.TicketmasterController;
import org.ulpgc.dacd.control.TicketmasterService;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: <Ciudad> <Intervalo_Horas>");
            return;
        }

        String city = args[0];
        int interval = Integer.parseInt(args[1]);
        String brokerUrl = "tcp://localhost:61616";
        String topic = "events";

        EventProvider provider = new TicketmasterService();
        JmsPublisher publisher = new JmsPublisher(brokerUrl, topic);

        TicketmasterController controller = new TicketmasterController(provider, publisher, city);
        controller.execute(interval);
    }
}