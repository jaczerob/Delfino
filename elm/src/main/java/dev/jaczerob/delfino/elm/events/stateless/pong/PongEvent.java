package dev.jaczerob.delfino.elm.events.stateless.pong;

import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.elm.events.AbstractEmptyClientEvent;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;

public class PongEvent extends AbstractEmptyClientEvent {
    public PongEvent(InPacket inPacket, Client client, ChannelHandlerContext context) {
        super(inPacket, client, context);
    }
}
