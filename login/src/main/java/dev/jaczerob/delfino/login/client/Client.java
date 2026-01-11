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
package dev.jaczerob.delfino.login.client;

import dev.jaczerob.delfino.login.net.PacketHandler;
import dev.jaczerob.delfino.login.net.PacketProcessor;
import dev.jaczerob.delfino.login.net.netty.InvalidPacketHeaderException;
import dev.jaczerob.delfino.login.net.packet.InPacket;
import dev.jaczerob.delfino.login.net.packet.Packet;
import dev.jaczerob.delfino.login.net.packet.logging.MonitoredChrLogger;
import dev.jaczerob.delfino.login.net.server.Server;
import dev.jaczerob.delfino.login.net.server.coordinator.session.Hwid;
import dev.jaczerob.delfino.login.net.server.coordinator.session.SessionCoordinator;
import dev.jaczerob.delfino.login.server.ThreadManager;
import dev.jaczerob.delfino.login.tools.BCrypt;
import dev.jaczerob.delfino.login.tools.DatabaseConnection;
import dev.jaczerob.delfino.login.tools.PacketCreator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    public static final int LOGIN_NOTLOGGEDIN = 0;
    public static final int LOGIN_SERVER_TRANSITION = 1;
    public static final int LOGIN_LOGGEDIN = 2;

    private final Type type;
    private final long sessionId;
    private final PacketProcessor packetProcessor;

    private Hwid hwid;
    private String remoteAddress;
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
    private int gmlevel;
    private byte characterSlots = 3;
    private byte loginattempt = 0;
    private String pin = "";
    private int pinattempt = 0;
    private String pic = "";
    private int picattempt = 0;
    private byte gender = -1;
    private boolean disconnecting = false;
    private final Lock encoderLock = new ReentrantLock(true);
    private final Lock announcerLock = new ReentrantLock(true);

    public enum Type {
        LOGIN,
        CHANNEL
    }

    public Client(Type type, long sessionId, String remoteAddress, PacketProcessor packetProcessor, int world, int channel) {
        this.type = type;
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
        this.packetProcessor = packetProcessor;
        this.world = world;
        this.channel = channel;
    }

    public static Client createLoginClient(
            final long sessionId,
            final String remoteAddress,
            final PacketProcessor packetProcessor,
            final int world,
            final int channel
    ) {
        return new Client(Type.LOGIN, sessionId, remoteAddress, packetProcessor, world, channel);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final var channel = ctx.channel();
        if (!Server.getInstance().isOnline()) {
            channel.close();
            return;
        }

        this.remoteAddress = getRemoteAddress(channel);
        this.ioChannel = channel;
        log.info("Client active: {}", this.remoteAddress);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("Channel read called for client {}", this.remoteAddress);

        if (!(msg instanceof InPacket packet)) {
            log.warn("Received invalid message: {}", msg);
            return;
        }

        short opcode = packet.readShort();
        log.info("Packet received from {}: Opcode 0x{}", this.remoteAddress, opcode);
        final PacketHandler handler = packetProcessor.getHandler(opcode);

        if (handler == null || !handler.validateState(this)) {
            log.warn("No handler found or invalid state for opcode 0x{} from client {}", opcode, this.remoteAddress);
            return;
        }

        try {
            log.debug("Handling packet {} from client {}", handler.getClass().getSimpleName(), this.remoteAddress);
            MonitoredChrLogger.logPacketIfMonitored(this, opcode, packet.getBytes());
            handler.handlePacket(packet, this);
        } catch (final Exception exc) {
            log.error("Error handling packet {} from client {}", handler.getClass().getSimpleName(), this.remoteAddress, exc);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        log.info("User event triggered for client {}: {}", this.remoteAddress, event);
        if (event instanceof IdleStateEvent idleEvent) {
            checkIfIdle(idleEvent);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (player != null) {
            log.warn("Exception caught by {}", player, cause);
        } else {
            log.warn("Exception caught by client {}", this.remoteAddress, cause);
        }

        if (cause instanceof InvalidPacketHeaderException) {
            SessionCoordinator.getInstance().closeSession(this, true);
        } else if (cause instanceof IOException) {
            closeMapleSession();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("Client inactive: {}", this.remoteAddress);
        closeMapleSession();
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

    private void closeMapleSession() {
        switch (type) {
            case LOGIN -> SessionCoordinator.getInstance().closeLoginSession(this);
            case CHANNEL -> SessionCoordinator.getInstance().closeSession(this, null);
        }

        try {
            // client freeze issues on session transition states found thanks to yolinlin, Omo Oppa, Nozphex
            if (!inTransition) {
                disconnect();
            }
        } catch (Throwable t) {
            log.warn("Account stuck", t);
        } finally {
            closeSession();
        }
    }

    public void closeSession() {
        ioChannel.close();
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

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public int finishLogin() {
        encoderLock.lock();
        try {
            if (getLoginState() > LOGIN_NOTLOGGEDIN) { // 0 = LOGIN_NOTLOGGEDIN, 1= LOGIN_SERVER_TRANSITION, 2 = LOGIN_LOGGEDIN
                loggedIn = false;
                return 7;
            }
            updateLoginState(Client.LOGIN_LOGGEDIN);
        } finally {
            encoderLock.unlock();
        }

        return 0;
    }

    public void setPin(String pin) {
        this.pin = pin;
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET pin = ? WHERE id = ?")) {
            ps.setString(1, pin);
            ps.setInt(2, accId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPin() {
        return pin;
    }

    public boolean checkPin(String other) {
        return true;
    }

    public void setPic(String pic) {
        this.pic = pic;
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET pic = ? WHERE id = ?")) {
            ps.setString(1, pic);
            ps.setInt(2, accId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPic() {
        return pic;
    }

    public int login(String login, String pwd, Hwid hwid) {
        int loginok = 5;

        loginattempt++;
        if (loginattempt > 4) {
            loggedIn = false;
            SessionCoordinator.getInstance().closeSession(this, false);
            return 6;   // thanks Survival_Project for finding out an issue with AUTOMATIC_REGISTER here
        }

        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, password, gender, banned, pin, pic, characterslots, tos FROM accounts WHERE name = ?")) {
            ps.setString(1, login);

            try (ResultSet rs = ps.executeQuery()) {
                accId = -2;
                if (rs.next()) {
                    accId = rs.getInt("id");
                    if (accId <= 0) {
                        log.warn("Tried to log in with accId {}", accId);
                        return 15;
                    }

                    boolean banned = (rs.getByte("banned") == 1);
                    gmlevel = 0;
                    pin = rs.getString("pin");
                    pic = rs.getString("pic");
                    gender = rs.getByte("gender");
                    characterSlots = rs.getByte("characterslots");
                    String passhash = rs.getString("password");
                    byte tos = rs.getByte("tos");

                    if (banned) {
                        return 3;
                    }

                    if (getLoginState() > LOGIN_NOTLOGGEDIN) { // already loggedin
                        loggedIn = false;
                        loginok = 7;
                    } else if (passhash.charAt(0) == '$' && passhash.charAt(1) == '2' && BCrypt.checkpw(pwd, passhash)) {
                        loginok = tos == 0 ? 23 : 0;
                    } else {
                        loggedIn = false;
                        loginok = 4;
                    }
                } else {
                    accId = -3;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return loginok;
    }

    public void setAccID(int id) {
        this.accId = id;
    }

    public int getAccID() {
        return accId;
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

    public int getLoginState() {
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

    public final void disconnect() {
        if (canDisconnect()) {
            ThreadManager.getInstance().newTask(this::disconnectInternal);
        }
    }

    public final void forceDisconnect() {
        if (canDisconnect()) {
            disconnectInternal();
        }
    }

    private synchronized boolean canDisconnect() {
        if (disconnecting) {
            return false;
        }

        disconnecting = true;
        return true;
    }

    private void disconnectInternal() {
        SessionCoordinator.getInstance().closeSession(this, false);

        if (!serverTransition && isLoggedIn()) {
            updateLoginState(Client.LOGIN_NOTLOGGEDIN);

            clear();
        } else {
            if (!Server.getInstance().hasCharacteridInTransition(this)) {
                updateLoginState(Client.LOGIN_NOTLOGGEDIN);
            }
        }
    }

    private void clear() {
        this.accountName = null;
        this.hwid = null;
        this.birthday = null;
        this.player = null;
    }

    public int getChannel() {
        return channel;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String a) {
        this.accountName = a;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getWorld() {
        return world;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void checkIfIdle(final IdleStateEvent event) {
        sendPacket(PacketCreator.getPing());
    }

    public int getGMLevel() {
        return gmlevel;
    }

    public void setGMLevel(int level) {
        gmlevel = level;
    }

    public boolean acceptToS() {
        if (accountName == null) {
            return true;
        }

        boolean disconnect = false;
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT `tos` FROM accounts WHERE id = ?")) {
                ps.setInt(1, accId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getByte("tos") == 1) {
                            disconnect = true;
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET tos = 1 WHERE id = ?")) {
                ps.setInt(1, accId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return disconnect;
    }

    public short getAvailableCharacterSlots() {
        return (short) Math.max(0, this.characterSlots);
    }

    public short getCharacterSlots() {
        return this.characterSlots;
    }

    public byte getGender() {
        return gender;
    }

    public void setGender(byte m) {
        this.gender = m;

        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET gender = ? WHERE id = ?")) {
            ps.setByte(1, gender);
            ps.setInt(2, accId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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

    public long getSessionId() {
        return this.sessionId;
    }
}
