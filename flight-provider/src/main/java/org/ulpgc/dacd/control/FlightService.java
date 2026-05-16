package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.Flight;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlightService implements FlightProvider {
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
        List<Flight> flights = new ArrayList<>();
        String url = "https://www.aena.es/sites/Satellite?pagename=AENA_ConsultarVuelos"
                + "&airport=" + origin
                + "&flightType=S"
                + "&limit=500"
                + "&dosDias=si";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return flights;
            }

            JsonArray array = JsonParser.parseString(response.body().string()).getAsJsonArray();

            for (int i = 0; i < array.size(); i++) {
                JsonObject f = array.get(i).getAsJsonObject();
                String destination = getString(f, "iataOtro");

                if (!destinations.contains(destination)) {
                    continue;
                }

                flights.add(new Flight(
                        getString(f, "numVuelo"),
                        getString(f, "iataAena"),
                        destination,
                        getString(f, "ciudadIataOtro"),
                        getString(f, "fecha"),
                        getString(f, "horaProgramada"),
                        getString(f, "horaEstimada"),
                        getString(f, "estado"),
                        getString(f, "nombreCompania"),
                        getString(f, "terminal"),
                        "S"
                ));
            }
        }

        return flights;
    }

    private String getString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return "";
        }

        return object.get(key).getAsString();
    }
}
