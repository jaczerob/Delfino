package dev.jaczerob.delfino.elm.events.loggedout.accepttos;

import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.elm.events.AbstractClientEvent;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;

public class AcceptToSEvent extends AbstractClientEvent<AcceptToSPayload> {
    public AcceptToSEvent(InPacket inPacket, Client client, ChannelHandlerContext context) {
        super(inPacket, client, context);
    }

    @Override
    protected AcceptToSPayload parsePayload(final InPacket inPacket) {
        final boolean accepted = inPacket.available() != 0 && inPacket.readByte() == 1;
        return new AcceptToSPayload(accepted);
    }
}
