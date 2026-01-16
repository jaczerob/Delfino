package dev.jaczerob.delfino.elm.events.loggedin.serverstatus;

import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.elm.events.loggedin.AbstractLoggedInEventHandler;
import dev.jaczerob.delfino.elm.events.loggedin.serverlistrequest.ServerListRequestEvent;
import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import org.springframework.stereotype.Component;

@Component
public class ServerStatusRequestEventHandler extends AbstractLoggedInEventHandler<Object, ServerListRequestEvent> {
    public ServerStatusRequestEventHandler(final SessionCoordinator sessionCoordinator) {
        super(sessionCoordinator);
    }

    @Override
    protected void handleEventInternal(final ServerListRequestEvent event) {
        event.getContext().writeAndFlush(this.createPacket(0));
    }

    private Packet createPacket(final int status) {
        return OutPacket.create(SendOpcode.SERVERSTATUS)
                .writeShort(status);
    }
}
