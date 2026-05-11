package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.Event;
import org.ulpgc.dacd.model.Flight;
import org.ulpgc.dacd.model.Recommendation;
import org.ulpgc.dacd.model.RecommendationConfig;
import org.ulpgc.dacd.storage.DatamartRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RecommendationService {
    private static final DateTimeFormatter EVENT_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FLIGHT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    private final DatamartRepository repository;

    public RecommendationService(DatamartRepository repository) {
        this.repository = repository;
    }

    public void rebuildRecommendations() {
        RecommendationConfig config = repository.getConfig();
        List<Event> events = repository.findAllEvents();
        List<Flight> flights = repository.findAllFlights();

        System.out.println("Rebuilding recommendations...");
        System.out.println("Events: " + events.size());
        System.out.println("Flights: " + flights.size());

        repository.clearRecommendations();

        for (Event event : events) {
            System.out.println("Processing event: " + event.getName() + " - " + event.getDate());

            if (!isValidEvent(event)) {
                System.out.println("Skipping event without valid start time.");
                continue;
            }

            LocalDate eventDate = LocalDate.parse(event.getDate(), EVENT_DATE_FORMATTER);
            LocalTime eventStartTime = LocalTime.parse(event.getStartTime(), TIME_FORMATTER);
            LocalDateTime eventStart = LocalDateTime.of(eventDate, eventStartTime);
            LocalDateTime eventEnd = eventStart.plusHours(config.getDefaultEventDurationHours());

            List<Flight> outboundFlights = flights.stream()
                    .filter(flight -> isOutboundFlight(flight, event, config))
                    .filter(flight -> arrivesBeforeEventWithMargin(flight, eventStart, config))
                    .toList();

            List<Flight> returnFlights = flights.stream()
                    .filter(flight -> isReturnFlight(flight, event, config))
                    .filter(flight -> departsAfterEventWithMargin(flight, eventEnd, config))
                    .toList();

            int saved = 0;

            for (Flight outboundFlight : outboundFlights) {
                for (Flight returnFlight : returnFlights) {
                    repository.saveRecommendation(toRecommendation(
                            event,
                            eventEnd,
                            outboundFlight,
                            returnFlight
                    ));

                    saved++;

                    if (saved >= 10) {
                        break;
                    }
                }

                if (saved >= 10) {
                    break;
                }
            }

            System.out.println("Recommendations saved for event: " + saved);
        }
    }

    private boolean isValidEvent(Event event) {
        return event.getDate() != null
                && !event.getDate().isBlank()
                && event.getStartTime() != null
                && !event.getStartTime().isBlank()
                && !"N/A".equalsIgnoreCase(event.getStartTime());
    }

    private boolean isOutboundFlight(Flight flight, Event event, RecommendationConfig config) {
        return config.getOriginAirport().equalsIgnoreCase(flight.getOrigin())
                && matchesEventCity(flight.getDestinationCity(), event.getCity());
    }

    private boolean isReturnFlight(Flight flight, Event event, RecommendationConfig config) {
        return matchesEventCity(flight.getOrigin(), event.getCity())
                || matchesEventCity(flight.getDestinationCity(), config.getOriginAirport());
    }

    private boolean arrivesBeforeEventWithMargin(Flight flight, LocalDateTime eventStart,
                                                 RecommendationConfig config) {
        LocalDateTime arrival = parseFlightDateTime(flight);
        return !arrival.isAfter(eventStart.minusHours(config.getOutboundMarginHours()));
    }

    private boolean departsAfterEventWithMargin(Flight flight, LocalDateTime eventEnd,
                                                RecommendationConfig config) {
        LocalDateTime departure = parseFlightDateTime(flight);
        return !departure.isBefore(eventEnd.plusHours(config.getReturnMarginHours()));
    }

    private LocalDateTime parseFlightDateTime(Flight flight) {
        LocalDate date = LocalDate.parse(flight.getDate(), FLIGHT_DATE_FORMATTER);
        String time = flight.getEstimatedTime();

        if (time == null || time.isBlank()) {
            time = flight.getScheduledTime();
        }

        return LocalDateTime.of(date, LocalTime.parse(time, TIME_FORMATTER));
    }

    private boolean matchesEventCity(String flightValue, String eventCity) {
        if (flightValue == null || eventCity == null) {
            return false;
        }

        String normalizedFlightValue = normalize(flightValue);
        String normalizedEventCity = normalize(eventCity);

        return normalizedFlightValue.contains(normalizedEventCity)
                || normalizedEventCity.contains(normalizedFlightValue)
                || matchesKnownAirport(normalizedFlightValue, normalizedEventCity);
    }

    private boolean matchesKnownAirport(String normalizedFlightValue, String normalizedEventCity) {
        return (normalizedEventCity.contains("madrid") && normalizedFlightValue.contains("mad"))
                || (normalizedEventCity.contains("madrid") && normalizedFlightValue.contains("barajas"))
                || (normalizedEventCity.contains("barcelona") && normalizedFlightValue.contains("bcn"))
                || (normalizedEventCity.contains("barcelona") && normalizedFlightValue.contains("prat"));
    }

    private String normalize(String value) {
        return value.toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ü", "u")
                .replace("ñ", "n");
    }

    private Recommendation toRecommendation(Event event, LocalDateTime eventEnd,
                                            Flight outboundFlight, Flight returnFlight) {
        return new Recommendation(
                event.getId(),
                event.getName(),
                event.getCity(),
                event.getDate(),
                event.getStartTime(),
                eventEnd.toLocalTime().toString(),
                outboundFlight.getFlightNumber(),
                outboundFlight.getAirline(),
                outboundFlight.getOrigin(),
                outboundFlight.getDestination(),
                outboundFlight.getScheduledTime(),
                outboundFlight.getEstimatedTime(),
                returnFlight.getFlightNumber(),
                returnFlight.getAirline(),
                returnFlight.getOrigin(),
                returnFlight.getDestination(),
                returnFlight.getEstimatedTime(),
                Instant.now().toString()
        );
    }
}
