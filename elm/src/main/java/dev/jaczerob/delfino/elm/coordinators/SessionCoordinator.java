package dev.jaczerob.delfino.elm.coordinators;

import dev.jaczerob.delfino.common.cache.login.LoggedInUserService;
import dev.jaczerob.delfino.common.cache.login.LoginStatus;
import dev.jaczerob.delfino.elm.client.Client;
import org.springframework.stereotype.Component;

@Component
public class SessionCoordinator {
    private static SessionCoordinator INSTANCE;

    public static SessionCoordinator getInstance() {
        return INSTANCE;
    }

    private final LoggedInUserService loggedInUserService;

    public SessionCoordinator(final LoggedInUserService loggedInUserService) {
        this.loggedInUserService = loggedInUserService;
        INSTANCE = this;
    }

    public LoginStatus getLoggedInUserStatus(final Client client) {
        if (client == null || client.getAccount() == null) {
            return LoginStatus.NOT_LOGGED_IN;
        }

        final var accountId = client.getAccount().getId();
        return this.loggedInUserService.getLoggedInUserStatus(accountId);
    }

    public void login(final Client client) {
        this.updateLoggedInUserStatus(client, LoginStatus.LOGGED_IN);
    }

    public void serverTransition(final Client client) {
        this.updateLoggedInUserStatus(client, LoginStatus.SERVER_TRANSITION);
    }

    private void updateLoggedInUserStatus(final Client client, final LoginStatus status) {
        if (client == null || client.getAccount() == null) {
            return;
        }

        final var accountId = client.getAccount().getId();
        this.loggedInUserService.setLoggedInUserStatus(accountId, status);
    }

    public void logout(final Client client) {
        if (client == null) {
            return;
        }

        if (client.getAccount() != null) {
            final var accountId = client.getAccount().getId();
            this.loggedInUserService.removeLoggedInUser(accountId);
        }

        client.getIoChannel().close();
    }
}