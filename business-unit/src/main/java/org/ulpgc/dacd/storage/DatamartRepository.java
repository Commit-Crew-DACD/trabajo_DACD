package org.ulpgc.dacd.storage;

import org.ulpgc.dacd.model.Event;
import org.ulpgc.dacd.model.Flight;
import org.ulpgc.dacd.model.Recommendation;
import org.ulpgc.dacd.model.RecommendationConfig;

import java.util.List;

public class DatamartRepository {
    private final EventRepository eventRepository;
    private final FlightRepository flightRepository;
    private final RecommendationRepository recommendationRepository;
    private final ConfigRepository configRepository;

    public DatamartRepository() {
        DatabaseManager databaseManager = new DatabaseManager();
        this.eventRepository = new EventRepository(databaseManager);
        this.flightRepository = new FlightRepository(databaseManager);
        this.recommendationRepository = new RecommendationRepository(databaseManager);
        this.configRepository = new ConfigRepository(databaseManager);
    }

    public void saveEvent(Event event) {
        eventRepository.save(event);
    }

    public void saveFlight(Flight flight) {
        flightRepository.save(flight);
    }

    public RecommendationConfig getConfig() {
        return configRepository.getConfig();
    }

    public List<Event> findAllEvents() {
        return eventRepository.findAll();
    }

    public List<Flight> findAllFlights() {
        return flightRepository.findAll();
    }

    public void clearEvents() {
        eventRepository.clear();
    }

    public void clearFlights() {
        flightRepository.clear();
    }

    public void clearRecommendations() {
        recommendationRepository.clear();
    }

    public void saveRecommendation(Recommendation recommendation) {
        recommendationRepository.save(recommendation);
    }

    public List<Recommendation> findAllRecommendations() {
        return recommendationRepository.findAll();
    }

    public int countEvents() {
        return eventRepository.count();
    }

    public int countFlights() {
        return flightRepository.count();
    }

    public int countRecommendations() {
        return recommendationRepository.count();
    }

    public void saveConfig(RecommendationConfig config) {
        configRepository.saveConfig(config);
    }
}
