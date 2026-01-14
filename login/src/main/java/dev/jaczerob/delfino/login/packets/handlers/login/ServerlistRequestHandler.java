package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class ServerlistRequestHandler extends AbstractPacketHandler {
    public ServerlistRequestHandler(
            final SessionCoordinator sessionCoordinator,
            final LoginPacketCreator loginPacketCreator
    ) {
        super(sessionCoordinator, loginPacketCreator);
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.SERVERLIST_REQUEST;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client, final ChannelHandlerContext context) {
        context.writeAndFlush(this.loginPacketCreator.getServerList());
        context.writeAndFlush(this.loginPacketCreator.getEndOfServerList());
        context.writeAndFlush(this.loginPacketCreator.selectWorld(0));
        context.writeAndFlush(this.loginPacketCreator.sendRecommended(List.of()));
    }
}