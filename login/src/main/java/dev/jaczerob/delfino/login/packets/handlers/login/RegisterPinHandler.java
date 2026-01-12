package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.packets.coordinators.session.SessionCoordinator;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import org.springframework.stereotype.Component;

@Component
public final class RegisterPinHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.REGISTER_PIN;
    }

    @Override
    public void handlePacket(final InPacket p, final LoginClient c) {
        final var c2 = p.readByte();
        if (c2 == 0) {
            SessionCoordinator.getInstance().closeSession(c, null);
            c.updateLoginState(LoginClient.LOGIN_NOTLOGGEDIN);
        } else {
            final var pin = p.readString();
            if (pin != null) {
                // TODO: Update PIN via MDRS
                c.sendPacket(LoginPacketCreator.getInstance().pinRegistered());
                SessionCoordinator.getInstance().closeSession(c, null);
                c.updateLoginState(LoginClient.LOGIN_NOTLOGGEDIN);
            }
        }
    }
}
