package dev.jaczerob.delfino.login.client;

import dev.jaczerob.delfino.login.PacketProcessor;
import dev.jaczerob.delfino.login.packet.InPacket;
import dev.jaczerob.delfino.login.packet.logging.LoggingUtil;
import dev.jaczerob.delfino.login.server.coordinator.session.Hwid;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class LoginClient extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(LoginClient.class);

    private final PacketProcessor packetProcessor;
    private Channel ioChannel;
    private String remoteAddress;
    private Hwid hwid;
    private boolean isLoggedIn = false;
    private String clientIp;
    private long clientSessionId;

    public LoginClient(
            final PacketProcessor packetProcessor,
            final Hwid hwid,
            final String clientIp,
            final long clientSessionId
    ) {
        this.packetProcessor = packetProcessor;
        this.hwid = hwid;
        this.clientIp = clientIp;
        this.clientSessionId = clientSessionId;
    }

    public static LoginClient createLoginClient(
            final PacketProcessor packetProcessor,
            final Hwid hwid,
            final String clientIp,
            final long clientSessionId
    ) {
        return new LoginClient(packetProcessor, hwid, clientIp, clientSessionId);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        final var channel = ctx.channel();
        this.remoteAddress = getRemoteAddress(channel);
        this.ioChannel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof InPacket packet)) {
            log.warn("Received invalid message: {}", msg);
            return;
        }
ß
        final var opcode = packet.readShort();
        final var optionalHandler = packetProcessor.getHandler(opcode);
        if (optionalHandler.isEmpty()) {
            log.warn("No handler found for packet id {}", opcode);
            return;
        }

        final var handler = optionalHandler.get();

        if (!LoggingUtil.isIgnoredRecvPacket(opcode)) {
            log.debug("Received packet id {}", opcode);
        }

        if (!handler.validateState(this)) {
            log.warn("Invalid state for handling packet id {} for client {}", opcode, this);
            return;
        }

        try {
            handler.handlePacket(packet, this);
        } catch (final Throwable exc) {
            log.error("Failed to handle packet id {} for client {}", opcode, this, exc);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        log.error("Exception caught for client {}", this, cause);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        this.ioChannel.close();
    }

    public String getRemoteAddress() {
        return this.remoteAddress;
    }

    private static String getRemoteAddress(final Channel channel) {
        try {
            return ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        } catch (final NullPointerException exc) {
            log.warn("Unable to get remote address for client", exc);
            return "null";
        }
    }

    public boolean isLoggedIn() {
        return this.isLoggedIn;
    }

    public Hwid getHwid() {
        return this.hwid;
    }
}
