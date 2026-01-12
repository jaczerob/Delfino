package dev.jaczerob.delfino.network.packets;

import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public interface PacketHandler<T extends ChannelInboundHandlerAdapter> {
    RecvOpcode getOpcode();

    void handlePacket(final InPacket packet, final T client, final ChannelHandlerContext context);

    boolean validateState(final T client);
}
