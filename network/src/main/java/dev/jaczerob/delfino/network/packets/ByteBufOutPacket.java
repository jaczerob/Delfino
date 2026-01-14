package dev.jaczerob.delfino.network.packets;

import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.awt.*;

public class ByteBufOutPacket implements OutPacket {
    private final ByteBuf byteBuf;

    public ByteBufOutPacket() {
        this.byteBuf = Unpooled.buffer();
    }

    public ByteBufOutPacket(final SendOpcode op) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeShortLE((short) op.getValue());
        this.byteBuf = byteBuf;
    }

    @Override
    public byte[] getBytes() {
        return ByteBufUtil.getBytes(byteBuf);
    }

    @Override
    public OutPacket writeByte(byte value) {
        byteBuf.writeByte(value);
        return this;
    }

    @Override
    public OutPacket writeByte(int value) {
        writeByte((byte) value);
        return this;
    }

    @Override
    public OutPacket writeBytes(byte[] value) {
        byteBuf.writeBytes(value);
        return this;
    }

    @Override
    public OutPacket writeShort(int value) {
        byteBuf.writeShortLE(value);
        return this;
    }

    @Override
    public OutPacket writeInt(int value) {
        byteBuf.writeIntLE(value);
        return this;
    }

    @Override
    public OutPacket writeLong(long value) {
        byteBuf.writeLongLE(value);
        return this;
    }

    @Override
    public OutPacket writeBool(boolean value) {
        byteBuf.writeByte(value ? 1 : 0);
        return this;
    }

    @Override
    public OutPacket writeString(String value) {
        byte[] bytes = value.getBytes();
        writeShort(bytes.length);
        writeBytes(bytes);
        return this;
    }

    @Override
    public OutPacket writeFixedString(String value) {
        writeBytes(value.getBytes());
        return this;
    }

    @Override
    public OutPacket writePos(Point value) {
        writeShort((short) value.getX());
        writeShort((short) value.getY());
        return this;
    }

    @Override
    public OutPacket skip(int numberOfBytes) {
        writeBytes(new byte[numberOfBytes]);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ByteBufOutPacket other && byteBuf.equals(other.byteBuf);
    }
}
