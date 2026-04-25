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
        String url = "https://www.aena.es/sites/Satellite?pagename=AENA_ConsultarVuelos&airport=" + origin + "&flightType=S&limit=100&dosDias=si";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return flights;

            JsonArray array = JsonParser.parseString(response.body().string()).getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                JsonObject f = array.get(i).getAsJsonObject();
                String dest = f.get("iataOtro").getAsString();

                if (destinations.contains(dest)) {
                    flights.add(new Flight(
                            f.get("numVuelo").getAsString(),
                            f.get("iataAena").getAsString(),
                            dest,
                            f.get("ciudadIataOtro").getAsString(),
                            f.get("fecha").getAsString(),
                            f.get("horaProgramada").getAsString(),
                            f.get("horaEstimada").getAsString(),
                            f.get("estado").getAsString(),
                            f.get("nombreCompania").getAsString(),
                            f.get("terminal").getAsString(),
                            "S"
                    ));
                }
            }
        }
        return flights;
    }
}