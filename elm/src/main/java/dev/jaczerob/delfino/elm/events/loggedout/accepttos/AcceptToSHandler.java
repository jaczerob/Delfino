package dev.jaczerob.delfino.elm.events.loggedout.accepttos;

import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.elm.events.loggedout.AbstractLoggedOutEventHandler;
import org.springframework.stereotype.Component;

@Component
public class AcceptToSHandler extends AbstractLoggedOutEventHandler<AcceptToSPayload, AcceptToSEvent> {
    public AcceptToSHandler(SessionCoordinator sessionCoordinator) {
        super(sessionCoordinator);
    }

    @Override
    protected void handleEventInternal(AcceptToSEvent event) {
        if (!event.getPayload().accepted()) {
            this.getSessionCoordinator().logout(event.getClient());
            return;
        }

        event.getContext().writeAndFlush(this.getAuthSuccess(event.getClient()));
    }
}
