package dev.jaczerob.delfino.maplestory.net;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.network.packets.InPacket;

public interface PacketHandler {
    void handlePacket(InPacket p, Client c);

    boolean validateState(Client c);
}
