package org.ulpgc.dacd.storage;

import org.ulpgc.dacd.model.Flight;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlightRepository {
    private final DatabaseManager databaseManager;

    public FlightRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void save(Flight flight) {
        String sql = """
                INSERT INTO flights (
                    flight_number, origin, destination, destination_city,
                    date, scheduled_time, estimated_time, status, airline,
                    terminal, flight_type, captured_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, flight.getFlightNumber());
            statement.setString(2, flight.getOrigin());
            statement.setString(3, flight.getDestination());
            statement.setString(4, flight.getDestinationCity());
            statement.setString(5, flight.getDate());
            statement.setString(6, flight.getScheduledTime());
            statement.setString(7, flight.getEstimatedTime());
            statement.setString(8, flight.getStatus());
            statement.setString(9, flight.getAirline());
            statement.setString(10, flight.getTerminal());
            statement.setString(11, flight.getFlightType());
            statement.setString(12, flight.getCapturedAt());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving flight in datamart", e);
        }
    }

    public List<Flight> findAll() {
        String sql = """
                SELECT flight_number, origin, destination, destination_city,
                       date, scheduled_time, estimated_time, status, airline,
                       terminal, flight_type, captured_at
                FROM flights;
                """;

        List<Flight> flights = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                flights.add(new Flight(
                        resultSet.getString("flight_number"),
                        resultSet.getString("origin"),
                        resultSet.getString("destination"),
                        resultSet.getString("destination_city"),
                        resultSet.getString("date"),
                        resultSet.getString("scheduled_time"),
                        resultSet.getString("estimated_time"),
                        resultSet.getString("status"),
                        resultSet.getString("airline"),
                        resultSet.getString("terminal"),
                        resultSet.getString("flight_type"),
                        resultSet.getString("captured_at")
                ));
            }

            return flights;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding flights", e);
        }
    }

    public void clear() {
        String sql = "DELETE FROM flights;";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error clearing flights", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) AS total FROM flights;";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return resultSet.next() ? resultSet.getInt("total") : 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error counting flights", e);
        }
    }
}
