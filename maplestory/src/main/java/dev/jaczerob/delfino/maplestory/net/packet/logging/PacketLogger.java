package dev.jaczerob.delfino.network.packets.logging;

import dev.jaczerob.delfino.network.packets.Packet;

public interface PacketLogger {
    void log(Packet packet);
}
