package dev.jaczerob.delfino.maplestory.server.movement;

import dev.jaczerob.delfino.network.packets.OutPacket;

import java.awt.*;

public interface LifeMovementFragment {
    void serialize(OutPacket p);

    Point getPosition();
}
