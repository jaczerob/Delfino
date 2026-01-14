package dev.jaczerob.delfino.network.packets;

import dev.jaczerob.delfino.network.opcodes.SendOpcode;

import java.awt.*;

public interface OutPacket extends Packet {
    OutPacket writeByte(byte value);

    OutPacket writeByte(int value);

    OutPacket writeBytes(byte[] value);

    OutPacket writeShort(int value);

    OutPacket writeInt(int value);

    OutPacket writeLong(long value);

    OutPacket writeBool(boolean value);

    OutPacket writeString(String value);

    OutPacket writeFixedString(String value);

    OutPacket writePos(Point value);

    OutPacket skip(int numberOfBytes);

    static OutPacket create(SendOpcode opcode) {
        return new ByteBufOutPacket(opcode);
    }
}
