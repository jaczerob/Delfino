package dev.jaczerob.delfino.login.net.packet.logging;

import dev.jaczerob.delfino.login.net.packet.Packet;

public interface PacketLogger {
    void log(Packet packet);
}
