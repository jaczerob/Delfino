package dev.jaczerob.delfino.elm.events.loggedin;

import dev.jaczerob.delfino.common.cache.login.LoginStatus;
import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.elm.events.AbstractClientEvent;
import dev.jaczerob.delfino.elm.events.AbstractClientEventHandler;
import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractLoggedInEventHandler<T, E extends AbstractClientEvent<T>> extends AbstractClientEventHandler<T, E> {
    private final SessionCoordinator sessionCoordinator;

    public AbstractLoggedInEventHandler(final SessionCoordinator sessionCoordinator) {
        this.sessionCoordinator = sessionCoordinator;
    }

    @Override
    protected final boolean validateState(E event) {
        return this.getSessionCoordinator().getLoggedInUserStatus(event.getClient()) == LoginStatus.LOGGED_IN;
    }
}
