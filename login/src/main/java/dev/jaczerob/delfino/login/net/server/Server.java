package dev.jaczerob.delfino.login.net.server;

import com.google.protobuf.Empty;
import dev.jaczerob.delfino.grpc.proto.World;
import dev.jaczerob.delfino.grpc.proto.WorldServiceGrpc;
import dev.jaczerob.delfino.grpc.proto.character.Character;
import dev.jaczerob.delfino.grpc.proto.character.CharacterServiceGrpc;
import dev.jaczerob.delfino.grpc.proto.character.CharactersRequest;
import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.constants.net.OpcodeConstants;
import dev.jaczerob.delfino.login.constants.net.ServerConstants;
import dev.jaczerob.delfino.login.net.netty.LoginNettyServer;
import dev.jaczerob.delfino.login.server.ThreadManager;
import dev.jaczerob.delfino.login.tools.DatabaseConnection;
import io.grpc.StatusException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

@Component
public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private static Server INSTANCE = null;

    public static Server getInstance() {
        return INSTANCE;
    }

    private LoginNettyServer loginNettyServer;
    private final List<World> worlds = new ArrayList<>();

    private boolean online = false;

    private final DatabaseConnection databaseConnection;
    private final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub;
    private final CharacterServiceGrpc.CharacterServiceBlockingV2Stub characterServiceStub;

    public Server(
            final DatabaseConnection databaseConnection,
            final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub,
            final CharacterServiceGrpc.CharacterServiceBlockingV2Stub characterServiceStub
    ) {
        this.databaseConnection = databaseConnection;
        this.worldServiceStub = worldServiceStub;
        this.characterServiceStub = characterServiceStub;
    }

    @PostConstruct
    public void startServer() {
        INSTANCE = this;
        this.init();
    }

    public void init() {
        Instant beforeInit = Instant.now();
        log.info("Cosmic v{} starting up.", ServerConstants.VERSION);
        Runtime.getRuntime().addShutdownHook(new Thread(shutdown(false)));

        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")));

        try (Connection con = this.databaseConnection.getConnection()) {
            setAllLoggedOut(con);
        } catch (SQLException sqle) {
            log.error("Failed to run all startup-bound database tasks", sqle);
            throw new IllegalStateException(sqle);
        }

        ThreadManager.getInstance().start();

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

        this.loginNettyServer = initLoginServer(8484);

        log.info("Listening on port 8484");

        online = true;
        Duration initDuration = Duration.between(beforeInit, Instant.now());
        log.info("Cosmic is now online after {} ms.", initDuration.toMillis());

        OpcodeConstants.generateOpcodeNames();
    }

    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public boolean isOnline() {
        return online;
    }

    public String[] getInetSocket() {
        // TODO: Implement proper world/channel IP retrieval
        return new String[]{
                "127.0.0.1",
                "7575"
        };
    }

    private LoginNettyServer initLoginServer(int port) {
        LoginNettyServer srv = new LoginNettyServer(port);
        srv.start();
        return srv;
    }

    private static void setAllLoggedOut(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = 0")) {
            ps.executeUpdate();
        }
    }

    public List<Character> loadCharacters(int accId) {
        final var charactersRequest = CharactersRequest.newBuilder()
                .setAccountId(accId)
                .build();

        try {
            return this.characterServiceStub.getCharacters(charactersRequest).getCharactersList();
        } catch (final StatusException exc) {
            log.error("Failed to load characters for account id {} from character service", accId, exc);
            return Collections.emptyList();
        }
    }

    public void loadAccountCharacters(Client c) {
        final var gmLevel = this.loadCharacters(c.getAccID()).stream()
                .mapToInt(Character::getGmLevel)
                .max()
                .orElse(0);

        c.setGMLevel(gmLevel);
    }

    public void loadAccountStorages(Client c) {
    }

    public boolean hasCharacteridInTransition(final Client client) {
        return true;
    }

    public final Runnable shutdown(final boolean restart) {//no player should be online when trying to shutdown!
        return () -> shutdownInternal(restart);
    }

    private synchronized void shutdownInternal(boolean restart) {
        log.info("{} the server!", restart ? "Restarting" : "Shutting down");

        ThreadManager.getInstance().stop();

        log.info("Worlds and channels are offline.");
        loginNettyServer.stop();
        if (!restart) {
            new Thread(() -> System.exit(0)).start();
        } else {
            log.info("Restarting the server...");
            this.init();
        }
    }
}
