package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.Flight;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.RequestBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlightService implements FlightProvider {
    private static final String DEPARTURE_FLIGHT_TYPE = "S";
    private static final String ARRIVAL_FLIGHT_TYPE = "L";

    private final String origin;
    private final List<String> destinations;
    private final OkHttpClient client;

    public FlightService(String origin, List<String> destinations) {
        this.origin = origin;
        this.destinations = destinations;
        this.client = new OkHttpClient();
    }

    @Override
    public List<Flight> getFlights() throws IOException {
        Map<String, Flight> flightsByRouteKey = new LinkedHashMap<>();

        for (String airport : monitoredAirports()) {
            addFlights(flightsByRouteKey, fetchFlights(airport, DEPARTURE_FLIGHT_TYPE));
            addFlights(flightsByRouteKey, fetchFlights(airport, ARRIVAL_FLIGHT_TYPE));
        }

        return new ArrayList<>(flightsByRouteKey.values());
    }

    private List<String> monitoredAirports() {
        List<String> airports = new ArrayList<>();
        airports.add(origin);

        for (String destination : destinations) {
            if (airports.stream().noneMatch(destination::equalsIgnoreCase)) {
                airports.add(destination);
            }
        }

        return airports;
    }

    private void addFlights(Map<String, Flight> flightsByRouteKey, List<Flight> flights) {
        for (Flight flight : flights) {
            flightsByRouteKey.putIfAbsent(routeKey(flight), flight);
        }
    }

    private String routeKey(Flight flight) {
        return String.join("|",
                flight.getFlightNumber(),
                flight.getOrigin(),
                flight.getDestination(),
                flight.getDate(),
                flight.getScheduledTime(),
                flight.getEstimatedTime(),
                flight.getFlightType()
        ).toUpperCase();
    }

    private List<Flight> fetchFlights(String airport, String flightType) throws IOException {
        List<Flight> flights = new ArrayList<>();
        String url = "https://www.aena.es/sites/Satellite?pagename=AENA_ConsultarVuelos"
                + "&airport=" + airport
                + "&flightType=" + flightType;

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(new byte[0]))
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .addHeader("Referer", "https://www.aena.es/es/infovuelos.html")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return flights;
            }

            JsonArray array = JsonParser.parseString(response.body().string()).getAsJsonArray();

            for (int i = 0; i < array.size(); i++) {
                JsonObject f = array.get(i).getAsJsonObject();
                Flight flight = toFlight(f, flightType);

                if (!isMonitoredFlight(flight)) {
                    continue;
                }

                flights.add(flight);
            }
        }

        return flights;
    }

    private Flight toFlight(JsonObject object, String flightType) {
        if (DEPARTURE_FLIGHT_TYPE.equals(flightType)) {
            return new Flight(
                    getString(object, "numVuelo"),
                    getString(object, "iataAena"),
                    getString(object, "iataOtro"),
                    getString(object, "ciudadIataOtro"),
                    getString(object, "fecha"),
                    getString(object, "horaProgramada"),
                    getString(object, "horaEstimada"),
                    getString(object, "estado"),
                    getString(object, "nombreCompania"),
                    getString(object, "terminal"),
                    flightType
            );
        }

        return new Flight(
                getString(object, "numVuelo"),
                getString(object, "iataOtro"),
                getString(object, "iataAena"),
                getString(object, "ciudadIataAena"),
                getString(object, "fecha"),
                getString(object, "horaProgramada"),
                getString(object, "horaEstimada"),
                getString(object, "estado"),
                getString(object, "nombreCompania"),
                getString(object, "terminal"),
                flightType
        );
    }

    private boolean isMonitoredFlight(Flight flight) {
        return isRouteBetweenOriginAndDestination(flight.getOrigin(), flight.getDestination())
                || isRouteBetweenOriginAndDestination(flight.getDestination(), flight.getOrigin());
    }

    private boolean isRouteBetweenOriginAndDestination(String routeOrigin, String routeDestination) {
        return origin.equalsIgnoreCase(routeOrigin)
                && destinations.stream().anyMatch(destination -> destination.equalsIgnoreCase(routeDestination));
    }

    private String getString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return "";
        }

        return object.get(key).getAsString();
    }
}
