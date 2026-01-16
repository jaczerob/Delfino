package dev.jaczerob.delfino.maplestory.server.movement;

import dev.jaczerob.delfino.network.packets.OutPacket;

import java.awt.*;

public class RelativeLifeMovement extends AbstractLifeMovement {
    public RelativeLifeMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    @Override
    public void serialize(OutPacket p) {
        p.writeByte(getType());
        p.writePos(getPosition());
        p.writeByte(getNewstate());
        p.writeShort(getDuration());
    }
}
