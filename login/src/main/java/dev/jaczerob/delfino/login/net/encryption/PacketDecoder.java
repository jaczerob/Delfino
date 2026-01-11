package dev.jaczerob.delfino.login.net.encryption;

import dev.jaczerob.delfino.login.net.netty.InvalidPacketHeaderException;
import dev.jaczerob.delfino.login.net.packet.ByteBufInPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class PacketDecoder extends ReplayingDecoder<Void> {
    private final MapleAESOFB receiveCypher;

    public PacketDecoder(MapleAESOFB receiveCypher) {
        this.receiveCypher = receiveCypher;
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) {
        final var header = in.readInt();

        if (!this.receiveCypher.isValidHeader(header)) {
            throw new InvalidPacketHeaderException("Attempted to decode a packet with an invalid header", header);
        }

        final var packetLength = decodePacketLength(header);
        final var packet = new byte[packetLength];
        in.readBytes(packet);
        
        this.receiveCypher.crypt(packet);
        MapleCustomEncryption.decryptData(packet);
        out.add(new ByteBufInPacket(Unpooled.wrappedBuffer(packet)));
    }

    private static int decodePacketLength(int header) {
        final var length = ((header >>> 16) ^ (header & 0xFFFF));
        return ((length << 8) & 0xFF00) | ((length >>> 8) & 0xFF);
    }
}
