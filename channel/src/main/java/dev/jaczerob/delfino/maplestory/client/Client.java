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
package dev.jaczerob.delfino.maplestory.client;

import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.net.netty.InvalidPacketHeaderException;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.net.server.channel.Channel;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.session.Hwid;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.session.SessionCoordinator;
import dev.jaczerob.delfino.maplestory.net.server.world.MessengerCharacter;
import dev.jaczerob.delfino.maplestory.net.server.world.Party;
import dev.jaczerob.delfino.maplestory.net.server.world.PartyCharacter;
import dev.jaczerob.delfino.maplestory.net.server.world.PartyOperation;
import dev.jaczerob.delfino.maplestory.net.server.world.World;
import dev.jaczerob.delfino.maplestory.packets.ChannelPacketProcessor;
import dev.jaczerob.delfino.maplestory.server.ThreadManager;
import dev.jaczerob.delfino.maplestory.server.TimerManager;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.server.maps.FieldLimit;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.server.maps.MiniDungeonInfo;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;
import dev.jaczerob.delfino.network.packets.InPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import dev.jaczerob.delfino.network.packets.logging.LoggingUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Client extends ChannelInboundHandlerAdapter {
    public static final int LOGIN_NOTLOGGEDIN = 0;
    public static final int LOGIN_SERVER_TRANSITION = 1;
    public static final int LOGIN_LOGGEDIN = 2;
    private static final Logger log = LoggerFactory.getLogger(Client.class);
    private final Type type;
    private final long sessionId;
    private final ChannelPacketProcessor packetProcessor;
    private final Semaphore actionsSemaphore = new Semaphore(7);
    private final Lock lock = new ReentrantLock(true);
    private final Lock announcerLock = new ReentrantLock(true);
    private final byte gender = -1;
    private Hwid hwid;
    private String remoteAddress = "null";
    private volatile boolean inTransition;
    private io.netty.channel.Channel ioChannel;
    private Character player;
    private int channel = 1;
    private int accId = -4;
    private boolean loggedIn = false;
    private boolean serverTransition = false;
    private Calendar birthday = null;
    private String accountName = null;
    private int world;
    private volatile long lastPong;
    private int gmlevel;
    private Map<String, ScriptEngine> engines = new HashMap<>();
    private byte characterSlots = 3;
    private byte csattempt = 0;
    private boolean disconnecting = false;
    private long lastNpcClick;
    private long lastPacket = System.currentTimeMillis();
    private int lang = 0;

    public Client(Type type, long sessionId, ChannelPacketProcessor packetProcessor, int world, int channel) {
        this.type = type;
        this.sessionId = sessionId;
        this.packetProcessor = packetProcessor;
        this.world = world;
        this.channel = channel;
    }

    public static Client createChannelClient(long sessionId, ChannelPacketProcessor packetProcessor,
                                             int world, int channel) {
        return new Client(Type.CHANNEL, sessionId, packetProcessor, world, channel);
    }

    public static Client createMock() {
        return new Client(null, -1, null, -123, -123);
    }

    private static String getRemoteAddress(io.netty.channel.Channel channel) {
        String remoteAddress = "null";
        try {
            remoteAddress = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        } catch (NullPointerException npe) {
            log.warn("Unable to get remote address for client", npe);
        }

        return remoteAddress;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final io.netty.channel.Channel channel = ctx.channel();
        if (!Server.getInstance().isOnline()) {
            channel.close();
            return;
        }

        this.remoteAddress = getRemoteAddress(channel);
        this.ioChannel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof InPacket packet)) {
            log.warn("Received invalid message: {}", msg);
            return;
        }

        short opcode = packet.readShort();
        final var handler = packetProcessor.getHandler(opcode);

        if (YamlConfig.config.server.USE_DEBUG_SHOW_RCVD_PACKET && !LoggingUtil.isIgnoredRecvPacket(opcode)) {
            log.debug("Received packet id {}", opcode);
        }

        if (handler != null && handler.validateState(this)) {
            try {
                handler.handlePacket(packet, this, ctx);
            } catch (final Throwable t) {
                final String chrInfo = player != null ? player.getName() + " on map " + player.getMapId() : "?";
                log.warn("Error in packet handler {}. Chr {}, account {}. Packet: {}", handler.getClass().getSimpleName(),
                        chrInfo, getAccountName(), packet, t);
                //client.sendPacket(PacketCreator.enableActions());//bugs sometimes
            }
        }

        updateLastPacket();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof IdleStateEvent idleEvent) {
            checkIfIdle(idleEvent);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (player != null) {
            log.warn("Exception caught by {}", player, cause);
        }

        if (cause instanceof InvalidPacketHeaderException) {
            SessionCoordinator.getInstance().closeSession(this, true);
        } else if (cause instanceof IOException) {
            closeMapleSession();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        closeMapleSession();
    }

    private void closeMapleSession() {
        switch (type) {
            case LOGIN -> SessionCoordinator.getInstance().closeLoginSession(this);
            case CHANNEL -> SessionCoordinator.getInstance().closeSession(this, null);
        }

        try {
            // client freeze issues on session transition states found thanks to yolinlin, Omo Oppa, Nozphex
            if (!inTransition) {
                disconnect(false, false);
            }
        } catch (Throwable t) {
            log.warn("Account stuck", t);
        } finally {
            closeSession();
        }
    }

    public void updateLastPacket() {
        lastPacket = System.currentTimeMillis();
    }

    public long getLastPacket() {
        return lastPacket;
    }

    public void closeSession() {
        ioChannel.close();
    }

    public void disconnectSession() {
        ioChannel.disconnect();
    }

    public Hwid getHwid() {
        return hwid;
    }

    public void setHwid(Hwid hwid) {
        this.hwid = hwid;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public Character getPlayer() {
        return player;
    }

    public void setPlayer(Character player) {
        this.player = player;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public Calendar getTempBanCalendar() {
        return null;
    }

    public int getAccID() {
        return accId;
    }

    public void setAccID(int id) {
        this.accId = id;
    }

    public void updateLoginState(int newState) {
        // rules out possibility of multiple account entries
        if (newState == LOGIN_LOGGEDIN) {
            SessionCoordinator.getInstance().updateOnlineClient(this);
        }

        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, lastlogin = ? WHERE id = ?")) {
            // using sql currenttime here could potentially break the login, thanks Arnah for pointing this out

            ps.setInt(1, newState);
            ps.setTimestamp(2, new Timestamp(Server.getInstance().getCurrentTime()));
            ps.setInt(3, getAccID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (newState == LOGIN_NOTLOGGEDIN) {
            loggedIn = false;
            serverTransition = false;
            setAccID(0);
        } else {
            serverTransition = (newState == LOGIN_SERVER_TRANSITION);
            loggedIn = !serverTransition;
        }
    }

    public int getLoginState() {  // 0 = LOGIN_NOTLOGGEDIN, 1= LOGIN_SERVER_TRANSITION, 2 = LOGIN_LOGGEDIN
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            int state;
            try (PreparedStatement ps = con.prepareStatement("SELECT loggedin, lastlogin, birthday FROM accounts WHERE id = ?")) {
                ps.setInt(1, getAccID());

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("getLoginState - Client AccID: " + getAccID());
                    }

                    birthday = Calendar.getInstance();
                    try {
                        birthday.setTime(rs.getDate("birthday"));
                    } catch (SQLException e) {
                    }

                    state = rs.getInt("loggedin");
                    if (state == LOGIN_SERVER_TRANSITION) {
                        if (rs.getTimestamp("lastlogin").getTime() + 30000 < Server.getInstance().getCurrentTime()) {
                            int accountId = accId;
                            state = LOGIN_NOTLOGGEDIN;
                            updateLoginState(Client.LOGIN_NOTLOGGEDIN);   // ACCID = 0, issue found thanks to Tochi & K u ssss o & Thora & Omo Oppa
                            this.setAccID(accountId);
                        }
                    }
                }
            }
            if (state == LOGIN_LOGGEDIN) {
                loggedIn = true;
            } else if (state == LOGIN_SERVER_TRANSITION) {
                try (PreparedStatement ps2 = con.prepareStatement("UPDATE accounts SET loggedin = 0 WHERE id = ?")) {
                    ps2.setInt(1, getAccID());
                    ps2.executeUpdate();
                }
            } else {
                loggedIn = false;
            }
            return state;
        } catch (SQLException e) {
            loggedIn = false;
            e.printStackTrace();
            throw new RuntimeException("login state");
        }
    }

    public boolean checkBirthDate(Calendar date) {
        return date.get(Calendar.YEAR) == birthday.get(Calendar.YEAR) && date.get(Calendar.MONTH) == birthday.get(Calendar.MONTH) && date.get(Calendar.DAY_OF_MONTH) == birthday.get(Calendar.DAY_OF_MONTH);
    }

    private void removePartyPlayer(World wserv) {
        MapleMap map = player.getMap();
        final Party party = player.getParty();
        final int idz = player.getId();

        if (party != null) {
            final PartyCharacter chrp = new PartyCharacter(player);
            chrp.setOnline(false);
            wserv.updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
            if (party.getLeader().getId() == idz && map != null) {
                PartyCharacter lchr = null;
                for (PartyCharacter pchr : party.getMembers()) {
                    if (pchr != null && pchr.getId() != idz && (lchr == null || lchr.getLevel() <= pchr.getLevel()) && map.getCharacterById(pchr.getId()) != null) {
                        lchr = pchr;
                    }
                }
                if (lchr != null) {
                    wserv.updateParty(party.getId(), PartyOperation.CHANGE_LEADER, lchr);
                }
            }
        }
    }

    private void removePlayer(World wserv, boolean serverTransition) {
        try {
            player.setDisconnectedFromChannelWorld();
            player.notifyMapTransferToPartner(-1);
            player.removeIncomingInvites();
            player.cancelAllBuffs(true);

            player.closePlayerInteractions();
            player.closePartySearchInteractions();

            if (!serverTransition) {    // thanks MedicOP for detecting an issue with party leader change on changing channels
                removePartyPlayer(wserv);
            }

            if (player.getMap() != null) {
                player.getMap().removePlayer(player);

                if (player.getMap().getHPDec() > 0) {
                    getWorldServer().removePlayerHpDecrease(player);
                }
            }

        } catch (final Throwable t) {
            log.error("Account stuck", t);
        }
    }

    public final void disconnect(final boolean shutdown, final boolean cashshop) {
        if (canDisconnect()) {
            ThreadManager.getInstance().newTask(() -> disconnectInternal(shutdown, cashshop));
        }
    }

    public final void forceDisconnect() {
        if (canDisconnect()) {
            disconnectInternal(true, false);
        }
    }

    private synchronized boolean canDisconnect() {
        if (disconnecting) {
            return false;
        }

        disconnecting = true;
        return true;
    }

    private void disconnectInternal(boolean shutdown, boolean cashshop) {//once per Client instance
        if (player != null && player.isLoggedin() && player.getClient() != null) {
            final int messengerid = player.getMessenger() == null ? 0 : player.getMessenger().getId();
            //final int fid = player.getFamilyId();
            final BuddyList bl = player.getBuddylist();
            final MessengerCharacter chrm = new MessengerCharacter(player, 0);

            final World wserv = getWorldServer();   // obviously wserv is NOT null if this player was online on it
            try {
                removePlayer(wserv, this.serverTransition);

                if (!(channel == -1 || shutdown)) {
                    if (!cashshop) {
                        if (!this.serverTransition) { // meaning not changing channels
                            if (messengerid > 0) {
                                wserv.leaveMessenger(messengerid, chrm);
                            }
                                                        /*      
                                                        if (fid > 0) {
                                                                final Family family = worlda.getFamily(fid);
                                                                family.
                                                        }
                                                        */

                            player.forfeitExpirableQuests();    //This is for those quests that you have to stay logged in for a certain amount of time

                            if (bl != null) {
                                wserv.loggedOff(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
                            }
                        }
                    } else {
                        if (!this.serverTransition) { // if dc inside of cash shop.
                            if (bl != null) {
                                wserv.loggedOff(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                log.error("Account stuck", e);
            } finally {
                if (!this.serverTransition) {
                    wserv.removePlayer(player);

                    player.saveCooldowns();
                    player.cancelAllDebuffs();
                    player.saveCharToDB(true);

                    player.logOff();
                    if (YamlConfig.config.server.INSTANT_NAME_CHANGE) {
                        player.doPendingNameChange();
                    }
                } else {
                    getChannelServer().removePlayer(player);

                    player.saveCooldowns();
                    player.cancelAllDebuffs();
                    player.saveCharToDB();
                }
            }
        }

        SessionCoordinator.getInstance().closeSession(this, false);

        if (!serverTransition && isLoggedIn()) {
            updateLoginState(Client.LOGIN_NOTLOGGEDIN);
        } else {
            if (!Server.getInstance().hasCharacteridInTransition(this)) {
                updateLoginState(Client.LOGIN_NOTLOGGEDIN);
            }

            engines = null; // thanks Tochi for pointing out a NPE here
        }
    }

    public void setCharacterOnSessionTransitionState(int cid) {
        this.updateLoginState(Client.LOGIN_SERVER_TRANSITION);
        this.inTransition = true;
        Server.getInstance().setCharacteridInTransition(this, cid);
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public Channel getChannelServer() {
        return Server.getInstance().getChannel(world, channel);
    }

    public World getWorldServer() {
        return Server.getInstance().getWorld(world);
    }

    public Channel getChannelServer(byte channel) {
        return Server.getInstance().getChannel(world, channel);
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String a) {
        this.accountName = a;
    }

    public int getWorld() {
        return world;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void pongReceived() {
        lastPong = System.currentTimeMillis();
    }

    public void checkIfIdle(final IdleStateEvent event) {
        final long pingedAt = System.currentTimeMillis();
        sendPacket(ChannelPacketCreator.getInstance().getPing());
        TimerManager.getInstance().schedule(() -> {
            try {
                if (lastPong < pingedAt) {
                    if (ioChannel.isActive()) {
                        log.info("Disconnected {} due to idling. Reason: {}", remoteAddress, event.state());
                        updateLoginState(Client.LOGIN_NOTLOGGEDIN);
                        disconnectSession();
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }, SECONDS.toMillis(15));
    }

    public int getGMLevel() {
        return gmlevel;
    }

    public void setGMLevel(int level) {
        gmlevel = level;
    }

    public void lockClient() {
        lock.lock();
    }

    public void unlockClient() {
        lock.unlock();
    }

    public boolean tryacquireClient() {
        if (actionsSemaphore.tryAcquire()) {
            lockClient();
            return true;
        } else {
            return false;
        }
    }

    public void releaseClient() {
        unlockClient();
        actionsSemaphore.release();
    }

    public short getAvailableCharacterWorldSlots(int world) {
        return (short) Math.max(0, characterSlots - Server.getInstance().getAccountWorldCharacterCount(accId, world));
    }

    public short getCharacterSlots() {
        return characterSlots;
    }

    public void setCharacterSlots(byte slots) {
        characterSlots = slots;
    }

    public boolean canGainCharacterSlot() {
        return characterSlots < 15;
    }

    public synchronized boolean gainCharacterSlot() {
        if (canGainCharacterSlot()) {
            try (Connection con = DatabaseConnection.getStaticConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE accounts SET characterslots = ? WHERE id = ?")) {
                ps.setInt(1, this.characterSlots += 1);
                ps.setInt(2, accId);
                ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public byte getGender() {
        return gender;
    }

    private void announceDisableServerMessage() {
        if (!this.getWorldServer().registerDisabledServerMessage(player.getId())) {
            sendPacket(ChannelPacketCreator.getInstance().serverMessage(""));
        }
    }

    public void announceServerMessage() {
        sendPacket(ChannelPacketCreator.getInstance().serverMessage(this.getChannelServer().getServerMessage()));
    }

    public synchronized void announceBossHpBar(Monster mm, final int mobHash, Packet packet) {
        long timeNow = System.currentTimeMillis();
        int targetHash = player.getTargetHpBarHash();

        if (mobHash != targetHash) {
            if (timeNow - player.getTargetHpBarTime() >= SECONDS.toMillis(5)) {
                // is there a way to INTERRUPT this annoying thread running on the client that drops the boss bar after some time at every attack?
                announceDisableServerMessage();
                sendPacket(packet);

                player.setTargetHpBarHash(mobHash);
                player.setTargetHpBarTime(timeNow);
            }
        } else {
            announceDisableServerMessage();
            sendPacket(packet);

            player.setTargetHpBarTime(timeNow);
        }
    }

    public void sendPacket(Packet packet) {
        announcerLock.lock();
        try {
            ioChannel.writeAndFlush(packet);
        } finally {
            announcerLock.unlock();
        }
    }

    public void announceHint(String msg, int length) {
        sendPacket(ChannelPacketCreator.getInstance().sendHint(msg, length, 10));
        sendPacket(ChannelPacketCreator.getInstance().enableActions());
    }

    public void changeChannel(int channel) {
        Server server = Server.getInstance();
        if (player.isBanned()) {
            disconnect(false, false);
            return;
        }
        if (!player.isAlive() || FieldLimit.CANNOTMIGRATE.check(player.getMap().getFieldLimit())) {
            sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        } else if (MiniDungeonInfo.isDungeonMap(player.getMapId())) {
            sendPacket(ChannelPacketCreator.getInstance().serverNotice(5, "Changing channels or entering Cash Shop or MTS are disabled when inside a Mini-Dungeon."));
            sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        String[] socket = Server.getInstance().getInetSocket(this, getWorld(), channel);
        if (socket == null) {
            sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "Channel " + channel + " is currently disabled. Try another channel."));
            sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        player.closePlayerInteractions();
        player.closePartySearchInteractions();

        player.unregisterChairBuff();
        server.getPlayerBuffStorage().addBuffsToStorage(player.getId(), player.getAllBuffs());
        server.getPlayerBuffStorage().addDiseasesToStorage(player.getId(), player.getAllDiseases());
        player.setDisconnectedFromChannelWorld();
        player.notifyMapTransferToPartner(-1);
        player.removeIncomingInvites();
        player.cancelAllBuffs(true);
        player.cancelAllDebuffs();
        player.cancelBuffExpireTask();
        player.cancelDiseaseExpireTask();
        player.cancelSkillCooldownTask();
        player.cancelQuestExpirationTask();
        //Cancelling magicdoor? Nope
        //Cancelling mounts? Noty

        player.getInventory(InventoryType.EQUIPPED).checked(false); //test
        player.getMap().removePlayer(player);
        player.getClient().getChannelServer().removePlayer(player);

        player.saveCharToDB();

        player.setSessionTransitionState();
        try {
            sendPacket(ChannelPacketCreator.getInstance().getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public boolean canClickNPC() {
        return lastNpcClick + 500 < Server.getInstance().getCurrentTime();
    }

    public void setClickedNPC() {
        lastNpcClick = Server.getInstance().getCurrentTime();
    }

    public void removeClickedNPC() {
        lastNpcClick = 0;
    }

    public void closePlayerScriptInteractions() {
        this.removeClickedNPC();
        NPCScriptManager.getInstance().dispose(this);
        QuestScriptManager.getInstance().dispose(this);
    }

    public boolean attemptCsCoupon() {
        if (csattempt > 2) {
            resetCsCoupon();
            return false;
        }

        csattempt++;
        return true;
    }

    public void resetCsCoupon() {
        csattempt = 0;
    }

    public void enableCSActions() {
        sendPacket(ChannelPacketCreator.getInstance().enableCSUse(player));
    }

    public int getLanguage() {
        return lang;
    }

    public void setLanguage(int lingua) {
        this.lang = lingua;
    }

    public enum Type {
        LOGIN,
        CHANNEL
    }
}
