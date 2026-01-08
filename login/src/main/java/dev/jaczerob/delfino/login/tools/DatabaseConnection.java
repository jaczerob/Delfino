package dev.jaczerob.delfino.login.tools;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseConnection {
    private static DatabaseConnection INSTANCE;

    private final DataSource dataSource;

    public DatabaseConnection(final DataSource dataSource) {
        this.dataSource = dataSource;
        INSTANCE = this;
    }

    public static Connection getStaticConnection() throws SQLException {
        if (INSTANCE == null) {
            throw new IllegalStateException("Unable to get connection - connection pool is uninitialized");
        }

        return INSTANCE.dataSource.getConnection();
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
}
