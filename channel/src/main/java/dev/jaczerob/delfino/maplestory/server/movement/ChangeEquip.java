package dev.jaczerob.delfino.maplestory.server.movement;

import dev.jaczerob.delfino.network.packets.OutPacket;

import java.awt.*;

public class ChangeEquip implements LifeMovementFragment {
    private final int wui;

    public ChangeEquip(int wui) {
        this.wui = wui;
    }

    @Override
    public void serialize(OutPacket p) {
        p.writeByte(10);
        p.writeByte(wui);
    }

    @Override
    public Point getPosition() {
        return new Point(0, 0);
    }
}
