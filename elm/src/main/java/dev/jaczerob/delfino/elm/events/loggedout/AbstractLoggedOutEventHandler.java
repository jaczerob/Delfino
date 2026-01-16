package dev.jaczerob.delfino.elm.events.loggedout;

import dev.jaczerob.delfino.common.cache.login.LoginStatus;
import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.elm.events.AbstractClientEvent;
import dev.jaczerob.delfino.elm.events.AbstractClientEventHandler;
import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractLoggedOutEventHandler<T, E extends AbstractClientEvent<T>> extends AbstractClientEventHandler<T, E> {
    private final SessionCoordinator sessionCoordinator;

    public AbstractLoggedOutEventHandler(final SessionCoordinator sessionCoordinator) {
        this.sessionCoordinator = sessionCoordinator;
    }

    @Override
    protected final boolean validateState(E event) {
        return this.getSessionCoordinator().getLoggedInUserStatus(event.getClient()) == LoginStatus.NOT_LOGGED_IN;
    }

    protected Packet getAuthSuccess(final Client client) {
        return OutPacket.create(SendOpcode.LOGIN_STATUS)
                .writeInt(0)
                .writeShort(0)
                .writeInt(client.getAccount().getId())
                .writeByte(0)

                .writeBool(false)                       // is GM account
                .writeByte(0)
                .writeByte(0)

                .writeString(client.getAccount().getName())
                .writeByte(0)

                .writeByte(0)
                .writeLong(0)
                .writeLong(0)

                .writeInt(1)

                .writeByte(PINEnabled.DISABLED)
                .writeByte(PICEnabled.DISABLED);
    }

    private static class PINEnabled {
        private static final int ENABLED = 0;
        private static final int DISABLED = 1;
    }

    private static class PICEnabled {
        private static final int ENABLED = 0;
        private static final int ASK_FOR = 1;
        private static final int DISABLED = 2;
    }
}
