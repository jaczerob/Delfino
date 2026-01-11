package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.packets.coordinators.session.SessionCoordinator;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import org.springframework.stereotype.Component;

@Component
public class SetGenderHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.SET_GENDER;
    }

    @Override
    public void handlePacket(final InPacket p, final LoginClient c) {
        if (c.getGender() != 10) {
            return;
        }

        final var confirmed = p.readByte();
        if (confirmed == 0x01) {
            c.setGender(p.readByte());
            c.sendPacket(LoginPacketCreator.getInstance().getAuthSuccess(c));
        } else {
            SessionCoordinator.getInstance().closeSession(c, null);
            c.updateLoginState(LoginClient.LOGIN_NOTLOGGEDIN);
        }
    }
}
