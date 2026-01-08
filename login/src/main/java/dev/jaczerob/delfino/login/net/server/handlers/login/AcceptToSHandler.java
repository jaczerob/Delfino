package dev.jaczerob.delfino.login.net.server.handlers.login;

import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.net.AbstractPacketHandler;
import dev.jaczerob.delfino.login.net.opcodes.RecvOpcode;
import dev.jaczerob.delfino.login.net.packet.InPacket;
import dev.jaczerob.delfino.login.tools.PacketCreator;
import org.springframework.stereotype.Component;

@Component
public final class AcceptToSHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.ACCEPT_TOS;
    }

    @Override
    public boolean validateState(Client c) {
        return !c.isLoggedIn();
    }

    @Override
    public final void handlePacket(final InPacket p, final Client c) {
        if (p.available() == 0 || p.readByte() != 1 || c.acceptToS()) {
            c.disconnect();//Client dc's but just because I am cool I do this (:
            return;
        }
        if (c.finishLogin() == 0) {
            c.sendPacket(PacketCreator.getAuthSuccess(c));
        } else {
            c.sendPacket(PacketCreator.getLoginFailed(9));//shouldn't happen XD
        }
    }
}
