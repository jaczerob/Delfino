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
        if (loginClient == null) {
            return;
        }

        final var accountId = loginClient.getAccId();
        disconnectClientIfOnline(accountId);
        this.onlineClients.put(accountId, loginClient);
    }

    private void disconnectClientIfOnline(int accountId) {
        LoginClient inGameLoginClient = onlineClients.get(accountId);
        if (inGameLoginClient != null) {     // thanks MedicOP for finding out a loss of loggedin account uniqueness when using the CMS "Unstuck" feature
            inGameLoginClient.forceDisconnect();
        }
    }

    public void closeLoginSession(final LoginClient loginClient) {
        if (loginClient == null) {
            return;
        }

        final var nibbleHwid = loginClient.getHwid();
        loginClient.setHwid(null);
        if (nibbleHwid != null) {
            LoginClient loggedLoginClient = onlineClients.get(loginClient.getAccId());
            if (loggedLoginClient != null && loggedLoginClient.getSessionId() == loginClient.getSessionId()) {
                onlineClients.remove(loginClient.getAccId());
            }
        }
    }

    public void closeSession(LoginClient loginClient, Boolean immediately) {
        if (loginClient == null) {
            return;
        }

        final HWID hwid = loginClient.getHwid();
        loginClient.setHwid(null);

        final boolean isGameSession = hwid != null;
        if (isGameSession) {
            onlineClients.remove(loginClient.getAccId());
        } else {
            LoginClient loggedLoginClient = onlineClients.get(loginClient.getAccId());

            if (loggedLoginClient != null && loggedLoginClient.getSessionId() == loginClient.getSessionId()) {
                onlineClients.remove(loginClient.getAccId());
            }
        }

        if (immediately != null && immediately) {
            loginClient.closeSession();
        }
    }
}