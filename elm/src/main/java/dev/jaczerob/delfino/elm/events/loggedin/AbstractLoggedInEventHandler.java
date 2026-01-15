package dev.jaczerob.delfino.elm.events.loggedin;

import dev.jaczerob.delfino.common.cache.login.LoginStatus;
import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.elm.events.AbstractClientEvent;
import dev.jaczerob.delfino.elm.events.AbstractClientEventHandler;

public abstract class AbstractLoggedInEventHandler<T, E extends AbstractClientEvent<T>> extends AbstractClientEventHandler<T, E> {
    public AbstractLoggedInEventHandler(SessionCoordinator sessionCoordinator) {
        super(sessionCoordinator);
    }

    @Override
    protected final boolean validateState(E event) {
        return this.getSessionCoordinator().getLoggedInUserStatus(event.getClient()) == LoginStatus.LOGGED_IN;
    }
}
