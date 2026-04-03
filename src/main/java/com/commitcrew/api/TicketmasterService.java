package com.commitcrew.api;

import com.commitcrew.model.Event;
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

public class TicketmasterService {
    private final Call.Factory client;
    private final String apiKey;

    public TicketmasterService() {
        this.client = new OkHttpClient();
        this.apiKey = System.getenv("TICKETMASTER_KEY");
    }

    public TicketmasterService(Call.Factory client, String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
    }
    public List<Event> getEvents(String city) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API Key no encontrada en las variables de entorno");
        }

        String url = "https://app.ticketmaster.com/discovery/v2/events.json?apikey=" + apiKey + "&city=" + city;
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error: " + response);
            return parseEvents(response.body().string());
        }
    }

    private List<Event> parseEvents(String json) {
        List<Event> events = new ArrayList<>();
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        if (!jsonObject.has("_embedded")) return events;

        JsonArray eventsArray = jsonObject.getAsJsonObject("_embedded").getAsJsonArray("events");

        for (int i = 0; i < eventsArray.size(); i++) {
            JsonObject eventJson = eventsArray.get(i).getAsJsonObject();
            String id = eventJson.get("id").getAsString();
            String name = eventJson.get("name").getAsString();
            String url = eventJson.get("url").getAsString();
            String date = eventJson.getAsJsonObject("dates").getAsJsonObject("start").get("localDate").getAsString();

            String venue = "Unknown Venue";
            String city = "Unknown City";

            if (eventJson.has("_embedded")) {
                JsonObject embedded = eventJson.getAsJsonObject("_embedded");
                if (embedded.has("venues")) {
                    JsonArray venues = embedded.getAsJsonArray("venues");
                    if (venues.size() > 0) {
                        JsonObject vObj = venues.get(0).getAsJsonObject();
                        venue = vObj.get("name").getAsString();
                        if (vObj.has("city")) {
                            city = vObj.getAsJsonObject("city").get("name").getAsString();
                        }
                    }
                }
            }
            events.add(new Event(id, name, city, venue, date, url));
        }
        return events;
    }
}