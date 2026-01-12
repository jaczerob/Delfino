package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.grpc.proto.account.Account;
import dev.jaczerob.delfino.grpc.proto.account.AccountRequest;
import dev.jaczerob.delfino.grpc.proto.account.AccountServiceGrpc;
import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class LoginPasswordHandler extends AbstractPacketHandler {
    private final Logger log = LoggerFactory.getLogger(LoginPasswordHandler.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final AccountServiceGrpc.AccountServiceBlockingV2Stub accountService;

    public LoginPasswordHandler(
            final SessionCoordinator sessionCoordinator,
            final LoginPacketCreator loginPacketCreator,
            final AccountServiceGrpc.AccountServiceBlockingV2Stub accountService
    ) {
        super(sessionCoordinator, loginPacketCreator);
        this.accountService = accountService;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.LOGIN_PASSWORD;
    }

    @Override
    public boolean validateState(final LoginClient client) {
        return this.sessionCoordinator.getLoggedInUserStatus(client) != dev.jaczerob.delfino.login.client.LoginStatus.LOGGED_IN;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client, final ChannelHandlerContext context) {
        final var remoteHost = client.getRemoteAddress();
        if (remoteHost.contentEquals("null")) {
            this.log.warn("Client has null remote address, rejecting login attempt");
            context.writeAndFlush(this.loginPacketCreator.getLoginFailed(14));
            return;
        }

        final var payload = LoginPasswordPayload.fromPacket(packet);
        final Account account;
        try {
            account = this.accountService.getAccount(AccountRequest.newBuilder().setUsername(payload.username()).build()).getAccount();
        } catch (final Exception exc) {
            log.error("Error retrieving account for username {}", payload.username(), exc);
            context.writeAndFlush(this.loginPacketCreator.getLoginFailed(5));
            return;
        }

        final var loginStatus = this.login(account, payload.password());

        if (loginStatus != LoginStatus.SUCCESS) {
            context.writeAndFlush(this.loginPacketCreator.getLoginFailed(loginStatus.code()));
            return;
        }

        client.setAccount(account);
        this.sessionCoordinator.login(client);
        context.writeAndFlush(this.loginPacketCreator.getAuthSuccess(client));
    }

    private LoginStatus login(final Account account, final String password) {
        return this.passwordEncoder.matches(password, account.getPassword()) ? LoginStatus.SUCCESS : LoginStatus.INVALID_CREDENTIALS;
    }

    private enum LoginStatus {
        SUCCESS(0),
        INVALID_CREDENTIALS(4),
        FAILED_TO_LOGIN(7);

        private final int code;

        LoginStatus(final int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

    private record LoginPasswordPayload(
            String username,
            String password
    ) {
        static LoginPasswordPayload fromPacket(final InPacket packet) {
            final var username = packet.readString();
            final var password = packet.readString();
            return new LoginPasswordPayload(username, password);
        }
    }
}
