package dev.jaczerob.delfino.elm.events.loggedout.loginpassword;

import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.elm.events.AbstractClientEvent;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;

public class LoginPasswordEvent extends AbstractClientEvent<LoginPasswordPayload> {
    public LoginPasswordEvent(InPacket inPacket, Client client, ChannelHandlerContext context) {
        super(inPacket, client, context);
    }

    @Override
    protected LoginPasswordPayload parsePayload(InPacket inPacket) {
        final var username = inPacket.readString();
        final var password = inPacket.readString();
        return new LoginPasswordPayload(username, password);
    }
}
