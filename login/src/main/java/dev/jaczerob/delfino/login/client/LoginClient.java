package dev.jaczerob.delfino.login.client;

import dev.jaczerob.delfino.grpc.proto.account.Account;
import dev.jaczerob.delfino.grpc.proto.character.Character;
import dev.jaczerob.delfino.login.packets.PacketProcessor;
import dev.jaczerob.delfino.login.packets.coordinators.session.HWID;
import dev.jaczerob.delfino.login.packets.coordinators.session.SessionCoordinator;
import dev.jaczerob.delfino.login.server.LoginServer;
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

    private Account account;
    private Character selectedCharacter;

    private HWID hwid;
    private String remoteAddress;

    private io.netty.channel.Channel ioChannel;
    private boolean loggedIn = false;
    private boolean serverTransition = false;
    private boolean disconnecting = false;
    private final Lock encoderLock = new ReentrantLock(true);
    private final Lock announcerLock = new ReentrantLock(true);

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
    public void channelActive(final ChannelHandlerContext context) {
        final var channel = context.channel();
        if (!LoginServer.getInstance().isOnline()) {
            channel.close();
            return;
        }

        this.remoteAddress = getRemoteAddress(channel);
        this.ioChannel = channel;
        log.info("Client active: {}", this.remoteAddress);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext context) {
        log.info("Client inactive: {}", this.remoteAddress);
        closeMapleSession();
    }

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object msg) throws Exception {
        log.debug("Channel read called for client {}", this.remoteAddress);

        if (!(msg instanceof InPacket packet)) {
            log.warn("Received invalid message: {}", msg);
            return;
        }

        final var opcode = packet.readShort();
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
    public void userEventTriggered(ChannelHandlerContext context, Object event) {
        log.info("User event triggered for client {}: {}", this.remoteAddress, event);
        if (event instanceof IdleStateEvent) {
            ping();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
        log.warn("Exception caught by client {}", this.remoteAddress, cause);

        if (cause instanceof InvalidPacketHeaderException) {
            SessionCoordinator.getInstance().closeSession(this, true);
        } else if (cause instanceof IOException) {
            closeMapleSession();
        }
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

    public boolean checkPin(final String other) {
        return true;
    }

    public void updateLoginState(int newState) {
        if (newState == LOGIN_LOGGEDIN) {
            SessionCoordinator.getInstance().updateOnlineClient(this);
        }

        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, lastlogin = ? WHERE id = ?")) {

            ps.setInt(1, newState);
            ps.setTimestamp(2, new Timestamp(LoginServer.getInstance().getCurrentTime()));
            ps.setInt(3, this.getAccount().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (newState == LOGIN_NOTLOGGEDIN) {
            this.loggedIn = false;
            this.serverTransition = false;
        } else {
            this.serverTransition = (newState == LOGIN_SERVER_TRANSITION);
            this.loggedIn = !this.serverTransition;
        }
    }

    public int getLoginState() {
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            int state;
            try (PreparedStatement ps = con.prepareStatement("SELECT loggedin, lastlogin FROM accounts WHERE id = ?")) {
                ps.setInt(1, this.account.getId());

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("getLoginState - Client AccID: " + this.getAccount().getId());
                    }

                    state = rs.getInt("loggedin");
                    if (state == LOGIN_SERVER_TRANSITION) {
                        if (rs.getTimestamp("lastlogin").getTime() + 30000 < LoginServer.getInstance().getCurrentTime()) {
                            state = LOGIN_NOTLOGGEDIN;
                            updateLoginState(LoginClient.LOGIN_NOTLOGGEDIN);
                        }
                    }
                }
            }
            if (state == LOGIN_LOGGEDIN) {
                loggedIn = true;
            } else if (state == LOGIN_SERVER_TRANSITION) {
                try (PreparedStatement ps2 = con.prepareStatement("UPDATE accounts SET loggedin = 0 WHERE id = ?")) {
                    ps2.setInt(1, this.getAccount().getId());
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

    public void ping() {
        sendPacket(LoginPacketCreator.getInstance().getPing());
    }

    public boolean acceptToS() {
        return true;
    }

    public void sendPacket(final Packet packet) {
        announcerLock.lock();
        try {
            ioChannel.writeAndFlush(packet);
        } finally {
            announcerLock.unlock();
        }
    }
}
