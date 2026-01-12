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
public class ViewAllCharSelectedWithPicHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(ViewAllCharSelectedWithPicHandler.class);
    private final LoginServer server;

    public ViewAllCharSelectedWithPicHandler(
            final LoginServer server,
            final SessionCoordinator sessionCoordinator,
            final LoginPacketCreator loginPacketCreator
    ) {
        super(sessionCoordinator, loginPacketCreator);
        this.server = server;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.VIEW_ALL_WITH_PIC;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client, final ChannelHandlerContext context) {
        packet.readString();
        int charId = packet.readInt();
        packet.readInt();

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
            log.error("Failed to resolve server address", exc);
        }
    }
}
