package com.productivity;

import com.productivity.console.MenuDriver;

public class Main {

    public static void main(String[] args) {
        try {
            new MenuDriver().run();
        } catch (IllegalStateException e) {
            System.err.println("Configuration error: " + e.getMessage());
            System.err.println("Copy db.properties.example to src/main/resources/db.properties and set jdbc.url, jdbc.user, jdbc.password. Then run again.");
        } catch (Exception e) {
            System.err.println("Fatal: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            e.printStackTrace();
        }
    }
}
