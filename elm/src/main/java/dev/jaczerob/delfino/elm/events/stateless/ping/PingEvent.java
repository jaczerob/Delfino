package dev.jaczerob.delfino.elm.events.stateless.ping;

import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.elm.events.AbstractClientEvent;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;

public class PingEvent extends AbstractClientEvent<Object> {
    public PingEvent(InPacket inPacket, Client client, ChannelHandlerContext context) {
        super(inPacket, client, context);
    }

    @Override
    protected Object parsePayload(InPacket inPacket) {
        return new Object();
    }
}
