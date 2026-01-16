package dev.jaczerob.delfino.elm.events.stateless.hello;

import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.elm.events.AbstractEmptyClientEvent;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;

public class HelloEvent extends AbstractEmptyClientEvent {
    public HelloEvent(InPacket inPacket, Client client, ChannelHandlerContext context) {
        super(inPacket, client, context);
    }
}
