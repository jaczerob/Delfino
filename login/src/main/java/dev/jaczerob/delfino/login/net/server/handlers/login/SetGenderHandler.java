package dev.jaczerob.delfino.login.net.server.handlers.login;

import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.net.AbstractPacketHandler;
import dev.jaczerob.delfino.login.net.opcodes.RecvOpcode;
import dev.jaczerob.delfino.login.net.packet.InPacket;
import dev.jaczerob.delfino.login.net.server.coordinator.session.SessionCoordinator;
import dev.jaczerob.delfino.login.tools.PacketCreator;
import org.springframework.stereotype.Component;

@Component
public class SetGenderHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.SET_GENDER;
    }

    @Override
    public void handlePacket(final InPacket p, final Client c) {
        if (c.getGender() != 10) {
            return;
        }

        final var confirmed = p.readByte();
        if (confirmed == 0x01) {
            c.setGender(p.readByte());
            c.sendPacket(PacketCreator.getAuthSuccess(c));
        } else {
            SessionCoordinator.getInstance().closeSession(c, null);
            c.updateLoginState(Client.LOGIN_NOTLOGGEDIN);
        }
    }

}
