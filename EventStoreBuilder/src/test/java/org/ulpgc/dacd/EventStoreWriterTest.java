package org.ulpgc.dacd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EventStoreWriterTest {

    @TempDir
    Path tempDir;

    private EventStoreWriter writer;

    @BeforeEach
    void setUp() {
        writer = new EventStoreWriter(tempDir.toString());
    }

    @Test
    void testWriteCreatesCorrectFile() {
        String json = "{\"flightNumber\":\"1234\",\"origin\":\"LPA\"," +
                "\"ts\":\"2026-04-27T10:00:00Z\",\"ss\":\"flight-provider\"}";

        writer.write("Flight", json);

        Path expectedFile = tempDir
                .resolve("Flight")
                .resolve("flight-provider")
                .resolve("20260427.events");

        assertTrue(Files.exists(expectedFile), "El fichero .events debe existir");
    }

    @Test
    void testWriteAppendsCorrectContent() throws Exception {
        String json = "{\"flightNumber\":\"1234\",\"origin\":\"LPA\"," +
                "\"ts\":\"2026-04-27T10:00:00Z\",\"ss\":\"flight-provider\"}";

        writer.write("Flight", json);

        Path file = tempDir
                .resolve("Flight")
                .resolve("flight-provider")
                .resolve("20260427.events");

        String content = Files.readString(file);
        assertTrue(content.contains("\"flightNumber\":\"1234\""));
        assertTrue(content.contains("\"ss\":\"flight-provider\""));
    }

    @Test
    void testWriteMultipleEventsAppendsLines() throws Exception {
        String json1 = "{\"flightNumber\":\"1111\",\"ts\":\"2026-04-27T10:00:00Z\",\"ss\":\"flight-provider\"}";
        String json2 = "{\"flightNumber\":\"2222\",\"ts\":\"2026-04-27T11:00:00Z\",\"ss\":\"flight-provider\"}";

        writer.write("Flight", json1);
        writer.write("Flight", json2);

        Path file = tempDir
                .resolve("Flight")
                .resolve("flight-provider")
                .resolve("20260427.events");
        try (java.util.stream.Stream<String> lines = Files.lines(file)) {
            long lineCount = lines.count();
            assertEquals(2, lineCount, "Deben haber 2 líneas en el fichero");
        }
    }

    @Test
    void testWriteDifferentTopicsCreatesDifferentFolders() {
        String flightJson = "{\"ts\":\"2026-04-27T10:00:00Z\",\"ss\":\"flight-provider\"}";
        String eventJson = "{\"ts\":\"2026-04-27T10:00:00Z\",\"ss\":\"ticketmaster-provider\"}";

        writer.write("Flight", flightJson);
        writer.write("Prediction", eventJson);

        assertTrue(Files.exists(tempDir.resolve("Flight")));
        assertTrue(Files.exists(tempDir.resolve("Prediction")));
    }
}