package dev.jaczerob.delfino.elm.events;

import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationListener;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractClientEventHandler<T, E extends AbstractClientEvent<T>> implements ApplicationListener<E> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public final void onApplicationEvent(final E event) {
        MDC.put("event.name", event.getClass().getSimpleName());

        final boolean validState;
        try {
            this.getLogger().info("Validating state for event handling");
            validState = this.validateState(event);
        } catch (final Exception exc) {
            this.getLogger().error("Exception occurred during state validation, aborting", exc);
            return;
        }


        if (!validState) {
            this.getLogger().info("Invalid state for event handling, aborting");
            return;
        }

        try {
            this.getLogger().info("Handling event");
            this.handleEventInternal(event);
            this.getLogger().info("Event handled successfully");
        } catch (final Exception exc) {
            this.getLogger().error("Exception occurred during event handling", exc);
        }
    }

    protected boolean validateState(E event) {
        return true;
    }

    protected abstract void handleEventInternal(E event);
}
