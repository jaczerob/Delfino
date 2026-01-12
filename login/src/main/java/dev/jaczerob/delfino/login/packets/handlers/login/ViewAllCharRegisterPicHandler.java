package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.server.LoginServer;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
public final class ViewAllCharRegisterPicHandler extends AbstractPacketHandler {
    private final LoginServer server;

    public ViewAllCharRegisterPicHandler(
            final LoginServer server,
            final SessionCoordinator sessionCoordinator,
            final LoginPacketCreator loginPacketCreator
    ) {
        super(sessionCoordinator, loginPacketCreator);
        this.server = server;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.VIEW_ALL_PIC_REGISTER;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client, final ChannelHandlerContext context) {
        final var payload = ViewAllCharRegisterPicPayload.from(packet);

        final var socket = this.server.getInetSocket();
        if (socket == null) {
            context.writeAndFlush(this.loginPacketCreator.getAfterLoginError(10));
            return;
        }

        try {
            context.writeAndFlush(this.loginPacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), payload.charId()));
        } catch (final UnknownHostException exc) {
            log.error("Failed to resolve server address", exc);
        }
    }

    private record ViewAllCharRegisterPicPayload(int charId) {
        public static ViewAllCharRegisterPicPayload from(final InPacket packet) {
            packet.readByte();
            final var charId = packet.readInt();
            packet.readInt();

            packet.readString();
            packet.readString();
            packet.readString();

            return new ViewAllCharRegisterPicPayload(charId);
        }
    }
}
