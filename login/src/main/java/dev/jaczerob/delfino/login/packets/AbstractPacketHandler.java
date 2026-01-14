package dev.jaczerob.delfino.login.packets;

import dev.jaczerob.delfino.common.cache.login.LoginStatus;
import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.packets.PacketHandler;

public abstract class AbstractPacketHandler implements PacketHandler<LoginClient> {
    protected final SessionCoordinator sessionCoordinator;
    protected final LoginPacketCreator loginPacketCreator;

    public AbstractPacketHandler(final SessionCoordinator sessionCoordinator, final LoginPacketCreator loginPacketCreator) {
        this.sessionCoordinator = sessionCoordinator;
        this.loginPacketCreator = loginPacketCreator;
    }

    @Override
    public boolean validateState(final LoginClient client) {
        return this.sessionCoordinator.getLoggedInUserStatus(client) == LoginStatus.LOGGED_IN;
    }
}