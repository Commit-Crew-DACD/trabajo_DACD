package org.ulpgc.dacd.storage;

import org.ulpgc.dacd.model.RecommendationConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigRepository {
    private final DatabaseManager databaseManager;

    public ConfigRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public RecommendationConfig getConfig() {
        String sql = """
                SELECT origin_airport, outbound_margin_hours,
                       return_margin_hours, default_event_duration_hours
                FROM recommendation_config
                WHERE id = 1;
                """;

        try (Connection connection = databaseManager.getConnection();
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
}
