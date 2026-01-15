package dev.jaczerob.delfino.elm.events.loggedin.afterloggedin;

import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.elm.events.AbstractClientEvent;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;

public class AfterLoggedInEvent extends AbstractClientEvent<Object> {
    public AfterLoggedInEvent(InPacket inPacket, Client client, ChannelHandlerContext context) {
        super(inPacket, client, context);
    }

    @Override
    protected Object parsePayload(InPacket inPacket) {
        return new Object();
    }
}
