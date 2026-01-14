package dev.jaczerob.delfino.common.cache.login;

import org.springframework.stereotype.Service;

@Service
public class LoggedInUserService {
    private final LoggedInUserRepository repository;

    public LoggedInUserService(final LoggedInUserRepository repository) {
        this.repository = repository;
    }

    public void removeLoggedInUser(final int id) {
        this.repository.deleteById(id);
    }

    public void setLoggedInUserStatus(final int id, final LoginStatus status) {
        final var loggedInUser = new LoggedInUser();
        loggedInUser.setId(id);
        loggedInUser.setStatus(status);
        this.repository.save(loggedInUser);
    }

    public LoginStatus getLoggedInUserStatus(final int id) {
        return this.repository.findById(id)
                .map(LoggedInUser::getStatus)
                .orElse(LoginStatus.NOT_LOGGED_IN);
    }
}
