package org.ulpgc.dacd.storage;

import org.ulpgc.dacd.model.Event;
import org.ulpgc.dacd.model.Flight;
import org.ulpgc.dacd.model.Recommendation;
import org.ulpgc.dacd.model.RecommendationConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatamartRepository {
    private static final String URL = "jdbc:sqlite:business-unit.db";

    public DatamartRepository() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS events (
                        id TEXT PRIMARY KEY,
                        name TEXT,
                        city TEXT,
                        venue TEXT,
                        date TEXT,
                        start_time TEXT,
                        url TEXT,
                        captured_at TEXT
                    );
                    """);

            statement.execute("""
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
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS flight_event_recommendations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        event_id TEXT,
                        event_name TEXT,
                        event_city TEXT,
                        event_date TEXT,
                        event_start_time TEXT,
                        event_end_time TEXT,
                        outbound_flight_number TEXT,
                        outbound_airline TEXT,
                        outbound_origin TEXT,
                        outbound_destination TEXT,
                        outbound_departure_time TEXT,
                        outbound_arrival_time TEXT,
                        return_flight_number TEXT,
                        return_airline TEXT,
                        return_origin TEXT,
                        return_destination TEXT,
                        return_departure_time TEXT,
                        captured_at TEXT
                    );
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS recommendation_config (
                        id INTEGER PRIMARY KEY CHECK (id = 1),
                        origin_airport TEXT,
                        outbound_margin_hours INTEGER,
                        return_margin_hours INTEGER,
                        default_event_duration_hours INTEGER
                    );
                    """);

            statement.execute("""
                    INSERT OR IGNORE INTO recommendation_config (
                        id,
                        origin_airport,
                        outbound_margin_hours,
                        return_margin_hours,
                        default_event_duration_hours
                    ) VALUES (1, 'LPA', 3, 2, 3);
                    """);

        } catch (SQLException e) {
            throw new RuntimeException("Error initializing business-unit datamart", e);
        }
    }

    public void saveEvent(Event event) {
        String sql = """
                INSERT OR REPLACE INTO events (
                    id, name, city, venue, date, start_time, url, captured_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection = DriverManager.getConnection(URL);
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

    public void saveFlight(Flight flight) {
        String sql = """
                INSERT INTO flights (
                    flight_number, origin, destination, destination_city,
                    date, scheduled_time, estimated_time, status, airline,
                    terminal, flight_type, captured_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection = DriverManager.getConnection(URL);
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

    public RecommendationConfig getConfig() {
        String sql = """
                SELECT origin_airport, outbound_margin_hours,
                       return_margin_hours, default_event_duration_hours
                FROM recommendation_config
                WHERE id = 1;
                """;

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (!resultSet.next()) {
                return new RecommendationConfig("LPA", 3, 2, 3);
            }

            return new RecommendationConfig(
                    resultSet.getString("origin_airport"),
                    resultSet.getInt("outbound_margin_hours"),
                    resultSet.getInt("return_margin_hours"),
                    resultSet.getInt("default_event_duration_hours")
            );

        } catch (SQLException e) {
            throw new RuntimeException("Error reading recommendation config", e);
        }
    }

    public List<Event> findAllEvents() {
        String sql = """
                SELECT id, name, city, venue, date, start_time, url, captured_at
                FROM events;
                """;

        List<Event> events = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL);
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

    public List<Flight> findAllFlights() {
        String sql = """
                SELECT flight_number, origin, destination, destination_city,
                       date, scheduled_time, estimated_time, status, airline,
                       terminal, flight_type, captured_at
                FROM flights;
                """;

        List<Flight> flights = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL);
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

    public void clearRecommendations() {
        String sql = "DELETE FROM flight_event_recommendations;";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error clearing recommendations", e);
        }
    }

    public void saveRecommendation(Recommendation recommendation) {
        String sql = """
                INSERT INTO flight_event_recommendations (
                    event_id, event_name, event_city, event_date,
                    event_start_time, event_end_time,
                    outbound_flight_number, outbound_airline,
                    outbound_origin, outbound_destination,
                    outbound_departure_time, outbound_arrival_time,
                    return_flight_number, return_airline,
                    return_origin, return_destination,
                    return_departure_time, captured_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, recommendation.getEventId());
            statement.setString(2, recommendation.getEventName());
            statement.setString(3, recommendation.getEventCity());
            statement.setString(4, recommendation.getEventDate());
            statement.setString(5, recommendation.getEventStartTime());
            statement.setString(6, recommendation.getEventEndTime());
            statement.setString(7, recommendation.getOutboundFlightNumber());
            statement.setString(8, recommendation.getOutboundAirline());
            statement.setString(9, recommendation.getOutboundOrigin());
            statement.setString(10, recommendation.getOutboundDestination());
            statement.setString(11, recommendation.getOutboundDepartureTime());
            statement.setString(12, recommendation.getOutboundArrivalTime());
            statement.setString(13, recommendation.getReturnFlightNumber());
            statement.setString(14, recommendation.getReturnAirline());
            statement.setString(15, recommendation.getReturnOrigin());
            statement.setString(16, recommendation.getReturnDestination());
            statement.setString(17, recommendation.getReturnDepartureTime());
            statement.setString(18, recommendation.getCapturedAt());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving recommendation", e);
        }
    }

    public int countEvents() {
        return countRows("events");
    }

    public int countFlights() {
        return countRows("flights");
    }

    public int countRecommendations() {
        return countRows("flight_event_recommendations");
    }

    private int countRows(String table) {
        String sql = "SELECT COUNT(*) AS total FROM " + table;

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error counting rows from " + table, e);
        }
    }
}
