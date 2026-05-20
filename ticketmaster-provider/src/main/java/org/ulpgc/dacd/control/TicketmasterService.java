package org.ulpgc.dacd.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ulpgc.dacd.model.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TicketmasterService implements EventProvider {
    private static final String DISCOVERY_EVENTS_URL = "https://app.ticketmaster.com/discovery/v2/events.json";
    private static final int PAGE_SIZE = 200;
    private static final int FIRST_PAGE = 0;

    private final OkHttpClient client;
    private final String apiKey;

    public TicketmasterService() {
        this.client = new OkHttpClient();
        this.apiKey = System.getenv("TICKETMASTER_KEY");
    }

    @Override
    public List<Event> fetchEvents(String city) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API Key no encontrada. Configura TICKETMASTER_KEY en Environment Variables.");
        }

        Map<String, Event> eventsById = new LinkedHashMap<>();
        int totalPages = 1;

        for (int page = FIRST_PAGE; page < totalPages; page++) {
            JsonObject response = fetchEventsPage(city, page);
            parseJson(response).forEach(event -> eventsById.putIfAbsent(event.getId(), event));
            totalPages = Math.max(totalPages, readTotalPages(response));
        }

        return new ArrayList<>(eventsById.values());
    }

    private JsonObject fetchEventsPage(String city, int page) throws IOException {
        HttpUrl url = HttpUrl.parse(DISCOVERY_EVENTS_URL).newBuilder()
                .addQueryParameter("apikey", apiKey)
                .addQueryParameter("city", city)
                .addQueryParameter("countryCode", "ES")
                .addQueryParameter("classificationName", "music")
                .addQueryParameter("sort", "date,asc")
                .addQueryParameter("size", String.valueOf(PAGE_SIZE))
                .addQueryParameter("page", String.valueOf(page))
                .build();

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return new JsonObject();
            }

            return JsonParser.parseString(response.body().string()).getAsJsonObject();
        }
    }

    private int readTotalPages(JsonObject root) {
        if (!root.has("page") || !root.get("page").isJsonObject()) {
            return 1;
        }

        JsonObject page = root.getAsJsonObject("page");

        if (!page.has("totalPages") || page.get("totalPages").isJsonNull()) {
            return 1;
        }

        return Math.max(1, page.get("totalPages").getAsInt());
    }

    private List<Event> parseJson(JsonObject root) {
        List<Event> events = new ArrayList<>();

        if (!root.has("_embedded")) {
            return events;
        }

        JsonArray eventsArray = root.getAsJsonObject("_embedded").getAsJsonArray("events");

        for (int i = 0; i < eventsArray.size(); i++) {
            JsonObject event = eventsArray.get(i).getAsJsonObject();
            String venue = "N/A";
            String city = "N/A";

            if (event.has("_embedded") && event.getAsJsonObject("_embedded").has("venues")) {
                JsonObject venueObject = event.getAsJsonObject("_embedded").getAsJsonArray("venues").get(0).getAsJsonObject();
                venue = getString(venueObject, "name", "N/A");

                if (venueObject.has("city") && venueObject.get("city").isJsonObject()) {
                    city = getString(venueObject.getAsJsonObject("city"), "name", "N/A");
                }
            }

            JsonObject startDate = event.getAsJsonObject("dates").getAsJsonObject("start");
            String date = getString(startDate, "localDate", "");
            String time = getString(startDate, "localTime", "N/A");

            events.add(new Event(
                    getString(event, "id", ""),
                    getString(event, "name", "N/A"),
                    city,
                    venue,
                    date,
                    time,
                    getString(event, "url", "")
            ));
        }

        return events;
    }

    private String getString(JsonObject object, String key, String defaultValue) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return defaultValue;
        }

        return object.get(key).getAsString();
    }
}
