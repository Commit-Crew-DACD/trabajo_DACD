package org.ulpgc.dacd.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ulpgc.dacd.model.Event;
import org.ulpgc.dacd.model.Flight;

public class EventMessageParser {

    public boolean isEventMessage(String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        return object.has("ss") && "ticketmaster-provider".equals(object.get("ss").getAsString());
    }

    public boolean isFlightMessage(String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        return object.has("ss") && "flight-provider".equals(object.get("ss").getAsString());
    }

    public Event parseEvent(String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();

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
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();

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
