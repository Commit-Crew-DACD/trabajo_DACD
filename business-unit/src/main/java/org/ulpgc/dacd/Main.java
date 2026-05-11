package org.ulpgc.dacd;

import org.ulpgc.dacd.control.EventMessageParser;
import org.ulpgc.dacd.control.EventStoreLoader;
import org.ulpgc.dacd.control.RecommendationService;
import org.ulpgc.dacd.model.RecommendationConfig;
import org.ulpgc.dacd.storage.DatamartRepository;
import org.ulpgc.dacd.control.RestApi;

public class Main {
    public static void main(String[] args) {
        DatamartRepository repository = new DatamartRepository();
        RecommendationConfig config = repository.getConfig();

        EventMessageParser parser = new EventMessageParser();
        EventStoreLoader eventStoreLoader = new EventStoreLoader("eventstore", parser, repository);
        repository.clearEvents();
        repository.clearFlights();
        repository.clearRecommendations();
        eventStoreLoader.load();

        RecommendationService recommendationService = new RecommendationService(repository);
        recommendationService.rebuildRecommendations();

        System.out.println("Business Unit datamart initialized.");
        System.out.println("Origin airport: " + config.getOriginAirport());
        System.out.println("Historical eventstore loaded.");
        System.out.println("Events loaded: " + repository.countEvents());
        System.out.println("Flights loaded: " + repository.countFlights());
        System.out.println("Recommendations generated: " + repository.countRecommendations());
        RestApi restApi = new RestApi(repository, recommendationService, 7070);
        restApi.start();
    }
}
