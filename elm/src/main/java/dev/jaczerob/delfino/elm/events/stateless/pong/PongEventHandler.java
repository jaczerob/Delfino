package dev.jaczerob.delfino.elm.events.stateless.pong;

import dev.jaczerob.delfino.elm.events.stateless.AbstractStatelessEventHandler;
import org.springframework.stereotype.Component;

@Component
public class PongEventHandler extends AbstractStatelessEventHandler<Object, PongEvent> {
    @Override
    protected void handleEventInternal(PongEvent event) {

    }
}
