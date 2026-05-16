package org.ulpgc.dacd.control;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.ulpgc.dacd.model.Event;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TicketmasterController {
    private final EventProvider eventProvider;
    private final JmsPublisher publisher;
    private final String city;
    private final Gson gson = new Gson();

    public TicketmasterController(EventProvider eventProvider, JmsPublisher publisher, String city) {
        this.eventProvider = eventProvider;
        this.publisher = publisher;
        this.city = city;
    }

    public void execute(int intervalHours) {
        try {
            publisher.connect();
        } catch (Exception e) {
            System.err.println("Error conexión JMS: " + e.getMessage());
            return;
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Event> events = eventProvider.fetchEvents(city);
                for (Event event : events) {
                    publisher.publish(toJson(event));
                }
                System.out.println("Eventos enviados al broker: " + events.size());
            } catch (Exception e) {
                System.err.println("Error en ciclo de captura: " + e.getMessage());
            }
        }, 0, intervalHours, TimeUnit.HOURS);
    }

    private String toJson(Event event) {
        JsonObject obj = gson.toJsonTree(event).getAsJsonObject();
        obj.addProperty("ts", Instant.now().toString());
        obj.addProperty("ss", "ticketmaster-provider");
        return obj.toString();
    }
}