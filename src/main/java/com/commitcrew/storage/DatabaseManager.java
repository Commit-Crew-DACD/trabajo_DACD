package com.commitcrew.storage;

import com.commitcrew.model.Event;
import com.commitcrew.model.Flight;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:vuelo_evento.db";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DatabaseManager() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            if (conn != null) {
                String sqlEvents = "CREATE TABLE IF NOT EXISTS events (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "event_api_id TEXT," +
                        "name TEXT," +
                        "city TEXT," +
                        "venue TEXT," +
                        "date TEXT," +
                        "captured_at TEXT" +
                        ");";

                String sqlFlights = "CREATE TABLE IF NOT EXISTS flights (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "flight_number TEXT," +
                        "origin TEXT," +
                        "destination TEXT," +
                        "destination_city TEXT," +
                        "date TEXT," +
                        "scheduled_time TEXT," +
                        "estimated_time TEXT," +
                        "status TEXT," +
                        "airline TEXT," +
                        "terminal TEXT," +
                        "flight_type TEXT," +
                        "captured_at TEXT" +
                        ");";

                Statement stmt = conn.createStatement();
                stmt.execute(sqlEvents);
                stmt.execute(sqlFlights);
            }
        } catch (SQLException e) {
            System.err.println("Error al inicializar SQLite: " + e.getMessage());
        }
    }

    public void saveEvents(List<Event> events) {
        String sql = "INSERT INTO events(event_api_id, name, city, venue, date, captured_at) VALUES(?,?,?,?,?,?)";
        String timestamp = LocalDateTime.now().format(formatter);

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Event event : events) {
                pstmt.setString(1, event.getId());
                pstmt.setString(2, event.getName());
                pstmt.setString(3, event.getCity());
                pstmt.setString(4, event.getVenue());
                pstmt.setString(5, event.getDate());
                pstmt.setString(6, timestamp);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("Éxito: Se han persistido " + events.size() + " eventos en la DB.");
        } catch (SQLException e) {
            System.err.println("Error al guardar eventos: " + e.getMessage());
        }
    }

    public void saveFlights(List<Flight> flights) {
        String sql = "INSERT INTO flights(" +
                "flight_number, origin, destination, destination_city, " +
                "date, scheduled_time, estimated_time, " +
                "status, airline, terminal, flight_type, captured_at" +
                ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        String timestamp = LocalDateTime.now().format(formatter);

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Flight f : flights) {
                pstmt.setString(1, f.getFlightNumber());
                pstmt.setString(2, f.getOrigin());
                pstmt.setString(3, f.getDestination());
                pstmt.setString(4, f.getDestinationCity());
                pstmt.setString(5, f.getDate());
                pstmt.setString(6, f.getScheduledTime());
                pstmt.setString(7, f.getEstimatedTime());
                pstmt.setString(8, f.getStatus());
                pstmt.setString(9, f.getAirline());
                pstmt.setString(10, f.getTerminal());
                pstmt.setString(11, f.getFlightType());
                pstmt.setString(12, timestamp);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("Éxito: Se han persistido " + flights.size() + " vuelos en la DB.");
        } catch (SQLException e) {
            System.err.println("Error al guardar vuelos: " + e.getMessage());
        }
    }
}