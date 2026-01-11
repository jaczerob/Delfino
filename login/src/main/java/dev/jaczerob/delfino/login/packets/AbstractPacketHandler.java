package dev.jaczerob.delfino.login.packets;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.network.packets.PacketHandler;

public abstract class AbstractPacketHandler implements PacketHandler<LoginClient> {
    @Override
    public boolean validateState(final LoginClient c) {
        return c.isLoggedIn();
    }
}