package dev.jaczerob.delfino.maplestory.tools;

import dev.jaczerob.delfino.maplestory.database.note.NoteRowMapper;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseConnection {
    private static DatabaseConnection INSTANCE;

    private final DataSource dataSource;
    private final Jdbi jdbi;

    public DatabaseConnection(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbi = Jdbi.create(dataSource).registerRowMapper(new NoteRowMapper());
        INSTANCE = this;
    }

    public static Connection getStaticConnection() throws SQLException {
        if (INSTANCE == null) {
            throw new IllegalStateException("Unable to get connection - connection pool is uninitialized");
        }

        return INSTANCE.dataSource.getConnection();
    }

    public static Handle getStaticHandle() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Unable to get handle - connection pool is uninitialized");
        }

        return INSTANCE.jdbi.open();
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public Handle getHandle() {
        return this.jdbi.open();
    }
}
