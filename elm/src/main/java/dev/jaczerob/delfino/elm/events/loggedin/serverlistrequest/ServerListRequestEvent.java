package dev.jaczerob.delfino.elm.events.loggedin.serverlistrequest;

import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.elm.events.AbstractEmptyClientEvent;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;

public class ServerListRequestEvent extends AbstractEmptyClientEvent {
    public ServerListRequestEvent(InPacket inPacket, Client client, ChannelHandlerContext context) {
        super(inPacket, client, context);
    }
}
