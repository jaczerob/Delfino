package dev.jaczerob.delfino.elm.events.loggedout.relog;

import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.elm.events.loggedout.AbstractLoggedOutEventHandler;
import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import org.springframework.stereotype.Component;

@Component
public class RelogEventHandler extends AbstractLoggedOutEventHandler<Object, RelogEvent> {
    public RelogEventHandler(final SessionCoordinator sessionCoordinator) {
        super(sessionCoordinator);
    }

    @Override
    protected void handleEventInternal(RelogEvent event) {
        event.getContext().writeAndFlush(this.createRelogPacket());
    }

    private Packet createRelogPacket() {
        return OutPacket.create(SendOpcode.RELOG_RESPONSE)
                .writeByte(1);
    }
}
