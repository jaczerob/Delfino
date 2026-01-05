package dev.jaczerob.delfino.maplestory.tools.mapletools;

import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

final class SimpleDatabaseConnection {
    private SimpleDatabaseConnection() {
    }

    static Connection getConnection() {
        DatabaseConnection.initializeConnectionPool();

        try {
            return DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get database connection", e);
        }
    }
}
