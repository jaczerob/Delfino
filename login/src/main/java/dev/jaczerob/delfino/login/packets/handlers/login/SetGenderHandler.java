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
    public void handlePacket(final InPacket packet, final LoginClient client) {
        final var confirmed = packet.readByte();
        if (confirmed == 0x01) {
            client.sendPacket(LoginPacketCreator.getInstance().getAuthSuccess(client));
        } else {
            SessionCoordinator.getInstance().closeSession(client, null);
            client.updateLoginState(LoginClient.LOGIN_NOTLOGGEDIN);
        }
    }
}
