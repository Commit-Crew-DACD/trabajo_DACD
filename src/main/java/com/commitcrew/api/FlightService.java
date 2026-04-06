package com.commitcrew.api;

import com.commitcrew.model.Flight;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlightService {
    private final Call.Factory client;

    public FlightService() {
        this.client = new OkHttpClient();
    }

    public FlightService(Call.Factory client) {
        this.client = client;
    }

    public List<Flight> getFlights(String airport, String flightType) throws IOException {
        List<Flight> flights = new ArrayList<>();

        String url = "https://www.aena.es/sites/Satellite"
                + "?pagename=AENA_ConsultarVuelos"
                + "&airport=" + airport
                + "&flightType=" + flightType
                + "&limit=50"
                + "&dosDias=si";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Error AENA: " + response.code());
                return flights;
            }

            String body = response.body().string();
            JsonArray array = JsonParser.parseString(body).getAsJsonArray();

            for (int i = 0; i < array.size(); i++) {
                JsonObject f = array.get(i).getAsJsonObject();

                String numVuelo    = f.get("numVuelo").getAsString();
                String origen      = f.get("iataAena").getAsString();
                String destino     = f.get("iataOtro").getAsString();
                String ciudad      = f.get("ciudadIataOtro").getAsString();
                String fecha       = f.get("fecha").getAsString();
                String horaProg    = f.get("horaProgramada").getAsString();
                String horaEst     = f.get("horaEstimada").getAsString();
                String estado      = f.get("estado").getAsString();
                String compania    = f.get("nombreCompania").getAsString();
                String terminal    = f.get("terminal").getAsString();

                flights.add(new Flight(
                        numVuelo, origen, destino, ciudad,
                        fecha, horaProg, horaEst,
                        estado, compania, terminal, flightType
                ));
            }
        }
        return flights;
    }
}