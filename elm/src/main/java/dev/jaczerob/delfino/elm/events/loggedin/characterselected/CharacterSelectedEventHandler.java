package dev.jaczerob.delfino.elm.events.loggedin.characterselected;

import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.elm.events.loggedin.AbstractLoggedInEventHandler;
import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class CharacterSelectedEventHandler extends AbstractLoggedInEventHandler<CharacterSelectedPayload, CharacterSelectedEvent> {
    public CharacterSelectedEventHandler(SessionCoordinator sessionCoordinator) {
        super(sessionCoordinator);
    }

    @Override
    protected void handleEventInternal(final CharacterSelectedEvent event) {
        try {
            event.getContext().writeAndFlush(this.getServerIP(InetAddress.getByName("127.0.0.1"), 7575, event.getPayload().characterId()));
        } catch (final UnknownHostException exc) {
            this.getLogger().error("Failed to resolve server address", exc);
            event.getContext().writeAndFlush(this.getAfterLoginError(10));
        }
    }

    private Packet getServerIP(final InetAddress address, final int port, final int clientId) {
        return OutPacket.create(SendOpcode.SERVER_IP)
                .writeShort(0)
                .writeBytes(address.getAddress())
                .writeShort(port)
                .writeInt(clientId)
                .writeBytes(new byte[]{0, 0, 0, 0, 0});
    }

    private Packet getAfterLoginError(final int reason) {
        return OutPacket.create(SendOpcode.SELECT_CHARACTER_BY_VAC)
                .writeShort(reason);
    }
}
