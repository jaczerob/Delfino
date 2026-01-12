package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.server.ServerStatus;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class ServerStatusRequestHandler extends AbstractPacketHandler {
    public ServerStatusRequestHandler(
            final SessionCoordinator sessionCoordinator,
            final LoginPacketCreator loginPacketCreator
    ) {
        super(sessionCoordinator, loginPacketCreator);
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.SERVERSTATUS_REQUEST;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client, final ChannelHandlerContext context) {
        final var status = ServerStatus.NORMAL;
        context.writeAndFlush(this.loginPacketCreator.getServerStatus(status.getCode()));
    }
}
