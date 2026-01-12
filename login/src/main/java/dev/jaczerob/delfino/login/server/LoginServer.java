package dev.jaczerob.delfino.login.server;

import com.google.protobuf.Empty;
import dev.jaczerob.delfino.grpc.proto.World;
import dev.jaczerob.delfino.grpc.proto.WorldServiceGrpc;
import dev.jaczerob.delfino.login.tools.DatabaseConnection;
import dev.jaczerob.delfino.network.server.AbstractServer;
import io.grpc.StatusException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Slf4j
@Component
public class LoginServer extends AbstractServer {
    private static LoginServer INSTANCE = null;

    private boolean online = false;

    private final List<World> worlds = new ArrayList<>();
    private final DatabaseConnection databaseConnection;
    private final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub;

    public LoginServer(
            final @Value("${server.port}") int port,
            final LoginServerInitializer loginServerInitializer,
            final DatabaseConnection databaseConnection,
            final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub
    ) {
        super(port, loginServerInitializer);
        this.databaseConnection = databaseConnection;
        this.worldServiceStub = worldServiceStub;
    }

    @PostConstruct
    public void start() {
        this.startServer();
        this.init();
        INSTANCE = this;
    }

    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")));

        try (Connection con = this.databaseConnection.getConnection()) {
            setAllLoggedOut(con);
        } catch (final SQLException exc) {
            log.error("Failed to run all startup-bound database tasks", exc);
            throw new IllegalStateException(exc);
        }

        try {
            final var loadedWorlds = this.worldServiceStub.getWorlds(Empty.newBuilder().build()).getWorldsList();
            this.worlds.addAll(loadedWorlds);
        } catch (final StatusException exc) {
            log.error("Failed to load worlds from world server, is world server down?", exc);
            throw new IllegalStateException("Failed to load worlds from world server, is world server down", exc);
        }

        if (this.worlds.isEmpty()) {
            log.error("No worlds loaded from world server, cannot start login server");
            throw new IllegalStateException("No worlds loaded from world server");
        }

        online = true;
    }

    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public boolean isOnline() {
        return this.online;
    }

    public String[] getInetSocket() {
        // TODO: Implement proper world/channel IP retrieval
        return new String[]{
                "127.0.0.1",
                "7575"
        };
    }

    private static void setAllLoggedOut(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = 0")) {
            ps.executeUpdate();
        }
    }

    public static LoginServer getInstance() {
        return INSTANCE;
    }

    @PreDestroy
    public void stop() {
        this.stopServer();
    }
}
