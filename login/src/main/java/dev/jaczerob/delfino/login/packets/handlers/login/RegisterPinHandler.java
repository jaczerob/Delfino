package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class RegisterPinHandler extends AbstractPacketHandler {
    public RegisterPinHandler(SessionCoordinator sessionCoordinator, LoginPacketCreator loginPacketCreator) {
        super(sessionCoordinator, loginPacketCreator);
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.REGISTER_PIN;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client, final ChannelHandlerContext context) {
        // TODO: Implement PIN registration logic
        this.sessionCoordinator.logout(client);
    }
}
