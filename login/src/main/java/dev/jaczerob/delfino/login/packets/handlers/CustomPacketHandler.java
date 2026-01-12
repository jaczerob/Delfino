package dev.jaczerob.delfino.login.packets.handlers;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public class CustomPacketHandler extends AbstractPacketHandler {
    public CustomPacketHandler(final SessionCoordinator sessionCoordinator, final LoginPacketCreator loginPacketCreator) {
        super(sessionCoordinator, loginPacketCreator);
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CUSTOM_PACKET;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client, final ChannelHandlerContext context) {
        if (packet.available() > 0 && client.getAccount().getGm() == 4) {
            context.writeAndFlush(this.loginPacketCreator.customPacket(packet.readBytes(packet.available())));
        }
    }

    @Override
    public boolean validateState(final LoginClient client) {
        return true;
    }
}
