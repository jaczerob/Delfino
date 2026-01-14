package dev.jaczerob.delfino.maplestory.server.life;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.server.ShopFactory;
import dev.jaczerob.delfino.maplestory.server.maps.MapObjectType;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;

public class NPC extends AbstractLoadedLife {
    private final NPCStats stats;

    public NPC(int id, NPCStats stats) {
        super(id);
        this.stats = stats;
    }

    public boolean hasShop() {
        return ShopFactory.getInstance().getShopForNPC(getId()) != null;
    }

    public void sendShop(Client c) {
        ShopFactory.getInstance().getShopForNPC(getId()).sendShop(c);
    }

    @Override
    public void sendSpawnData(Client client) {
        client.sendPacket(ChannelPacketCreator.getInstance().spawnNPC(this));
        client.sendPacket(ChannelPacketCreator.getInstance().spawnNPCRequestController(this, true));
    }

    @Override
    public void sendDestroyData(Client client) {
        client.sendPacket(ChannelPacketCreator.getInstance().removeNPCController(getObjectId()));
        client.sendPacket(ChannelPacketCreator.getInstance().removeNPC(getObjectId()));
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.NPC;
    }

    public String getName() {
        return stats.getName();
    }
}
