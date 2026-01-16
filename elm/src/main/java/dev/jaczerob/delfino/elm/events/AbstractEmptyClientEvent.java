package dev.jaczerob.delfino.elm.events;

import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractEmptyClientEvent extends AbstractClientEvent<Object> {
    public AbstractEmptyClientEvent(InPacket inPacket, Client client, ChannelHandlerContext context) {
        super(inPacket, client, context);
    }

    @Override
    protected Object parsePayload(InPacket inPacket) {
        return new Object();
    }
}
