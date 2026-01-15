package dev.jaczerob.delfino.elm.events.loggedout.loginpassword;

import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.elm.events.loggedout.AbstractLoggedOutEventHandler;
import dev.jaczerob.delfino.grpc.proto.account.Account;
import dev.jaczerob.delfino.grpc.proto.account.AccountRequest;
import dev.jaczerob.delfino.grpc.proto.account.AccountServiceGrpc;
import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class LoginPasswordHandler extends AbstractLoggedOutEventHandler<LoginPasswordPayload, LoginPasswordEvent> {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AccountServiceGrpc.AccountServiceBlockingV2Stub accountService;

    public LoginPasswordHandler(
            final SessionCoordinator sessionCoordinator,
            final AccountServiceGrpc.AccountServiceBlockingV2Stub accountService
    ) {
        super(sessionCoordinator);
        this.accountService = accountService;
    }

    @Override
    protected void handleEventInternal(LoginPasswordEvent event) {
        final Account account;
        try {
            account = this.accountService.getAccount(AccountRequest.newBuilder().setUsername(event.getPayload().username()).build()).getAccount();
        } catch (final Exception exc) {
            this.getLogger().error("Error retrieving account for username {}", event.getPayload().username(), exc);
            event.getContext().writeAndFlush(this.createLoginFailedPacket(5));
            return;
        }

        final var passwordMatches = this.passwordEncoder.matches(event.getPayload().password(), account.getPassword());
        if (!passwordMatches) {
            event.getContext().writeAndFlush(this.createLoginFailedPacket(4));
            return;
        }

        event.getClient().setAccount(account);
        this.getSessionCoordinator().login(event.getClient());
        event.getContext().writeAndFlush(this.getAuthSuccess(event.getClient()));
    }

    private Packet createLoginFailedPacket(final int reason) {
        return OutPacket.create(SendOpcode.LOGIN_STATUS)
                .writeByte(reason)
                .writeByte(0)
                .writeInt(0);
    }
}
