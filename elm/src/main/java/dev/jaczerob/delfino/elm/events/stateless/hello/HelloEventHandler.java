package dev.jaczerob.delfino.elm.events.stateless.hello;

import dev.jaczerob.delfino.elm.events.stateless.AbstractStatelessEventHandler;
import org.springframework.stereotype.Component;

@Component
public class HelloEventHandler extends AbstractStatelessEventHandler<Object, HelloEvent> {
    @Override
    protected void handleEventInternal(HelloEvent event) {

    }
}
