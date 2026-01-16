package dev.jaczerob.delfino.elm.events.loggedin.serverstatus;

import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.elm.events.AbstractEmptyClientEvent;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;

public class ServerStatusRequestEvent extends AbstractEmptyClientEvent {
    public ServerStatusRequestEvent(InPacket inPacket, Client client, ChannelHandlerContext context) {
        super(inPacket, client, context);
    }
}
