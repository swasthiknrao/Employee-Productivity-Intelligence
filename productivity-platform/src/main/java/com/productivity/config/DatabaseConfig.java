package com.productivity.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Loads db.properties and provides JDBC connections.
 */
public class DatabaseConfig {

    private static final String PROPS_FILE = "db.properties";
    private static String url;
    private static String user;
    private static String password;
    private static boolean loaded;

    static {
        load();
    }

    private static void load() {
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream(PROPS_FILE)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + PROPS_FILE + ". Copy from db.properties.example and set jdbc.url, jdbc.user, jdbc.password.");
            }
            Properties p = new Properties();
            p.load(in);
            url = p.getProperty("jdbc.url");
            user = p.getProperty("jdbc.user");
            password = p.getProperty("jdbc.password");
            if (url == null || user == null || password == null) {
                throw new IllegalStateException("db.properties must define jdbc.url, jdbc.user, jdbc.password.");
            }
            loaded = true;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + PROPS_FILE + ": " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!loaded) {
            load();
        }
        return DriverManager.getConnection(url, user, password);
    }

    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getUrl() {
        return (url != null) ? url : "";
    }
}
