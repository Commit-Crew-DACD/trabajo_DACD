package org.ulpgc.dacd.control;

import org.ulpgc.dacd.storage.DatamartRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class EventStoreLoader {
    private final Path eventStorePath;
    private final EventMessageParser parser;
    private final DatamartRepository repository;

    public EventStoreLoader(String eventStorePath, EventMessageParser parser, DatamartRepository repository) {
        this.eventStorePath = ProjectPaths.resolve(eventStorePath);
        this.parser = parser;
        this.repository = repository;
    }

    public void load() {
        if (!Files.exists(eventStorePath)) {
            System.out.println("Eventstore not found: " + eventStorePath);
            return;
        }

        try (Stream<Path> files = Files.walk(eventStorePath)) {
            files.filter(path -> path.toString().endsWith(".events"))
                    .sorted()
                    .forEach(this::loadFile);
        } catch (IOException e) {
            throw new RuntimeException("Error reading eventstore", e);
        }
    }

    private void loadFile(Path file) {
        try (Stream<String> lines = Files.lines(file)) {
            lines.filter(line -> !line.isBlank())
                    .forEach(this::loadLine);
        } catch (IOException e) {
            throw new RuntimeException("Error reading eventstore file: " + file, e);
        }
    }

    private void loadLine(String json) {
        try {
            if (parser.isEventMessage(json)) {
                repository.saveEvent(parser.parseEvent(json));
                return;
            }

            if (parser.isFlightMessage(json)) {
                repository.saveFlight(parser.parseFlight(json));
            }
        } catch (Exception e) {
            System.err.println("Skipping invalid eventstore line: " + e.getMessage());
        }
    }
}
