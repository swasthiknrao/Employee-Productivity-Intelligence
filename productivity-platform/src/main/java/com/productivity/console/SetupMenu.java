package com.productivity.console;

import com.productivity.config.DatabaseConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Setup: test connection, optionally run schema.sql from classpath.
 */
public class SetupMenu {

    private final Scanner scanner;

    public SetupMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("\n--- Setup ---");
        System.out.println("1. Test database connection");
        System.out.println("2. Initialize schema (run schema.sql)");
        System.out.println("0. Back");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                testConnection();
                break;
            case "2":
                runSchema();
                break;
            case "0":
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void testConnection() {
        try {
            if (DatabaseConfig.testConnection()) {
                String url = DatabaseConfig.getUrl();
                System.out.println("Connection OK." + (url != null && !url.isEmpty() ? " URL: " + url : ""));
            } else {
                System.out.println("Connection failed. Check db.properties (jdbc.url, jdbc.user, jdbc.password).");
            }
        } catch (Exception e) {
            System.out.println("Connection failed: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            System.out.println("Ensure src/main/resources/db.properties exists with jdbc.url, jdbc.user, jdbc.password.");
        }
    }

    private void runSchema() {
        System.out.println("This will run schema.sql from classpath. Ensure database exists or create it manually first.");
        System.out.print("Proceed? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
            return;
        }
        try (InputStream in = SetupMenu.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) {
                System.out.println("schema.sql not found in classpath.");
                return;
            }
            String sql = readFully(in);
            List<String> statements = splitStatements(sql);
            try (Connection conn = DatabaseConfig.getConnection(); Statement st = conn.createStatement()) {
                for (String s : statements) {
                    String t = s.trim();
                    if (t.isEmpty() || t.startsWith("--")) continue;
                    try {
                        st.execute(s);
                    } catch (Exception e) {
                        System.out.println("Statement failed: " + t.substring(0, Math.min(60, t.length())) + "... " + e.getMessage());
                    }
                }
                System.out.println("Schema execution finished.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static String readFully(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private static List<String> splitStatements(String sql) {
        List<String> list = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inDelimiter = false;
        for (String line : sql.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--")) continue;
            if (trimmed.toUpperCase().startsWith("USE ")) {
                if (current.length() > 0) {
                    list.add(current.toString().trim());
                    current = new StringBuilder();
                }
                list.add(trimmed);
                continue;
            }
            current.append(line).append("\n");
        }
        if (current.length() > 0) {
            list.add(current.toString().trim());
        }
        return list;
    }
}
