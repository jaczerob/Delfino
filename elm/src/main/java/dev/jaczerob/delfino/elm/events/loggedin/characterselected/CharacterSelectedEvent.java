package dev.jaczerob.delfino.elm.events.loggedin.characterselected;

import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.elm.events.AbstractClientEvent;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;

public class CharacterSelectedEvent extends AbstractClientEvent<CharacterSelectedPayload> {
    public CharacterSelectedEvent(InPacket inPacket, Client client, ChannelHandlerContext context) {
        super(inPacket, client, context);
    }

    @Override
    protected CharacterSelectedPayload parsePayload(final InPacket inPacket) {
        inPacket.readString();
        final var characterId = inPacket.readInt();
        inPacket.readString();
        inPacket.readString();

        return new CharacterSelectedPayload(characterId);
    }
}
