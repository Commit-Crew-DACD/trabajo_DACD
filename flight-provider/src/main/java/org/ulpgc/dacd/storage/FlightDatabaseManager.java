package org.ulpgc.dacd.storage;

import org.ulpgc.dacd.model.Flight;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FlightDatabaseManager {
    private static final String URL = "jdbc:sqlite:flights.db";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FlightDatabaseManager() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            String sqlFlights = "CREATE TABLE IF NOT EXISTS flights (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "flight_number TEXT, origin TEXT, destination TEXT," +
                    "destination_city TEXT, date TEXT, scheduled_time TEXT," +
                    "estimated_time TEXT, status TEXT, airline TEXT," +
                    "terminal TEXT, flight_type TEXT, captured_at TEXT);";
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
        } catch (SQLException e) {
            System.err.println("Error al persistir vuelos: " + e.getMessage());
        }
    }
}