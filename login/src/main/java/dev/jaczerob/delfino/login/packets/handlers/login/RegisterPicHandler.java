package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.server.LoginServer;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public final class RegisterPicHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(RegisterPicHandler.class);

    private final LoginServer server;

    public RegisterPicHandler(
            final LoginServer server,
            final SessionCoordinator sessionCoordinator,
            final LoginPacketCreator loginPacketCreator
    ) {
        super(sessionCoordinator, loginPacketCreator);
        this.server = server;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.REGISTER_PIC;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client, final ChannelHandlerContext context) {
        // TODO: Re-implement registering PIC
        packet.readByte();
        final var charId = packet.readInt();

        packet.readString();
        packet.readString();

        final var socket = this.server.getInetSocket();
        if (socket == null) {
            context.writeAndFlush(this.loginPacketCreator.getAfterLoginError(10));
            return;
        }

        try {
            context.writeAndFlush(this.loginPacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
        } catch (final UnknownHostException exc) {
            log.error("Failed to resolve login server address", exc);
        }
    }
}