package dev.jaczerob.delfino.maplestory.net.server;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.CashIdGenerator;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.constants.net.OpcodeConstants;
import dev.jaczerob.delfino.maplestory.constants.net.ServerConstants;
import dev.jaczerob.delfino.maplestory.net.server.channel.Channel;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.session.IpAddresses;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.session.SessionCoordinator;
import dev.jaczerob.delfino.maplestory.net.server.task.CharacterDiseaseTask;
import dev.jaczerob.delfino.maplestory.net.server.task.InvitationTask;
import dev.jaczerob.delfino.maplestory.net.server.task.RespawnTask;
import dev.jaczerob.delfino.maplestory.net.server.world.World;
import dev.jaczerob.delfino.maplestory.server.CashShop.CashItemFactory;
import dev.jaczerob.delfino.maplestory.server.TimerManager;
import dev.jaczerob.delfino.maplestory.server.life.PlayerNPC;
import dev.jaczerob.delfino.maplestory.server.quest.Quest;
import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;
import dev.jaczerob.delfino.maplestory.tools.Pair;
import dev.jaczerob.delfino.network.packets.Packet;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Component
@DependsOn("channelPacketCreator")
public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    public static long uptime = System.currentTimeMillis();
    private static Server INSTANCE = null;

    private final List<Map<Integer, String>> channels = new LinkedList<>();
    private final List<World> worlds = new ArrayList<>();
    private final Map<Integer, Set<Integer>> accountChars = new HashMap<>();
    private final Map<Integer, Short> accountCharacterCount = new HashMap<>();
    private final Map<Integer, Integer> worldChars = new HashMap<>();
    private final Map<String, Integer> transitioningChars = new HashMap<>();

    private final PlayerBuffStorage buffStorage = new PlayerBuffStorage();
    private final List<Client> processDiseaseAnnouncePlayers = new LinkedList<>();
    private final List<Client> registeredDiseaseAnnouncePlayers = new LinkedList<>();

    private final Lock disLock = new ReentrantLock();

    private final Lock wldRLock;
    private final Lock wldWLock;

    private final Lock lgnRLock;
    private final Lock lgnWLock;

    private final AtomicLong currentTime = new AtomicLong(0);
    private final TimerManager timerManager;
    private final DatabaseConnection databaseConnection;
    private long serverCurrentTime = 0;
    private boolean online = false;

    public Server(
            final TimerManager timerManager,
            final DatabaseConnection databaseConnection
    ) {
        ReadWriteLock worldLock = new ReentrantReadWriteLock(true);
        this.wldRLock = worldLock.readLock();
        this.wldWLock = worldLock.writeLock();

        ReadWriteLock loginLock = new ReentrantReadWriteLock(true);
        this.lgnRLock = loginLock.readLock();
        this.lgnWLock = loginLock.writeLock();

        this.timerManager = timerManager;
        this.databaseConnection = databaseConnection;
    }

    public static Server getInstance() {
        return INSTANCE;
    }

    private static long getTimeLeftForNextHour() {
        Calendar nextHour = Calendar.getInstance();
        nextHour.add(Calendar.HOUR, 1);
        nextHour.set(Calendar.MINUTE, 0);
        nextHour.set(Calendar.SECOND, 0);

        return Math.max(0, nextHour.getTimeInMillis() - System.currentTimeMillis());
    }

    public static void cleanNxcodeCoupons(Connection con) throws SQLException {
        if (!YamlConfig.config.server.USE_CLEAR_OUTDATED_COUPONS) {
            return;
        }

        long timeClear = System.currentTimeMillis() - DAYS.toMillis(14);

        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM nxcode WHERE expiration <= ?")) {
            ps.setLong(1, timeClear);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.isLast()) {
                    try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM nxcode_items WHERE codeid = ?")) {
                        while (rs.next()) {
                            ps2.setInt(1, rs.getInt("id"));
                            ps2.addBatch();
                        }
                        ps2.executeBatch();
                    }

                    try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM nxcode WHERE expiration <= ?")) {
                        ps2.setLong(1, timeClear);
                        ps2.executeUpdate();
                    }
                }
            }
        }
    }

    private static void setAllMerchantsInactive(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET HasMerchant = 0")) {
            ps.executeUpdate();
        }
    }

    private static void applyAllNameChanges(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM namechanges WHERE completionTime IS NULL");
             ResultSet rs = ps.executeQuery()) {
            List<Pair<String, String>> changedNames = new LinkedList<>(); //logging only

            con.setAutoCommit(false);
            try {
                while (rs.next()) {
                    int nameChangeId = rs.getInt("id");
                    int characterId = rs.getInt("characterId");
                    String oldName = rs.getString("old");
                    String newName = rs.getString("new");
                    boolean success = Character.doNameChange(con, characterId, oldName, newName, nameChangeId);
                    if (!success) {
                        con.rollback(); //discard changes
                    } else {
                        con.commit();
                        changedNames.add(new Pair<>(oldName, newName));
                    }
                }
            } finally {
                con.setAutoCommit(true);
            }
            //log
            for (Pair<String, String> namePair : changedNames) {
                log.info("Name change applied - from: \"{}\" to \"{}\"", namePair.getLeft(), namePair.getRight());
            }
        } catch (SQLException e) {
            log.warn("Failed to retrieve list of pending name changes", e);
            throw e;
        }
    }

    private static void applyAllWorldTransfers(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM worldtransfers WHERE completionTime IS NULL",
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = ps.executeQuery()) {
            List<Integer> removedTransfers = new LinkedList<>();
            while (rs.next()) {
                int nameChangeId = rs.getInt("id");
                int characterId = rs.getInt("characterId");
                int oldWorld = rs.getInt("from");
                int newWorld = rs.getInt("to");
                String reason = Character.checkWorldTransferEligibility(con, characterId, oldWorld, newWorld); //check if character is still eligible
                if (reason != null) {
                    removedTransfers.add(nameChangeId);
                    log.info("World transfer canceled: chrId {}, reason {}", characterId, reason);
                    try (PreparedStatement delPs = con.prepareStatement("DELETE FROM worldtransfers WHERE id = ?")) {
                        delPs.setInt(1, nameChangeId);
                        delPs.executeUpdate();
                    } catch (SQLException e) {
                        log.error("Failed to delete world transfer for chrId {}", characterId, e);
                    }
                }
            }
            rs.beforeFirst();
            List<Pair<Integer, Pair<Integer, Integer>>> worldTransfers = new LinkedList<>(); //logging only <charid, <oldWorld, newWorld>>

            con.setAutoCommit(false);
            try {
                while (rs.next()) {
                    int nameChangeId = rs.getInt("id");
                    if (removedTransfers.contains(nameChangeId)) {
                        continue;
                    }
                    int characterId = rs.getInt("characterId");
                    int oldWorld = rs.getInt("from");
                    int newWorld = rs.getInt("to");
                    boolean success = Character.doWorldTransfer(con, characterId, oldWorld, newWorld, nameChangeId);
                    if (!success) {
                        con.rollback();
                    } else {
                        con.commit();
                        worldTransfers.add(new Pair<>(characterId, new Pair<>(oldWorld, newWorld)));
                    }
                }
            } finally {
                con.setAutoCommit(true);
            }

            //log
            for (Pair<Integer, Pair<Integer, Integer>> worldTransferPair : worldTransfers) {
                int charId = worldTransferPair.getLeft();
                int oldWorld = worldTransferPair.getRight().getLeft();
                int newWorld = worldTransferPair.getRight().getRight();
                log.info("World transfer applied - character id {} from world {} to world {}", charId, oldWorld, newWorld);
            }
        } catch (SQLException e) {
            log.warn("Failed to retrieve list of pending world transfers", e);
            throw e;
        }
    }

    private static String getRemoteHost(Client client) {
        return SessionCoordinator.getSessionRemoteHost(client);
    }

    public int getCurrentTimestamp() {
        return (int) (Server.getInstance().getCurrentTime() - Server.uptime);
    }

    public long getCurrentTime() {  // returns a slightly delayed time value, under frequency of UPDATE_INTERVAL
        return serverCurrentTime;
    }

    public void updateCurrentTime() {
        serverCurrentTime = currentTime.addAndGet(YamlConfig.config.server.UPDATE_INTERVAL);
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

    private void loadPlayerNpcMapStepFromDb() {
        final List<World> wlist = this.getWorlds();

        try (Connection con = this.databaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs_field");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int world = rs.getInt("world");
                int map = rs.getInt("map");
                int step = rs.getInt("step");
                int podium = rs.getInt("podium");

                World w = wlist.get(world);
                if (w != null) {
                    w.setPlayerNpcMapData(map, step, podium);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public int getWorldsSize() {
        wldRLock.lock();
        try {
            return worlds.size();
        } finally {
            wldRLock.unlock();
        }
    }

    public Channel getChannel(int world, int channel) {
        try {
            return this.getWorld(world).getChannel(channel);
        } catch (NullPointerException npe) {
            return null;
        }
    }

    public List<Channel> getChannelsFromWorld(int world) {
        try {
            return this.getWorld(world).getChannels();
        } catch (NullPointerException npe) {
            return new ArrayList<>(0);
        }
    }

    public List<Channel> getAllChannels() {
        try {
            List<Channel> channelz = new ArrayList<>();
            for (World world : this.getWorlds()) {
                channelz.addAll(world.getChannels());
            }
            return channelz;
        } catch (NullPointerException npe) {
            return new ArrayList<>(0);
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

    private int initWorld() {
        int i;

        wldRLock.lock();
        try {
            i = worlds.size();

            if (i >= YamlConfig.config.server.WLDLIST_SIZE) {
                return -1;
            }
        } finally {
            wldRLock.unlock();
        }

        log.info("Starting world {}", i);

        int exprate = YamlConfig.config.worlds.get(i).exp_rate;
        int mesorate = YamlConfig.config.worlds.get(i).meso_rate;
        int droprate = YamlConfig.config.worlds.get(i).drop_rate;
        int bossdroprate = YamlConfig.config.worlds.get(i).boss_drop_rate;
        int questrate = YamlConfig.config.worlds.get(i).quest_rate;
        int fishingrate = YamlConfig.config.worlds.get(i).fishing_rate;

        final var world = new World(
                i,
                exprate,
                droprate,
                bossdroprate,
                mesorate,
                questrate,
                fishingrate
        );

        Map<Integer, String> channelInfo = new HashMap<>();
        long bootTime = getCurrentTime();
        Channel channel = new Channel(i, 1, bootTime);

        world.addChannel(channel);
        channelInfo.put(1, channel.getIP());

        boolean canDeploy;

        wldWLock.lock();    // thanks Ashen for noticing a deadlock issue when trying to deploy a channel
        try {
            canDeploy = world.getId() == worlds.size();
            if (canDeploy) {
                worlds.add(world);
                channels.add(i, channelInfo);
            }
        } finally {
            wldWLock.unlock();
        }

        if (canDeploy) {
            world.setServerMessage(YamlConfig.config.worlds.get(i).server_message);

            log.info("Finished loading world {}", i);
            return i;
        } else {
            log.error("Could not load world {}...", i);
            return -2;
        }
    }

    public void runAnnouncePlayerDiseasesSchedule() {
        List<Client> processDiseaseAnnounceClients;
        disLock.lock();
        try {
            processDiseaseAnnounceClients = new LinkedList<>(processDiseaseAnnouncePlayers);
            processDiseaseAnnouncePlayers.clear();
        } finally {
            disLock.unlock();
        }

        while (!processDiseaseAnnounceClients.isEmpty()) {
            Client c = processDiseaseAnnounceClients.remove(0);
            Character player = c.getPlayer();
            if (player != null && player.isLoggedinWorld()) {
                player.announceDiseases();
                player.collectDiseases();
            }
        }

        disLock.lock();
        try {
            // this is to force the system to wait for at least one complete tick before releasing disease info for the registered clients
            while (!registeredDiseaseAnnouncePlayers.isEmpty()) {
                Client c = registeredDiseaseAnnouncePlayers.remove(0);
                processDiseaseAnnouncePlayers.add(c);
            }
        } finally {
            disLock.unlock();
        }
    }

    public void registerAnnouncePlayerDiseases(Client c) {
        disLock.lock();
        try {
            registeredDiseaseAnnouncePlayers.add(c);
        } finally {
            disLock.unlock();
        }
    }

    public void init() {
        Instant beforeInit = Instant.now();
        log.info("Cosmic v{} starting up.", ServerConstants.VERSION);

        final ExecutorService initExecutor = Executors.newFixedThreadPool(10);
        // Run slow operations asynchronously to make startup faster
        final List<Future<?>> futures = new ArrayList<>();
        futures.add(initExecutor.submit(SkillFactory::loadAllSkills));
        futures.add(initExecutor.submit(CashItemFactory::loadAllCashItems));
        futures.add(initExecutor.submit(Quest::loadAllQuests));
        initExecutor.shutdown();

        TimeZone.setDefault(TimeZone.getTimeZone(YamlConfig.config.server.TIMEZONE));

        final int worldCount = Math.min(GameConstants.WORLD_NAMES.length, YamlConfig.config.server.WORLDS);
        try (Connection con = this.databaseConnection.getConnection()) {
            setAllMerchantsInactive(con);
            cleanNxcodeCoupons(con);
            CashIdGenerator.loadExistentCashIdsFromDb(con);
            applyAllNameChanges(con); // -- name changes can be missed by INSTANT_NAME_CHANGE --
            applyAllWorldTransfers(con);
            PlayerNPC.loadRunningRankData(con, worldCount);
        } catch (SQLException sqle) {
            log.error("Failed to run all startup-bound database tasks", sqle);
            throw new IllegalStateException(sqle);
        }

        initializeTimelyTasks();    // aggregated method for timely tasks thanks to lxconan

        try {
            initWorld();
            loadPlayerNpcMapStepFromDb();
        } catch (Exception e) {
            log.error("[SEVERE] Syntax error in 'world.ini'.", e); //For those who get errors
            System.exit(0);
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("Failed to run all startup-bound loading tasks", e);
                throw new IllegalStateException(e);
            }
        }

        online = true;
        Duration initDuration = Duration.between(beforeInit, Instant.now());
        log.info("Cosmic is now online after {} ms.", initDuration.toMillis());

        OpcodeConstants.generateOpcodeNames();
    }

    private void initializeTimelyTasks() {
        this.timerManager.register(this.timerManager::purge, YamlConfig.config.server.PURGING_INTERVAL);
        long timeLeft = getTimeLeftForNextHour();
        this.timerManager.register(new CharacterDiseaseTask(), YamlConfig.config.server.UPDATE_INTERVAL, YamlConfig.config.server.UPDATE_INTERVAL);
        this.timerManager.register(new InvitationTask(), SECONDS.toMillis(30), SECONDS.toMillis(30));
        this.timerManager.register(new RespawnTask(), YamlConfig.config.server.RESPAWN_INTERVAL, YamlConfig.config.server.RESPAWN_INTERVAL);
    }

    @PostConstruct
    public void startServer() {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");

        INSTANCE = this;
        this.init();
    }

    public PlayerBuffStorage getPlayerBuffStorage() {
        return buffStorage;
    }

    public void broadcastMessage(int world, Packet packet) {
        for (Channel ch : getChannelsFromWorld(world)) {
            ch.broadcastPacket(packet);
        }
    }

    public void broadcastGMMessage(int world, Packet packet) {
        for (Channel ch : getChannelsFromWorld(world)) {
            ch.broadcastGMPacket(packet);
        }
    }

    public boolean isGmOnline(int world) {
        for (Channel ch : getChannelsFromWorld(world)) {
            for (Character player : ch.getPlayerStorage().getAllCharacters()) {
                if (player.isGM()) {
                    return true;
                }
            }
        }
        return false;
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
}
