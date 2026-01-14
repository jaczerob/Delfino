package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.common.cache.login.LoginStatus;
import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class AcceptToSHandler extends AbstractPacketHandler {
    public AcceptToSHandler(final SessionCoordinator sessionCoordinator, final LoginPacketCreator loginPacketCreator) {
        super(sessionCoordinator, loginPacketCreator);
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.ACCEPT_TOS;
    }

    @Override
    public boolean validateState(final LoginClient client) {
        return this.sessionCoordinator.getLoggedInUserStatus(client) != LoginStatus.LOGGED_IN;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client, final ChannelHandlerContext context) {
        if (packet.available() == 0 || packet.readByte() != 1) {
            client.getIoChannel().close();
            return;
        }

        final var authSuccess = this.loginPacketCreator.getAuthSuccess(client);
        context.writeAndFlush(authSuccess);
    }
}
