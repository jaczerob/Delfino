package dev.jaczerob.delfino.login.server.handlers.login;

import dev.jaczerob.delfino.login.AbstractPacketHandler;
import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.packet.InPacket;
import dev.jaczerob.delfino.login.tools.PacketCreator;

/**
 * @author kevintjuh93
 */
public final class AcceptToSHandler extends AbstractPacketHandler {

    @Override
    public boolean validateState(@org.jetbrains.annotations.UnknownNullability LoginClient c) {
        return !c.isLoggedIn();
    }

    @Override
    public final void handlePacket(InPacket p, LoginClient c) {
        if (p.available() == 0 || p.readByte() != 1 || c.acceptToS()) {
            c.disconnect(false, false);//Client dc's but just because I am cool I do this (:
            return;
        }
        if (c.finishLogin() == 0) {
            c.sendPacket(PacketCreator.getAuthSuccess(c));
        } else {
            c.sendPacket(PacketCreator.getLoginFailed(9));//shouldn't happen XD
        }
    }
}
