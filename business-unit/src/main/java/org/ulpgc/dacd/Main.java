package org.ulpgc.dacd;

import org.ulpgc.dacd.control.EventMessageParser;
import org.ulpgc.dacd.control.EventStoreLoader;
import org.ulpgc.dacd.model.RecommendationConfig;
import org.ulpgc.dacd.storage.DatamartRepository;

public class Main {
    public static void main(String[] args) {
        DatamartRepository repository = new DatamartRepository();
        RecommendationConfig config = repository.getConfig();

        EventMessageParser parser = new EventMessageParser();
        EventStoreLoader eventStoreLoader = new EventStoreLoader("eventstore", parser, repository);
        eventStoreLoader.load();

        System.out.println("Business Unit datamart initialized.");
        System.out.println("Origin airport: " + config.getOriginAirport());
        System.out.println("Historical eventstore loaded.");
        System.out.println("Events loaded: " + repository.countEvents());
        System.out.println("Flights loaded: " + repository.countFlights());

    }
}
