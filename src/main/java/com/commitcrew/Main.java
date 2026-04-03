package com.commitcrew;

import com.commitcrew.api.TicketmasterService;
import com.commitcrew.model.Event;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            TicketmasterService service = new TicketmasterService();

            // Recolectamos datos de Madrid
            List<Event> madridEvents = service.getEvents("Madrid");
            System.out.println("--- EVENTOS EN MADRID (" + madridEvents.size() + ") ---");
            madridEvents.forEach(e -> System.out.println("- " + e.getName() + " | Recinto: " + e.getVenue()));

            // Recolectamos datos de Barcelona
            List<Event> bcnEvents = service.getEvents("Barcelona");
            System.out.println("\n--- EVENTOS EN BARCELONA (" + bcnEvents.size() + ") ---");
            bcnEvents.forEach(e -> System.out.println("- " + e.getName() + " | Recinto: " + e.getVenue()));

        } catch (Exception e) {
            System.err.println("Error en la recolección: " + e.getMessage());
        }
    }
}