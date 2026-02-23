package com.femfort.listener;

import com.femfort.dao.DatabaseConnectionManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initializing Database...");
        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            // Run Schema
            runScript(conn, "schema.sql");

            // Check if data exists before running seed
            if (isTableEmpty(conn, "users")) {
                System.out.println("Seeding database...");
                runScript(conn, "seed_data.sql");
            } else {
                System.out.println("Database already seeded.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    private void runScript(Connection conn, String filename) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (is == null) {
                System.err.println("Script not found: " + filename);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String sql = reader.lines().collect(Collectors.joining("\n"));
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }
        }
    }

    private boolean isTableEmpty(Connection conn, String tableName) {
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (Exception e) {
            // Table might not exist yet if schema failed, or other error
            return true;
        }
        return true;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
    }
}
