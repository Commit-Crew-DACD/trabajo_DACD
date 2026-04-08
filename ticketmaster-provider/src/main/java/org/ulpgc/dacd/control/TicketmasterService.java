package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.Event;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TicketmasterService {
    private final OkHttpClient client;
    private final String apiKey;

    public TicketmasterService() {
        this.client = new OkHttpClient();
        // Recuerda configurar esta variable de entorno en IntelliJ
        this.apiKey = System.getenv("TICKETMASTER_KEY");
    }

    public List<Event> getEvents(String city) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API Key no encontrada. Configura TICKETMASTER_KEY.");
        }

        String url = "https://app.ticketmaster.com/discovery/v2/events.json?apikey=" + apiKey + "&city=" + city;
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return new ArrayList<>();
            return parseJson(response.body().string());
        }
    }

    private List<Event> parseJson(String json) {
        List<Event> events = new ArrayList<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        if (!root.has("_embedded")) return events;

        JsonArray eventsArray = root.getAsJsonObject("_embedded").getAsJsonArray("events");
        for (int i = 0; i < eventsArray.size(); i++) {
            JsonObject e = eventsArray.get(i).getAsJsonObject();

            String venue = "N/A";
            String city = "N/A";
            if (e.has("_embedded") && e.getAsJsonObject("_embedded").has("venues")) {
                JsonObject v = e.getAsJsonObject("_embedded").getAsJsonArray("venues").get(0).getAsJsonObject();
                venue = v.get("name").getAsString();
                city = v.getAsJsonObject("city").get("name").getAsString();
            }

            events.add(new Event(
                    e.get("id").getAsString(),
                    e.get("name").getAsString(),
                    city,
                    venue,
                    e.getAsJsonObject("dates").getAsJsonObject("start").get("localDate").getAsString(),
                    e.get("url").getAsString()
            ));
        }
        return events;
    }
}