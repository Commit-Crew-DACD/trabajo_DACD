package org.ulpgc.dacd;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class EventStoreWriter {
    private static final String BASE_PATH = "eventstore";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    public void write(String topic, String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String ss = obj.get("ss").getAsString();
            String ts = obj.get("ts").getAsString();
            String date = DATE_FORMATTER.format(Instant.parse(ts));

            Path dir = Paths.get(BASE_PATH, topic, ss);
            Files.createDirectories(dir);

            Path file = dir.resolve(date + ".events");
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(file.toFile(), true))) {
                writer.write(json);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo evento: " + e.getMessage());
        }
    }
}