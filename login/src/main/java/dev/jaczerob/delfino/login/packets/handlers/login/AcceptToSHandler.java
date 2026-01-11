package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import org.springframework.stereotype.Component;

@Component
public final class AcceptToSHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.ACCEPT_TOS;
    }

    @Override
    public boolean validateState(LoginClient c) {
        return !c.isLoggedIn();
    }

    @Override
    public void handlePacket(final InPacket p, final LoginClient c) {
        if (p.available() == 0 || p.readByte() != 1 || c.acceptToS()) {
            c.disconnect();//Client dc's but just because I am cool I do this (:
            return;
        }
        if (c.finishLogin() == 0) {
            c.sendPacket(LoginPacketCreator.getInstance().getAuthSuccess(c));
        } else {
            c.sendPacket(LoginPacketCreator.getInstance().getLoginFailed(9));//shouldn't happen XD
        }
    }
}
