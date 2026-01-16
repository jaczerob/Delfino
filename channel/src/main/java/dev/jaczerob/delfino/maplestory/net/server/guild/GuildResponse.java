package dev.jaczerob.delfino.maplestory.net.server.guild;

import dev.jaczerob.delfino.network.packets.Packet;

public enum GuildResponse {
    NOT_IN_CHANNEL(0x2a),
    ALREADY_IN_GUILD(0x28),
    NOT_IN_GUILD(0x2d),
    NOT_FOUND_INVITE(0x2e),
    MANAGING_INVITE(0x36),
    DENIED_INVITE(0x37);

    private final int value;

    GuildResponse(int val) {
        value = val;
    }

    public final Packet getPacket(String targetName) {
        if (value >= MANAGING_INVITE.value) {
            return GuildPackets.responseGuildMessage((byte) value, targetName);
        } else {
            return GuildPackets.genericGuildMessage((byte) value);
        }
    }
}
