package org.ulpgc.dacd.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ulpgc.dacd.model.Event;
import org.ulpgc.dacd.model.Flight;

public class EventMessageParser {

    private JsonObject parse(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    public boolean isEventMessage(String json) {
        return "ticketmaster-provider".equals(getString(parse(json), "ss"));
    }

    public boolean isFlightMessage(String json) {
        return "flight-provider".equals(getString(parse(json), "ss"));
    }

    public Event parseEvent(String json) {
        JsonObject object = parse(json);

        return new Event(
                getString(object, "id"),
                getString(object, "name"),
                getString(object, "city"),
                getString(object, "venue"),
                getString(object, "date"),
                getString(object, "time"),
                getString(object, "url"),
                getString(object, "ts")
        );
    }

    public Flight parseFlight(String json) {
        JsonObject object = parse(json);

        return new Flight(
                getString(object, "flightNumber"),
                getString(object, "origin"),
                getString(object, "destination"),
                getString(object, "destinationCity"),
                getString(object, "date"),
                getString(object, "scheduledTime"),
                getString(object, "estimatedTime"),
                getString(object, "status"),
                getString(object, "airline"),
                getString(object, "terminal"),
                getString(object, "flightType"),
                getString(object, "ts")
        );
    }

    private String getString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return "";
        }
        return object.get(key).getAsString();
    }
}