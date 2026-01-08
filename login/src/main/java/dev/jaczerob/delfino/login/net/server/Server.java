/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.jaczerob.delfino.login.net.server;

import com.google.protobuf.Empty;
import dev.jaczerob.delfino.grpc.proto.World;
import dev.jaczerob.delfino.grpc.proto.WorldServiceGrpc;
import dev.jaczerob.delfino.grpc.proto.character.Character;
import dev.jaczerob.delfino.grpc.proto.character.CharacterServiceGrpc;
import dev.jaczerob.delfino.grpc.proto.character.CharactersRequest;
import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.config.YamlConfig;
import dev.jaczerob.delfino.login.constants.net.OpcodeConstants;
import dev.jaczerob.delfino.login.constants.net.ServerConstants;
import dev.jaczerob.delfino.login.net.netty.LoginNettyServer;
import dev.jaczerob.delfino.login.net.server.coordinator.session.IpAddresses;
import dev.jaczerob.delfino.login.net.server.coordinator.session.SessionCoordinator;
import dev.jaczerob.delfino.login.net.server.task.LoginCoordinatorTask;
import dev.jaczerob.delfino.login.net.server.task.LoginStorageTask;
import dev.jaczerob.delfino.login.server.ThreadManager;
import dev.jaczerob.delfino.login.server.TimerManager;
import dev.jaczerob.delfino.login.tools.DatabaseConnection;
import dev.jaczerob.delfino.login.tools.Pair;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;

@Component
public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private static Server INSTANCE = null;

    public static Server getInstance() {
        return INSTANCE;
    }

    private LoginNettyServer loginNettyServer;
    private final List<Map<Integer, String>> channels = new LinkedList<>();
    private final List<World> worlds = new ArrayList<>();
    private final Map<Integer, Set<Integer>> accountChars = new HashMap<>();
    private final Map<Integer, Short> accountCharacterCount = new HashMap<>();
    private final Map<Integer, Integer> worldChars = new HashMap<>();
    private final Map<String, Integer> transitioningChars = new HashMap<>();
    private final Map<Client, Long> inLoginState = new HashMap<>(100);

    private final List<List<Pair<String, Integer>>> playerRanking = new LinkedList<>();

    private final Lock srvLock = new ReentrantLock();

    private final Lock wldRLock;

    private final Lock lgnRLock;
    private final Lock lgnWLock;

    private final AtomicLong currentTime = new AtomicLong(0);
    private long serverCurrentTime = 0;

    private boolean online = false;

    private final TimerManager timerManager;
    private final DatabaseConnection databaseConnection;
    private final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub;
    private final CharacterServiceGrpc.CharacterServiceBlockingV2Stub characterServiceStub;

    public Server(
            final TimerManager timerManager,
            final DatabaseConnection databaseConnection,
            final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub,
            final CharacterServiceGrpc.CharacterServiceBlockingV2Stub characterServiceStub
    ) {
        ReadWriteLock worldLock = new ReentrantReadWriteLock(true);
        this.wldRLock = worldLock.readLock();

        ReadWriteLock loginLock = new ReentrantReadWriteLock(true);
        this.lgnRLock = loginLock.readLock();
        this.lgnWLock = loginLock.writeLock();

        this.timerManager = timerManager;
        this.databaseConnection = databaseConnection;
        this.worldServiceStub = worldServiceStub;
        this.characterServiceStub = characterServiceStub;
    }

    @PostConstruct
    public void startServer() {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");

        INSTANCE = this;
        this.init();
    }

    public void init() {
        Instant beforeInit = Instant.now();
        log.info("Cosmic v{} starting up.", ServerConstants.VERSION);

        if (YamlConfig.config.server.SHUTDOWNHOOK) {
            Runtime.getRuntime().addShutdownHook(new Thread(shutdown(false)));
        }

        TimeZone.setDefault(TimeZone.getTimeZone(YamlConfig.config.server.TIMEZONE));

        try (Connection con = this.databaseConnection.getConnection()) {
            setAllLoggedOut(con);
        } catch (SQLException sqle) {
            log.error("Failed to run all startup-bound database tasks", sqle);
            throw new IllegalStateException(sqle);
        }

        ThreadManager.getInstance().start();
        this.initializeTimelyTasks();

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

    public long getCurrentTime() {  // returns a slightly delayed time value, under frequency of UPDATE_INTERVAL
        return serverCurrentTime;
    }

    public long forceUpdateCurrentTime() {
        long timeNow = System.currentTimeMillis();
        serverCurrentTime = timeNow;
        currentTime.set(timeNow);

        return timeNow;
    }

    public boolean isOnline() {
        return online;
    }

    public World getWorld(int id) {
        wldRLock.lock();
        try {
            try {
                return worlds.get(id);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        } finally {
            wldRLock.unlock();
        }
    }

    public List<World> getWorlds() {
        wldRLock.lock();
        try {
            return Collections.unmodifiableList(worlds);
        } finally {
            wldRLock.unlock();
        }
    }

    private String getIP(int world, int channel) {
        wldRLock.lock();
        try {
            return channels.get(world).get(channel);
        } finally {
            wldRLock.unlock();
        }
    }

    public String[] getInetSocket(Client client, int world, int channel) {
        String remoteIp = client.getRemoteAddress();

        String[] hostAddress = getIP(world, channel).split(":");
        if (IpAddresses.isLocalAddress(remoteIp)) {
            hostAddress[0] = YamlConfig.config.server.LOCALHOST;
        } else if (IpAddresses.isLanAddress(remoteIp)) {
            hostAddress[0] = YamlConfig.config.server.LANHOST;
        }

        try {
            return hostAddress;
        } catch (Exception e) {
            return null;
        }
    }

    private static long getTimeLeftForNextHour() {
        Calendar nextHour = Calendar.getInstance();
        nextHour.add(Calendar.HOUR, 1);
        nextHour.set(Calendar.MINUTE, 0);
        nextHour.set(Calendar.SECOND, 0);

        return Math.max(0, nextHour.getTimeInMillis() - System.currentTimeMillis());
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

    private void initializeTimelyTasks() {
        this.timerManager.register(this.timerManager.purge(), YamlConfig.config.server.PURGING_INTERVAL);
        disconnectIdlesOnLoginTask();

        long timeLeft = getTimeLeftForNextHour();
        this.timerManager.register(new LoginCoordinatorTask(), HOURS.toMillis(1), timeLeft);
        this.timerManager.register(new LoginStorageTask(), MINUTES.toMillis(2), MINUTES.toMillis(2));
    }

    public int getCharacterWorld(Integer chrid) {
        lgnRLock.lock();
        try {
            Integer worldid = worldChars.get(chrid);
            return worldid != null ? worldid : -1;
        } finally {
            lgnRLock.unlock();
        }
    }

    public boolean haveCharacterEntry(Integer accountid, Integer chrid) {
        lgnRLock.lock();
        try {
            Set<Integer> accChars = accountChars.get(accountid);
            return accChars.contains(chrid);
        } finally {
            lgnRLock.unlock();
        }
    }

    public short getAccountCharacterCount(Integer accountid) {
        lgnRLock.lock();
        try {
            return accountCharacterCount.get(accountid);
        } finally {
            lgnRLock.unlock();
        }
    }

    public short getAccountWorldCharacterCount(Integer accountid, Integer worldid) {
        lgnRLock.lock();
        try {
            short count = 0;

            for (Integer chr : accountChars.get(accountid)) {
                if (worldChars.get(chr).equals(worldid)) {
                    count++;
                }
            }

            return count;
        } finally {
            lgnRLock.unlock();
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

    private static String getRemoteHost(Client client) {
        return SessionCoordinator.getSessionRemoteHost(client);
    }

    public void setCharacteridInTransition(Client client, int charId) {
        String remoteIp = getRemoteHost(client);

        lgnWLock.lock();
        try {
            transitioningChars.put(remoteIp, charId);
        } finally {
            lgnWLock.unlock();
        }
    }

    public Integer freeCharacteridInTransition(Client client) {
        if (!YamlConfig.config.server.USE_IP_VALIDATION) {
            return null;
        }

        String remoteIp = getRemoteHost(client);

        lgnWLock.lock();
        try {
            return transitioningChars.remove(remoteIp);
        } finally {
            lgnWLock.unlock();
        }
    }

    public boolean hasCharacteridInTransition(Client client) {
        if (!YamlConfig.config.server.USE_IP_VALIDATION) {
            return true;
        }

        String remoteIp = getRemoteHost(client);

        lgnRLock.lock();
        try {
            return transitioningChars.containsKey(remoteIp);
        } finally {
            lgnRLock.unlock();
        }
    }

    public void registerLoginState(Client c) {
        srvLock.lock();
        try {
            inLoginState.put(c, System.currentTimeMillis() + 600000);
        } finally {
            srvLock.unlock();
        }
    }

    public void unregisterLoginState(Client c) {
        srvLock.lock();
        try {
            inLoginState.remove(c);
        } finally {
            srvLock.unlock();
        }
    }

    private void disconnectIdlesOnLoginState() {
        List<Client> toDisconnect = new LinkedList<>();

        srvLock.lock();
        try {
            long timeNow = System.currentTimeMillis();

            for (Entry<Client, Long> mc : inLoginState.entrySet()) {
                if (timeNow > mc.getValue()) {
                    toDisconnect.add(mc.getKey());
                }
            }

            for (Client c : toDisconnect) {
                inLoginState.remove(c);
            }
        } finally {
            srvLock.unlock();
        }

        for (Client c : toDisconnect) {    // thanks Lei for pointing a deadlock issue with srvLock
            if (c.isLoggedIn()) {
                c.disconnect();
            } else {
                SessionCoordinator.getInstance().closeSession(c, true);
            }
        }
    }

    private void disconnectIdlesOnLoginTask() {
        TimerManager.getInstance().register(() -> disconnectIdlesOnLoginState(), 300000);
    }

    public final Runnable shutdown(final boolean restart) {//no player should be online when trying to shutdown!
        return () -> shutdownInternal(restart);
    }

    private synchronized void shutdownInternal(boolean restart) {
        log.info("{} the server!", restart ? "Restarting" : "Shutting down");

        ThreadManager.getInstance().stop();
        TimerManager.getInstance().purge();
        TimerManager.getInstance().stop();

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
