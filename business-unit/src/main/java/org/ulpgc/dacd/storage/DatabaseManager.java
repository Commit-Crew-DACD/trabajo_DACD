package org.ulpgc.dacd.storage;

import org.ulpgc.dacd.control.ProjectPaths;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final Path DATABASE_PATH = ProjectPaths.resolve("business-unit.db");
    private static final String URL = "jdbc:sqlite:" + DATABASE_PATH;

    public DatabaseManager() {
        initDatabase();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private void initDatabase() {
        try (Connection connection = getConnection();
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
                        captured_at TEXT,
                        PRIMARY KEY (flight_number, date, airline)
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
}
