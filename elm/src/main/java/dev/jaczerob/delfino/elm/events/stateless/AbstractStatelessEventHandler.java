package dev.jaczerob.delfino.elm.events.stateless;

import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.elm.events.AbstractClientEvent;
import dev.jaczerob.delfino.elm.events.AbstractClientEventHandler;

public abstract class AbstractStatelessEventHandler<T, E extends AbstractClientEvent<T>> extends AbstractClientEventHandler<T, E> {
    public AbstractStatelessEventHandler(SessionCoordinator sessionCoordinator) {
        super(sessionCoordinator);
    }

    @Override
    protected final boolean validateState(E event) {
        return true;
    }
}
