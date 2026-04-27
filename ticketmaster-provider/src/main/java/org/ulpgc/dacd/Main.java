package org.ulpgc.dacd;

import org.ulpgc.dacd.control.EventProvider;
import org.ulpgc.dacd.control.JmsPublisher;
import org.ulpgc.dacd.control.TicketmasterService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Error: Faltan argumentos. Uso: <Ciudad> <Intervalo_Horas>");
            return;
        }

        String city = args[0];
        int interval = Integer.parseInt(args[1]);
        String brokerUrl = "tcp://localhost:61616";

        EventProvider provider = new TicketmasterService();
        JmsPublisher publisher = new JmsPublisher(brokerUrl, "Prediction");

        try {
            publisher.connect();
        } catch (Exception e) {
            System.err.println("Error conectando a ActiveMQ: " + e.getMessage());
            return;
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        System.out.println("Iniciando Módulo de Eventos para: " + city);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                provider.fetchEvents(city).forEach(event -> {
                    try {
                        publisher.publish(toJson(event));
                    } catch (Exception e) {
                        System.err.println("Error publicando evento: " + e.getMessage());
                    }
                });
                System.out.println("Eventos de " + city + " publicados en ActiveMQ.");
            } catch (Exception e) {
                System.err.println("Error en la captura: " + e.getMessage());
            }
        }, 0, interval, TimeUnit.HOURS);
    }

    private static String toJson(Object obj) {
        JsonObject json = gson.toJsonTree(obj).getAsJsonObject();
        json.addProperty("ts", Instant.now().toString());
        json.addProperty("ss", "ticketmaster-provider");
        return json.toString();
    }
}