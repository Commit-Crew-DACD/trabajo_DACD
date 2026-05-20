package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.Event;
import org.ulpgc.dacd.model.Flight;
import org.ulpgc.dacd.model.Recommendation;
import org.ulpgc.dacd.model.RecommendationConfig;
import org.ulpgc.dacd.storage.DatamartRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class RecommendationService {
    private static final DateTimeFormatter EVENT_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FLIGHT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DatamartRepository repository;

    public RecommendationService(DatamartRepository repository) {
        this.repository = repository;
    }

    public void rebuildRecommendations() {
        RecommendationConfig config = repository.getConfig();
        List<Event> events = repository.findAllEvents();
        List<Flight> flights = repository.findAllFlights();

        System.out.println("Rebuilding recommendations...");
        repository.clearRecommendations();

        java.util.Set<String> processedEvents = new java.util.HashSet<>();

        for (Event event : events) {
            if (!isValidEvent(event)) {
                continue;
            }

            String eventDayKey = event.getName() + "-" + event.getDate();

            if (processedEvents.contains(eventDayKey)) {
                continue;
            }

            LocalDate eventDate = LocalDate.parse(event.getDate(), EVENT_DATE_FORMATTER);
            LocalTime eventStartTime = LocalTime.parse(event.getStartTime(), TIME_FORMATTER);
            LocalDateTime eventStart = LocalDateTime.of(eventDate, eventStartTime);
            LocalDateTime eventEnd = eventStart.plusHours(config.getDefaultEventDurationHours());

            List<Flight> outboundFlights = flights.stream()
                    .filter(flight -> isOutboundFlight(flight, event, config))
                    .filter(flight -> arrivesBeforeEventWithMargin(flight, eventStart, config))
                    .sorted(Comparator.comparing(this::estimateArrivalDateTime).reversed())
                    .toList();

            List<Flight> returnFlights = flights.stream()
                    .filter(flight -> isReturnFlight(flight, event, config))
                    .filter(flight -> departsAfterEventWithMargin(flight, eventEnd, config))
                    .sorted(Comparator.comparing(this::parseFlightDepartureDateTime))
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

            if (saved > 0) {
                processedEvents.add(eventDayKey);
            }

            System.out.println("Recommendations saved for event '" + event.getName() + "': " + saved);
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
                && (
                matchesEventCity(flight.getDestinationCity(), event.getCity())
                        || matchesEventCity(flight.getDestination(), event.getCity())
        );
    }

    private boolean isReturnFlight(Flight flight, Event event, RecommendationConfig config) {
        boolean isRealReturn = config.getOriginAirport().equalsIgnoreCase(flight.getDestination())
                && (matchesEventCity(flight.getOrigin(), event.getCity())
                || matchesEventCity(flight.getDestinationCity(), event.getCity()));

        boolean isSimulatedReturn = config.getOriginAirport().equalsIgnoreCase(flight.getOrigin())
                && (matchesEventCity(flight.getDestination(), event.getCity())
                || matchesEventCity(flight.getDestinationCity(), event.getCity()));

        return isRealReturn || isSimulatedReturn;
    }

    private boolean arrivesBeforeEventWithMargin(Flight flight, LocalDateTime eventStart, RecommendationConfig config) {
        LocalTime arrivalTime = estimateArrivalDateTime(flight).toLocalTime();
        LocalDateTime projectedArrival = LocalDateTime.of(eventStart.toLocalDate(), arrivalTime);
        if (projectedArrival.isAfter(eventStart.minusHours(2))) {
            projectedArrival = projectedArrival.minusDays(1);
        }

        return projectedArrival.isBefore(eventStart.minusHours(2));
    }

    private boolean departsAfterEventWithMargin(Flight flight, LocalDateTime eventEnd, RecommendationConfig config) {
        LocalTime departureTime = parseFlightDepartureDateTime(flight).toLocalTime();
        LocalDateTime projectedDeparture = LocalDateTime.of(eventEnd.toLocalDate().plusDays(1), departureTime);
        return projectedDeparture.isAfter(eventEnd.plusHours(2));
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

    private LocalDateTime parseFlightDepartureDateTime(Flight flight) {
        LocalDate date = LocalDate.parse(flight.getDate(), FLIGHT_DATE_FORMATTER);
        String time = flight.getEstimatedTime();
        if (time == null || time.isBlank()) {
            time = flight.getScheduledTime();
        }
        return LocalDateTime.of(date, LocalTime.parse(time, TIME_FORMATTER));
    }

    private LocalDateTime estimateArrivalDateTime(Flight flight) {
        return parseFlightDepartureDateTime(flight).plusMinutes(180);
    }

    private Recommendation toRecommendation(Event event, LocalDateTime eventEnd,
                                            Flight outboundFlight, Flight returnFlight) {
        LocalTime outboundDepTime = parseFlightDepartureDateTime(outboundFlight).toLocalTime();
        LocalTime outboundArrTime = outboundDepTime.plusMinutes(180);

        LocalDate eventDate = LocalDate.parse(event.getDate(), EVENT_DATE_FORMATTER);
        LocalDateTime eventStart = LocalDateTime.of(eventDate, LocalTime.parse(event.getStartTime(), TIME_FORMATTER));

        LocalDateTime outboundArrival = LocalDateTime.of(eventDate, outboundArrTime);
        LocalDateTime outboundDeparture = LocalDateTime.of(eventDate, outboundDepTime);

        if (outboundArrival.isAfter(eventStart.minusHours(2))) {
            outboundDeparture = outboundDeparture.minusDays(1);
            outboundArrival = outboundArrival.minusDays(1);
        }

        LocalTime returnDepTime = parseFlightDepartureDateTime(returnFlight).toLocalTime();
        LocalDateTime returnDeparture = LocalDateTime.of(eventDate.plusDays(1), returnDepTime);

        if (returnDeparture.isBefore(eventEnd.plusHours(2))) {
            returnDeparture = returnDeparture.plusDays(1);
        }

        String realReturnOrigin = returnFlight.getOrigin().equalsIgnoreCase(outboundFlight.getOrigin())
                ? returnFlight.getDestination()
                : returnFlight.getOrigin();

        String realReturnDestination = outboundFlight.getOrigin();

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
                outboundDeparture.format(DISPLAY_DATE_TIME_FORMATTER),
                outboundArrival.format(DISPLAY_DATE_TIME_FORMATTER),
                returnFlight.getFlightNumber() + "R",
                returnFlight.getAirline(),
                realReturnOrigin,
                realReturnDestination,
                returnDeparture.format(DISPLAY_DATE_TIME_FORMATTER),
                Instant.now().toString()
        );
    }
}