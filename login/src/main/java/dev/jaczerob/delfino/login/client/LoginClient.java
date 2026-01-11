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

import dev.jaczerob.delfino.login.packets.PacketProcessor;
import dev.jaczerob.delfino.login.packets.coordinators.session.HWID;
import dev.jaczerob.delfino.login.packets.coordinators.session.SessionCoordinator;
import dev.jaczerob.delfino.login.server.LoginServer;
import dev.jaczerob.delfino.login.tools.BCrypt;
import dev.jaczerob.delfino.login.tools.DatabaseConnection;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.packets.InPacket;
import dev.jaczerob.delfino.network.packets.InvalidPacketHeaderException;
import dev.jaczerob.delfino.network.packets.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
public class LoginClient extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(LoginClient.class);

    public static final int LOGIN_NOTLOGGEDIN = 0;
    public static final int LOGIN_SERVER_TRANSITION = 1;
    public static final int LOGIN_LOGGEDIN = 2;

    private final long sessionId;
    private final PacketProcessor packetProcessor;

    private HWID hwid;
    private String remoteAddress;

    private io.netty.channel.Channel ioChannel;
    private int accId = -4;
    private boolean loggedIn = false;
    private boolean serverTransition = false;
    private String accountName = null;
    private int gmLevel;
    private byte characterSlots = 3;
    private byte gender = -1;
    private boolean disconnecting = false;
    private final Lock encoderLock = new ReentrantLock(true);
    private final Lock announcerLock = new ReentrantLock(true);
    private String pin = "";
    private String pic = "";

    public LoginClient(long sessionId, String remoteAddress, PacketProcessor packetProcessor) {
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
        this.packetProcessor = packetProcessor;
    }

    public static LoginClient createLoginClient(
            final long sessionId,
            final String remoteAddress,
            final PacketProcessor packetProcessor
    ) {
        return new LoginClient(sessionId, remoteAddress, packetProcessor);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final var channel = ctx.channel();
        if (!LoginServer.getInstance().isOnline()) {
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
        final var handler = this.packetProcessor.getHandler(opcode);

        if (handler == null || !handler.validateState(this)) {
            log.warn("No handler found or invalid state for opcode 0x{} from client {}", opcode, this.remoteAddress);
            return;
        }

        try {
            log.debug("Handling packet {} from client {}", handler.getClass().getSimpleName(), this.remoteAddress);
            handler.handlePacket(packet, this);
        } catch (final Exception exc) {
            log.error("Error handling packet {} from client {}", handler.getClass().getSimpleName(), this.remoteAddress, exc);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        log.info("User event triggered for client {}: {}", this.remoteAddress, event);
        if (event instanceof IdleStateEvent idleEvent) {
            checkIfIdle();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Exception caught by client {}", this.remoteAddress, cause);

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
        SessionCoordinator.getInstance().closeLoginSession(this);

        try {
            this.disconnect();
        } catch (final Throwable exc) {
            log.warn("Account stuck", exc);
        } finally {
            this.closeSession();
        }
    }

    public void closeSession() {
        this.ioChannel.close();
    }

    public int finishLogin() {
        encoderLock.lock();
        try {
            if (getLoginState() > LOGIN_NOTLOGGEDIN) { // 0 = LOGIN_NOTLOGGEDIN, 1= LOGIN_SERVER_TRANSITION, 2 = LOGIN_LOGGEDIN
                loggedIn = false;
                return 7;
            }
            updateLoginState(LoginClient.LOGIN_LOGGEDIN);
        } finally {
            encoderLock.unlock();
        }

        return 0;
    }

    public void setPin(final String pin) {
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET pin = ? WHERE id = ?")) {
            ps.setString(1, pin);
            ps.setInt(2, accId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkPin(final String other) {
        return true;
    }

    public int login(String login, String pwd, HWID hwid) {
        int loginok = 5;

        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, password, gender, banned, characterslots, tos FROM accounts WHERE name = ?")) {
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
                    gmLevel = 0;
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

    public void updateLoginState(int newState) {
        if (newState == LOGIN_LOGGEDIN) {
            SessionCoordinator.getInstance().updateOnlineClient(this);
        }

        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, lastlogin = ? WHERE id = ?")) {
            // using sql currenttime here could potentially break the login, thanks Arnah for pointing this out

            ps.setInt(1, newState);
            ps.setTimestamp(2, new Timestamp(LoginServer.getInstance().getCurrentTime()));
            ps.setInt(3, this.getAccId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (newState == LOGIN_NOTLOGGEDIN) {
            loggedIn = false;
            serverTransition = false;
            setAccId(0);
        } else {
            serverTransition = (newState == LOGIN_SERVER_TRANSITION);
            loggedIn = !serverTransition;
        }
    }

    public int getLoginState() {
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            int state;
            try (PreparedStatement ps = con.prepareStatement("SELECT loggedin, lastlogin FROM accounts WHERE id = ?")) {
                ps.setInt(1, getAccId());

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("getLoginState - Client AccID: " + getAccId());
                    }

                    state = rs.getInt("loggedin");
                    if (state == LOGIN_SERVER_TRANSITION) {
                        if (rs.getTimestamp("lastlogin").getTime() + 30000 < LoginServer.getInstance().getCurrentTime()) {
                            int accountId = accId;
                            state = LOGIN_NOTLOGGEDIN;
                            updateLoginState(LoginClient.LOGIN_NOTLOGGEDIN);   // ACCID = 0, issue found thanks to Tochi & K u ssss o & Thora & Omo Oppa
                            this.setAccId(accountId);
                        }
                    }
                }
            }
            if (state == LOGIN_LOGGEDIN) {
                loggedIn = true;
            } else if (state == LOGIN_SERVER_TRANSITION) {
                try (PreparedStatement ps2 = con.prepareStatement("UPDATE accounts SET loggedin = 0 WHERE id = ?")) {
                    ps2.setInt(1, getAccId());
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
        this.disconnectInternal();
    }

    public final void forceDisconnect() {
        if (canDisconnect()) {
            this.disconnectInternal();
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
        this.updateLoginState(LoginClient.LOGIN_NOTLOGGEDIN);
    }

    public void checkIfIdle() {
        sendPacket(LoginPacketCreator.getInstance().getPing());
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

    public short getCharacterSlots() {
        return this.characterSlots;
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
}
