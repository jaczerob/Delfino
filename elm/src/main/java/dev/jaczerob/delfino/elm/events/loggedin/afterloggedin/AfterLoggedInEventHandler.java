package dev.jaczerob.delfino.elm.events.loggedin.afterloggedin;

import dev.jaczerob.delfino.elm.events.AbstractClientEventHandler;
import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import org.springframework.stereotype.Component;

@Component
public class AfterLoggedInEventHandler extends AbstractClientEventHandler<Object, AfterLoggedInEvent> {
    @Override
    protected void handleEventInternal(AfterLoggedInEvent event) {
        event.getContext().writeAndFlush(this.createPacket());
    }

    private Packet createPacket() {
        return OutPacket.create(SendOpcode.CHECK_PINCODE)
                .writeByte(0);
    }
}
