package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.Event;
import org.ulpgc.dacd.model.Flight;
import org.ulpgc.dacd.model.Recommendation;
import org.ulpgc.dacd.model.RecommendationConfig;
import org.ulpgc.dacd.storage.RecommendationDataStore;

import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

public class RecommendationService {
    // 1. EL LOGGER DEBE ESTAR AQUÍ COMO ATRIBUTO DE LA CLASE
    private static final Logger LOGGER = Logger.getLogger(RecommendationService.class.getName());

    private static final String ARRIVAL_FLIGHT_TYPE = "L";
    private static final DateTimeFormatter EVENT_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FLIGHT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int MAX_RECOMMENDATIONS_PER_EVENT = 10;
    private static final int MAX_REAL_FLIGHT_LOOKAHEAD_DAYS = 14;
    private static final int MAX_OUTBOUND_MARGIN_HOURS = 36;
    private static final int MAX_RETURN_MARGIN_HOURS = 72;
    private static final int DEFAULT_LOCAL_CLOCK_DURATION_MINUTES = 180;
    private static final int LPA_TO_MAD_LOCAL_CLOCK_DURATION_MINUTES = 225;
    private static final int MAD_TO_LPA_LOCAL_CLOCK_DURATION_MINUTES = 165;
    private static final int LPA_TO_BCN_LOCAL_CLOCK_DURATION_MINUTES = 260;
    private static final int BCN_TO_LPA_LOCAL_CLOCK_DURATION_MINUTES = 155;

    private final RecommendationDataStore repository;

    public RecommendationService(RecommendationDataStore repository) {
        this.repository = repository;
    }

    public synchronized void rebuildRecommendations() {
        RecommendationConfig config = repository.getConfig();
        List<Event> events = canonicalEvents(repository.findAllEvents());
        List<Flight> flights = repository.findAllFlights().stream()
                .filter(this::isValidFlight)
                .toList();
        List<Recommendation> recommendations = new ArrayList<>();
        String capturedAt = Instant.now().toString();

        // Cambiados los System.out.println por LOGGER.info
        LOGGER.info("Rebuilding recommendations...");
        LOGGER.info("Canonical events: " + events.size());
        LOGGER.info("Flights: " + flights.size());

        for (Event event : events) {
            LocalDate eventDate = LocalDate.parse(event.getDate(), EVENT_DATE_FORMATTER);
            LocalTime eventStartTime = LocalTime.parse(event.getStartTime(), TIME_FORMATTER);
            LocalDateTime eventStart = LocalDateTime.of(eventDate, eventStartTime);
            LocalDateTime eventEnd = eventStart.plusHours(config.getDefaultEventDurationHours());

            List<Flight> outboundFlights = flights.stream()
                    .filter(flight -> isOutboundFlight(flight, event, config))
                    .filter(flight -> arrivesBeforeEventWithMargin(flight, eventStart, config))
                    .sorted(outboundFlightComparator())
                    .toList();

            List<Flight> returnFlights = flights.stream()
                    .filter(flight -> isReturnFlight(flight, event, config))
                    .filter(flight -> departsAfterEventWithMargin(flight, eventEnd, config))
                    .sorted(returnFlightComparator())
                    .toList();

            int saved = addRecommendationsForEvent(recommendations, event, eventEnd,
                    outboundFlights, returnFlights, capturedAt);

            LOGGER.info("Recommendations saved for event " + event.getName() + ": " + saved);
        }

        repository.replaceRecommendations(recommendations);
        LOGGER.info("Recommendations generated: " + recommendations.size());
    }

    private int addRecommendationsForEvent(List<Recommendation> recommendations, Event event,
                                           LocalDateTime eventEnd, List<Flight> outboundFlights,
                                           List<Flight> returnFlights, String capturedAt) {
        int saved = 0;

        for (Flight outboundFlight : outboundFlights) {
            for (Flight returnFlight : returnFlights) {
                recommendations.add(toRecommendation(event, eventEnd, outboundFlight, returnFlight, capturedAt));
                saved++;

                if (saved >= MAX_RECOMMENDATIONS_PER_EVENT) {
                    return saved;
                }
            }
        }

        return saved;
    }

    private List<Event> canonicalEvents(List<Event> events) {
        Map<EventGroupKey, List<Event>> groupedEvents = new LinkedHashMap<>();

        events.stream()
                .filter(this::isValidEvent)
                .filter(event -> !isParkingEvent(event))
                .sorted(eventComparator())
                .forEach(event -> groupedEvents
                        .computeIfAbsent(EventGroupKey.from(event, this::normalize), ignored -> new ArrayList<>())
                        .add(event));

        return groupedEvents.values().stream()
                .map(this::chooseRepresentativeEvent)
                .sorted(eventComparator())
                .toList();
    }

    private Event chooseRepresentativeEvent(List<Event> events) {
        return events.stream()
                .min(Comparator.comparingInt(this::representativeScore).reversed()
                        .thenComparing(Event::getStartTime, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(Event::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .orElseThrow();
    }

    private boolean isValidEvent(Event event) {
        try {
            if (event.getDate() == null
                    || event.getDate().isBlank()
                    || event.getStartTime() == null
                    || event.getStartTime().isBlank()
                    || "N/A".equalsIgnoreCase(event.getStartTime())) {
                return false;
            }

            LocalDate.parse(event.getDate(), EVENT_DATE_FORMATTER);
            LocalTime.parse(event.getStartTime(), TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            LOGGER.warning("Skipping event with invalid date/time: " + event.getName());
            return false;
        }
    }

    private boolean isValidFlight(Flight flight) {
        try {
            if (flight.getDate() == null
                    || flight.getDate().isBlank()
                    || flightTime(flight).isBlank()) {
                return false;
            }

            LocalDate.parse(flight.getDate(), FLIGHT_DATE_FORMATTER);
            LocalTime.parse(flightTime(flight), TIME_FORMATTER);
            return isRealCapturedFlight(flight);
        } catch (DateTimeParseException e) {
            LOGGER.warning("Skipping flight with invalid date/time: " + flight.getFlightNumber());
            return false;
        }
    }

    private boolean isRealCapturedFlight(Flight flight) {
        try {
            if (flight.getCapturedAt() == null || flight.getCapturedAt().isBlank()) {
                LOGGER.warning("Skipping flight without capture timestamp: " + flight.getFlightNumber());
                return false;
            }

            LocalDate flightDate = LocalDate.parse(flight.getDate(), FLIGHT_DATE_FORMATTER);
            LocalDate captureDate = Instant.parse(flight.getCapturedAt()).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate lastRealFlightDate = captureDate.plusDays(MAX_REAL_FLIGHT_LOOKAHEAD_DAYS);

            boolean insideRealCaptureWindow = !flightDate.isBefore(captureDate)
                    && !flightDate.isAfter(lastRealFlightDate);

            if (!insideRealCaptureWindow) {
                LOGGER.warning("Skipping flight outside real capture window: "
                        + flight.getFlightNumber()
                        + " on " + flight.getDate()
                        + " captured at " + flight.getCapturedAt());
            }

            return insideRealCaptureWindow;
        } catch (DateTimeParseException e) {
            LOGGER.warning("Skipping flight with invalid capture timestamp: " + flight.getFlightNumber());
            return false;
        }
    }

    private boolean isParkingEvent(Event event) {
        return normalize(event.getName()).contains("parking");
    }

    private int representativeScore(Event event) {
        int score = 0;
        String name = normalize(event.getName());

        if (!name.contains("vip packages")) {
            score += 4;
        }
        if (!name.contains("parking")) {
            score += 4;
        }
        if (name.equals(normalize(EventGroupKey.extractArtistName(event.getName())))) {
            score += 4;
        }
        if (event.getVenue() != null && !event.getVenue().isBlank()) {
            score += 2;
        }
        if (event.getUrl() != null && !event.getUrl().isBlank()) {
            score += 1;
        }

        return score;
    }

    private boolean isOutboundFlight(Flight flight, Event event, RecommendationConfig config) {
        return config.getOriginAirport().equalsIgnoreCase(flight.getOrigin())
                && (matchesEventCity(flight.getDestinationCity(), event.getCity())
                || matchesEventCity(flight.getDestination(), event.getCity()));
    }

    private boolean isReturnFlight(Flight flight, Event event, RecommendationConfig config) {
        return config.getOriginAirport().equalsIgnoreCase(flight.getDestination())
                && matchesEventCity(flight.getOrigin(), event.getCity());
    }

    private boolean arrivesBeforeEventWithMargin(Flight flight, LocalDateTime eventStart,
                                                 RecommendationConfig config) {
        LocalDateTime arrival = estimateArrivalDateTime(flight);
        LocalDateTime latestAllowedArrival = eventStart.minusHours(config.getOutboundMarginHours());
        LocalDateTime earliestAllowedArrival = eventStart.minusHours(MAX_OUTBOUND_MARGIN_HOURS);

        return !arrival.isAfter(latestAllowedArrival)
                && !arrival.isBefore(earliestAllowedArrival);
    }

    private boolean departsAfterEventWithMargin(Flight flight, LocalDateTime eventEnd,
                                                RecommendationConfig config) {
        LocalDateTime departure = estimateDepartureDateTime(flight);
        LocalDateTime earliestAllowedDeparture = eventEnd.plusHours(config.getReturnMarginHours());
        LocalDateTime latestAllowedDeparture = eventEnd.plusHours(MAX_RETURN_MARGIN_HOURS);

        return !departure.isBefore(earliestAllowedDeparture)
                && !departure.isAfter(latestAllowedDeparture);
    }

    private Comparator<Event> eventComparator() {
        return Comparator.comparing(Event::getDate, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Event::getStartTime, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Event::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Event::getId, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    }

    private Comparator<Flight> outboundFlightComparator() {
        return Comparator.comparing(this::estimateArrivalDateTime).reversed()
                .thenComparing(this::estimateDepartureDateTime)
                .thenComparing(Flight::getFlightNumber, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Flight::getAirline, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    }

    private Comparator<Flight> returnFlightComparator() {
        return Comparator.comparing(this::estimateDepartureDateTime)
                .thenComparing(Flight::getFlightNumber, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Flight::getAirline, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    }

    private LocalDateTime estimateDepartureDateTime(Flight flight) {
        LocalDateTime scheduledDateTime = parseFlightDateTime(flight);

        if (ARRIVAL_FLIGHT_TYPE.equalsIgnoreCase(flight.getFlightType())) {
            return scheduledDateTime.minusMinutes(estimateLocalArrivalOffsetMinutes(flight));
        }

        return scheduledDateTime;
    }

    private LocalDateTime parseFlightDateTime(Flight flight) {
        LocalDate date = LocalDate.parse(flight.getDate(), FLIGHT_DATE_FORMATTER);
        return LocalDateTime.of(date, LocalTime.parse(flightTime(flight), TIME_FORMATTER));
    }

    private String flightTime(Flight flight) {
        String time = flight.getEstimatedTime();

        if (time == null || time.isBlank()) {
            time = flight.getScheduledTime();
        }

        return String.valueOf(time);
    }

    private LocalDateTime estimateArrivalDateTime(Flight flight) {
        if (ARRIVAL_FLIGHT_TYPE.equalsIgnoreCase(flight.getFlightType())) {
            return parseFlightDateTime(flight);
        }

        return estimateDepartureDateTime(flight).plusMinutes(estimateLocalArrivalOffsetMinutes(flight));
    }

    private long estimateLocalArrivalOffsetMinutes(Flight flight) {
        String origin = flight.getOrigin();
        String destination = flight.getDestination();

        if (isRoute(origin, destination, "LPA", "MAD")) {
            return LPA_TO_MAD_LOCAL_CLOCK_DURATION_MINUTES;
        }

        if (isRoute(origin, destination, "MAD", "LPA")) {
            return MAD_TO_LPA_LOCAL_CLOCK_DURATION_MINUTES;
        }

        if (isRoute(origin, destination, "LPA", "BCN")) {
            return LPA_TO_BCN_LOCAL_CLOCK_DURATION_MINUTES;
        }

        if (isRoute(origin, destination, "BCN", "LPA")) {
            return BCN_TO_LPA_LOCAL_CLOCK_DURATION_MINUTES;
        }

        return DEFAULT_LOCAL_CLOCK_DURATION_MINUTES;
    }

    private boolean isRoute(String origin, String destination, String expectedOrigin, String expectedDestination) {
        return expectedOrigin.equalsIgnoreCase(origin) && expectedDestination.equalsIgnoreCase(destination);
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
        return Normalizer.normalize(String.valueOf(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private Recommendation toRecommendation(Event event, LocalDateTime eventEnd,
                                            Flight outboundFlight, Flight returnFlight, String capturedAt) {
        LocalDateTime outboundDeparture = estimateDepartureDateTime(outboundFlight);
        LocalDateTime outboundArrival = estimateArrivalDateTime(outboundFlight);
        LocalDateTime returnDeparture = estimateDepartureDateTime(returnFlight);

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
                returnFlight.getFlightNumber(),
                returnFlight.getAirline(),
                returnFlight.getOrigin(),
                returnFlight.getDestination(),
                returnDeparture.format(DISPLAY_DATE_TIME_FORMATTER),
                capturedAt
        );
    }

    @FunctionalInterface
    private interface TextNormalizer {
        String normalize(String value);
    }

    private record EventGroupKey(String date, String artistName, String venueOrCity) {
        private static EventGroupKey from(Event event, TextNormalizer normalizer) {
            return new EventGroupKey(
                    event.getDate(),
                    normalizer.normalize(extractArtistName(event.getName())),
                    normalizer.normalize(preferredLocation(event))
            );
        }

        private static String extractArtistName(String name) {
            String cleanedName = String.valueOf(name)
                    .replaceAll("(?i)\\|\\s*vip packages.*$", "")
                    .replaceAll("\\s+", " ")
                    .trim();
            List<String> separators = List.of(" - ", ": ", " | ");

            for (String separator : separators) {
                int index = cleanedName.indexOf(separator);

                if (index > 0) {
                    return cleanedName.substring(0, index).trim();
                }
            }

            return cleanedName;
        }

        private static String preferredLocation(Event event) {
            if (event.getVenue() != null && !event.getVenue().isBlank()) {
                return event.getVenue();
            }

            return event.getCity();
        }
    }
}