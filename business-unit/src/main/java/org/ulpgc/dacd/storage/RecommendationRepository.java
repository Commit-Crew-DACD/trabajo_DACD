package org.ulpgc.dacd.storage;

import org.ulpgc.dacd.model.Recommendation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecommendationRepository {
    private final DatabaseManager databaseManager;

    public RecommendationRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void save(Recommendation recommendation) {
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

        try (Connection connection = databaseManager.getConnection();
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

    public List<Recommendation> findAll() {
        String sql = """
                SELECT event_id, event_name, event_city, event_date,
                       event_start_time, event_end_time,
                       outbound_flight_number, outbound_airline,
                       outbound_origin, outbound_destination,
                       outbound_departure_time, outbound_arrival_time,
                       return_flight_number, return_airline,
                       return_origin, return_destination,
                       return_departure_time, captured_at
                FROM flight_event_recommendations
                ORDER BY event_date, event_start_time, event_name,
                         outbound_arrival_time DESC, return_departure_time ASC;
                """;

        List<Recommendation> recommendations = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                recommendations.add(new Recommendation(
                        resultSet.getString("event_id"),
                        resultSet.getString("event_name"),
                        resultSet.getString("event_city"),
                        resultSet.getString("event_date"),
                        resultSet.getString("event_start_time"),
                        resultSet.getString("event_end_time"),
                        resultSet.getString("outbound_flight_number"),
                        resultSet.getString("outbound_airline"),
                        resultSet.getString("outbound_origin"),
                        resultSet.getString("outbound_destination"),
                        resultSet.getString("outbound_departure_time"),
                        resultSet.getString("outbound_arrival_time"),
                        resultSet.getString("return_flight_number"),
                        resultSet.getString("return_airline"),
                        resultSet.getString("return_origin"),
                        resultSet.getString("return_destination"),
                        resultSet.getString("return_departure_time"),
                        resultSet.getString("captured_at")
                ));
            }

            return recommendations;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding recommendations", e);
        }
    }

    public void clear() {
        String sql = "DELETE FROM flight_event_recommendations;";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error clearing recommendations", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) AS total FROM flight_event_recommendations;";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return resultSet.next() ? resultSet.getInt("total") : 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error counting recommendations", e);
        }
    }
}
