package dev.jaczerob.delfino.maplestory.packets;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.network.packets.PacketHandler;

public abstract class AbstractPacketHandler implements PacketHandler<Client> {
    @Override
    public boolean validateState(Client c) {
        return c.isLoggedIn();
    }

    protected static long currentServerTime() {
        return Server.getInstance().getCurrentTime();
    }
}