package org.ulpgc.dacd.storage;

import org.ulpgc.dacd.model.Event;
import org.ulpgc.dacd.model.Flight;
import org.ulpgc.dacd.model.Recommendation;
import org.ulpgc.dacd.model.RecommendationConfig;

import java.util.List;

public interface RecommendationDataStore {
    RecommendationConfig getConfig();

    List<Event> findAllEvents();

    List<Flight> findAllFlights();

    void replaceRecommendations(List<Recommendation> recommendations);
}
