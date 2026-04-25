package org.ulpgc.dacd.control.storage;

import org.ulpgc.dacd.model.Flight;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FlightDatabaseManager {
    private final String url;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FlightDatabaseManager(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(url)) {
            String sqlFlights = """
                CREATE TABLE IF NOT EXISTS flights (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    flight_number TEXT, 
                    origin TEXT, 
                    destination TEXT,
                    destination_city TEXT, 
                    date TEXT, 
                    scheduled_time TEXT,
                    estimated_time TEXT, 
                    status TEXT, 
                    airline TEXT,
                    terminal TEXT, 
                    flight_type TEXT, 
                    captured_at TEXT
                );
                """;
            conn.createStatement().execute(sqlFlights);
        } catch (SQLException e) {
            System.err.println("Error DB Vuelos: " + e.getMessage());
        }
    }

    public void saveFlights(List<Flight> flights) {
        String sql = "INSERT INTO flights(flight_number, origin, destination, destination_city, " +
                "date, scheduled_time, estimated_time, status, airline, terminal, " +
                "flight_type, captured_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        String timestamp = LocalDateTime.now().format(formatter);

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Flight f : flights) {
                pstmt.setString(1, f.flightNumber());
                pstmt.setString(2, f.origin());
                pstmt.setString(3, f.destination());
                pstmt.setString(4, f.destinationCity());
                pstmt.setString(5, f.date());
                pstmt.setString(6, f.scheduledTime());
                pstmt.setString(7, f.estimatedTime());
                pstmt.setString(8, f.status());
                pstmt.setString(9, f.airline());
                pstmt.setString(10, f.terminal());
                pstmt.setString(11, f.flightType());
                pstmt.setString(12, timestamp);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error al persistir vuelos: " + e.getMessage());
        }
    }
}