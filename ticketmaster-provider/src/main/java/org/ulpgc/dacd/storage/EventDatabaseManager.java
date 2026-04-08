package org.ulpgc.dacd.storage;

import org.ulpgc.dacd.model.Event;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventDatabaseManager {
    private static final String URL = "jdbc:sqlite:events.db";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EventDatabaseManager() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            String sql = "CREATE TABLE IF NOT EXISTS events (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "event_api_id TEXT, name TEXT, city TEXT, venue TEXT, date TEXT, captured_at TEXT);";
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveEvents(List<Event> events) {
        String sql = "INSERT INTO events(event_api_id, name, city, venue, date, captured_at) VALUES(?,?,?,?,?,?)";
        String timestamp = LocalDateTime.now().format(formatter);

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Event e : events) {
                pstmt.setString(1, e.getId());
                pstmt.setString(2, e.getName());
                pstmt.setString(3, e.getCity());
                pstmt.setString(4, e.getVenue());
                pstmt.setString(5, e.getDate());
                pstmt.setString(6, timestamp);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}