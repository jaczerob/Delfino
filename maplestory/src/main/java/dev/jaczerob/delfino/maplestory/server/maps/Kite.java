package dev.jaczerob.delfino.maplestory.server.maps;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.network.packets.Packet;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;

import java.awt.*;

public class Kite extends AbstractMapObject {
    private final Point pos;
    private final Character owner;
    private final String text;
    private final int ft;
    private final int itemid;

    public Kite(Character owner, String text, int itemId) {
        this.owner = owner;
        this.pos = owner.getPosition();
        this.ft = owner.getFh();
        this.text = text;
        this.itemid = itemId;
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.KITE;
    }

    @Override
    public Point getPosition() {
        return pos.getLocation();
    }

    public Character getOwner() {
        return owner;
    }

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendDestroyData(Client client) {
        client.sendPacket(makeDestroyData());
    }

    @Override
    public void sendSpawnData(Client client) {
        client.sendPacket(makeSpawnData());
    }

    public final Packet makeSpawnData() {
        return ChannelPacketCreator.getInstance().spawnKite(getObjectId(), itemid, owner.getName(), text, pos, ft);
    }

    public final Packet makeDestroyData() {
        return ChannelPacketCreator.getInstance().removeKite(getObjectId(), 0);
    }
}