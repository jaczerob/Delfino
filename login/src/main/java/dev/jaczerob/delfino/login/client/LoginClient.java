package dev.jaczerob.delfino.login.client;

import dev.jaczerob.delfino.grpc.proto.account.Account;
import dev.jaczerob.delfino.grpc.proto.character.Character;
import dev.jaczerob.delfino.login.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.login.packets.PacketProcessor;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.packets.InPacket;
import dev.jaczerob.delfino.network.packets.InvalidPacketHeaderException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

@Getter
@Setter
public class LoginClient extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(LoginClient.class);

    private final PacketProcessor packetProcessor;
    private final LoginPacketCreator loginPacketCreator;

    private Account account;
    private Character selectedCharacter;
    private String remoteAddress;
    private Channel ioChannel;

    public LoginClient(
            final String remoteAddress,
            final PacketProcessor packetProcessor,
            final LoginPacketCreator loginPacketCreator
    ) {
        this.remoteAddress = remoteAddress;
        this.packetProcessor = packetProcessor;
        this.loginPacketCreator = loginPacketCreator;
    }

    @Override
    public void channelActive(final ChannelHandlerContext context) {
        final var channel = context.channel();
        this.remoteAddress = this.getRemoteAddress(channel);
        this.ioChannel = channel;
        log.info("Client active: {}", this.remoteAddress);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext context) {
        log.info("Client inactive: {}", this.remoteAddress);
        SessionCoordinator.getInstance().logout(this);
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
            handler.handlePacket(packet, this, context);
        } catch (final Exception exc) {
            log.error("Error handling packet {} from client {}", handler.getClass().getSimpleName(), this.remoteAddress, exc);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object event) {
        log.info("User event triggered for client {}: {}", this.remoteAddress, event);
        if (event instanceof IdleStateEvent) {
            context.writeAndFlush(this.loginPacketCreator.getPing());
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) throws Exception {
        log.warn("Exception caught by client {}", this.remoteAddress, cause);

        if (cause instanceof InvalidPacketHeaderException || cause instanceof IOException) {
            SessionCoordinator.getInstance().logout(this);
        }
    }

    private String getRemoteAddress(final Channel channel) {
        try {
            return ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        } catch (final Exception exc) {
            log.warn("Unable to get remote address for client", exc);
            return "null";
        }
    }
}
