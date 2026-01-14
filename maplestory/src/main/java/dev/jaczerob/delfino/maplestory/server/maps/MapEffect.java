package dev.jaczerob.delfino.maplestory.server.maps;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.network.packets.Packet;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;

public class MapEffect {
    private final String msg;
    private final int itemId;
    private final boolean active = true;

    public MapEffect(String msg, int itemId) {
        this.msg = msg;
        this.itemId = itemId;
    }

    public final Packet makeDestroyData() {
        return ChannelPacketCreator.getInstance().removeMapEffect();
    }

    public final Packet makeStartData() {
        return ChannelPacketCreator.getInstance().startMapEffect(msg, itemId, active);
    }

    public void sendStartData(Client client) {
        client.sendPacket(makeStartData());
    }
}
