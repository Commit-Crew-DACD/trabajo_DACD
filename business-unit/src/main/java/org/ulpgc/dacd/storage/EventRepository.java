package org.ulpgc.dacd.storage;

import org.ulpgc.dacd.model.Event;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventRepository {
    private final DatabaseManager databaseManager;

    public EventRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void save(Event event) {
        String sql = """
                INSERT OR REPLACE INTO events (
                    id, name, city, venue, date, start_time, url, captured_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, event.getId());
            statement.setString(2, event.getName());
            statement.setString(3, event.getCity());
            statement.setString(4, event.getVenue());
            statement.setString(5, event.getDate());
            statement.setString(6, event.getStartTime());
            statement.setString(7, event.getUrl());
            statement.setString(8, event.getCapturedAt());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving event in datamart", e);
        }
    }

    public List<Event> findAll() {
        String sql = """
                SELECT id, name, city, venue, date, start_time, url, captured_at
                FROM events
                ORDER BY date, start_time, name;
                """;

        List<Event> events = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                events.add(new Event(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getString("city"),
                        resultSet.getString("venue"),
                        resultSet.getString("date"),
                        resultSet.getString("start_time"),
                        resultSet.getString("url"),
                        resultSet.getString("captured_at")
                ));
            }

            return events;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding events", e);
        }
    }

    public void clear() {
        String sql = "DELETE FROM events;";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error clearing events", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) AS total FROM events;";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return resultSet.next() ? resultSet.getInt("total") : 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error counting events", e);
        }
    }
}
