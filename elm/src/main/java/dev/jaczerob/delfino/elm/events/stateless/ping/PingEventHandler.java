package dev.jaczerob.delfino.elm.events.stateless.ping;

import dev.jaczerob.delfino.elm.events.stateless.AbstractStatelessEventHandler;
import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.OutPacket;
import org.springframework.stereotype.Component;

@Component
public class PingEventHandler extends AbstractStatelessEventHandler<Object, PingEvent> {
    @Override
    protected void handleEventInternal(PingEvent event) {
        event.getContext().writeAndFlush(OutPacket.create(SendOpcode.PING));
    }
}
