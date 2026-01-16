package dev.jaczerob.delfino.elm.events.loggedout.relog;

import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.elm.events.AbstractEmptyClientEvent;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;

public class RelogEvent extends AbstractEmptyClientEvent {
    public RelogEvent(InPacket inPacket, Client client, ChannelHandlerContext context) {
        super(inPacket, client, context);
    }
}
