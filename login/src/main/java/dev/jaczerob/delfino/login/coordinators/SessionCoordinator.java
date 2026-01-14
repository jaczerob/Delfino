package dev.jaczerob.delfino.login.coordinators;

import dev.jaczerob.delfino.common.cache.login.LoggedInUserService;
import dev.jaczerob.delfino.common.cache.login.LoginStatus;
import dev.jaczerob.delfino.login.client.LoginClient;
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

    public LoginStatus getLoggedInUserStatus(final LoginClient loginClient) {
        if (loginClient == null || loginClient.getAccount() == null) {
            return LoginStatus.NOT_LOGGED_IN;
        }

        final var accountId = loginClient.getAccount().getId();
        return this.loggedInUserService.getLoggedInUserStatus(accountId);
    }

    public void login(final LoginClient loginClient) {
        this.updateLoggedInUserStatus(loginClient, LoginStatus.LOGGED_IN);
    }

    public void serverTransition(final LoginClient loginClient) {
        this.updateLoggedInUserStatus(loginClient, LoginStatus.SERVER_TRANSITION);
    }

    private void updateLoggedInUserStatus(final LoginClient loginClient, final LoginStatus status) {
        if (loginClient == null || loginClient.getAccount() == null) {
            return;
        }

        final var accountId = loginClient.getAccount().getId();
        this.loggedInUserService.setLoggedInUserStatus(accountId, status);
    }

    public void logout(final LoginClient loginClient) {
        if (loginClient == null) {
            return;
        }

        if (loginClient.getAccount() != null) {
            final var accountId = loginClient.getAccount().getId();
            this.loggedInUserService.removeLoggedInUser(accountId);
        }

        loginClient.getIoChannel().close();
    }
}