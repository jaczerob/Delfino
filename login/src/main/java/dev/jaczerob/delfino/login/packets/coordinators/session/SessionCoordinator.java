package dev.jaczerob.delfino.login.packets.coordinators.session;

import dev.jaczerob.delfino.login.client.LoginClient;

import java.util.HashMap;
import java.util.Map;

public class SessionCoordinator {
    private static final SessionCoordinator instance = new SessionCoordinator();

    public static SessionCoordinator getInstance() {
        return instance;
    }

    private final Map<Integer, LoginClient> onlineClients = new HashMap<>();

    public void updateOnlineClient(final LoginClient loginClient) {
        if (loginClient == null || loginClient.getAccount() == null) {
            return;
        }

        final var accountId = loginClient.getAccount().getId();
        disconnectClientIfOnline(accountId);
        this.onlineClients.put(accountId, loginClient);
    }

    private void disconnectClientIfOnline(int accountId) {
        LoginClient inGameLoginClient = this.onlineClients.get(accountId);
        if (inGameLoginClient != null) {
            inGameLoginClient.forceDisconnect();
        }
    }

    public void closeLoginSession(final LoginClient loginClient) {
        if (loginClient == null || loginClient.getAccount() == null) {
            return;
        }

        final var accountId = loginClient.getAccount().getId();
        final var nibbleHwid = loginClient.getHwid();

        loginClient.setHwid(null);
        if (nibbleHwid != null) {
            final var loggedLoginClient = onlineClients.get(accountId);
            if (loggedLoginClient != null && loggedLoginClient.getSessionId() == loginClient.getSessionId()) {
                onlineClients.remove(accountId);
            }
        }
    }

    public void closeSession(LoginClient loginClient, Boolean immediately) {
        if (loginClient == null || loginClient.getAccount() == null) {
            return;
        }

        final var accountId = loginClient.getAccount().getId();
        final var hwid = loginClient.getHwid();
        loginClient.setHwid(null);

        final boolean isGameSession = hwid != null;
        if (isGameSession) {
            onlineClients.remove(accountId);
        } else {
            LoginClient loggedLoginClient = onlineClients.get(accountId);

            if (loggedLoginClient != null && loggedLoginClient.getSessionId() == loginClient.getSessionId()) {
                onlineClients.remove(accountId);
            }
        }

        if (immediately != null && immediately) {
            loginClient.closeSession();
        }
    }
}