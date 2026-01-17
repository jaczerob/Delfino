package dev.jaczerob.delfino.maplestory.net.server.channel;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.net.server.PlayerStorage;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.net.server.services.BaseService;
import dev.jaczerob.delfino.maplestory.net.server.services.ServicesManager;
import dev.jaczerob.delfino.maplestory.net.server.services.type.ChannelServices;
import dev.jaczerob.delfino.maplestory.net.server.world.World;
import dev.jaczerob.delfino.maplestory.server.maps.MapManager;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.packets.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Channel {
    private static final Logger log = LoggerFactory.getLogger(Channel.class);
    private static final int BASE_PORT = 7575;

    private final int port;
    private final String ip;
    private final int world;
    private final int channel;
    private final Map<Integer, Integer> storedVars = new HashMap<>();
    private final Set<Integer> playersAway = new HashSet<>();
    private final PlayerStorage players = new PlayerStorage();
    private final MapManager mapManager;
    private final boolean finishedShutdown = false;
    private String serverMessage;
    private ServicesManager services;

    public Channel(final int world, final int channel, long startTime) {
        this.world = world;
        this.channel = channel;

        this.mapManager = new MapManager(world, channel);
        this.port = BASE_PORT + (this.channel - 1) + (world * 100);
        this.ip = YamlConfig.config.server.HOST + ":" + port;

        try {
            services = new ServicesManager(ChannelServices.OVERALL);

            log.info("Channel {}: Listening on port {}", getId(), port);
        } catch (Exception e) {
            log.warn("Error during channel initialization", e);
        }
    }

    public synchronized void shutdown() {
    }

    public MapManager getMapFactory() {
        return mapManager;
    }

    public BaseService getServiceAccess(ChannelServices sv) {
        return services.getAccess(sv).getService();
    }

    public int getWorld() {
        return world;
    }

    public World getWorldServer() {
        return Server.getInstance().getWorld(world);
    }

    public void addPlayer(Character chr) {
        players.addPlayer(chr);
        chr.sendPacket(ChannelPacketCreator.getInstance().serverMessage(serverMessage));
    }

    public String getServerMessage() {
        return serverMessage;
    }

    public void setServerMessage(String message) {
        this.serverMessage = message;
        broadcastPacket(ChannelPacketCreator.getInstance().serverMessage(message));
        getWorldServer().resetDisabledServerMessages();
    }

    public PlayerStorage getPlayerStorage() {
        return players;
    }

    public boolean removePlayer(Character chr) {
        return players.removePlayer(chr.getId()) != null;
    }

    public void broadcastPacket(Packet packet) {
        for (Character chr : players.getAllCharacters()) {
            chr.sendPacket(packet);
        }
    }

    public final int getId() {
        return channel;
    }

    public String getIP() {
        return ip;
    }

    public void broadcastGMPacket(Packet packet) {
        for (Character chr : players.getAllCharacters()) {
            if (chr.isGM()) {
                chr.sendPacket(packet);
            }
        }
    }

    public void insertPlayerAway(int chrId) {   // either they in CS or MTS
        playersAway.add(chrId);
    }

    public void removePlayerAway(int chrId) {
        playersAway.remove(chrId);
    }

    public int[] multiBuddyFind(int charIdFrom, int[] characterIds) {
        List<Integer> ret = new ArrayList<>(characterIds.length);
        PlayerStorage playerStorage = getPlayerStorage();
        for (int characterId : characterIds) {
            Character chr = playerStorage.getCharacterById(characterId);
            if (chr != null) {
                if (chr.getBuddylist().containsVisible(charIdFrom)) {
                    ret.add(characterId);
                }
            }
        }
        int[] retArr = new int[ret.size()];
        int pos = 0;
        for (Integer i : ret) {
            retArr[pos++] = i;
        }
        return retArr;
    }

    public boolean finishedShutdown() {
        return finishedShutdown;
    }

    public void dropMessage(int type, String message) {
        for (Character player : getPlayerStorage().getAllCharacters()) {
            player.dropMessage(type, message);
        }
    }
}
