package dev.jaczerob.delfino.login.server;

import dev.jaczerob.delfino.network.server.AbstractServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoginServer extends AbstractServer {
    public LoginServer(
            final @Value("${server.port}") int port,
            final LoginServerInitializer loginServerInitializer
    ) {
        super(port, loginServerInitializer);
    }

    @PostConstruct
    public void start() {
        this.startServer();
    }

    public String[] getInetSocket() {
        // TODO: Implement proper world/channel IP retrieval
        return new String[]{
                "127.0.0.1",
                "7575"
        };
    }

    @PreDestroy
    public void stop() {
        this.stopServer();
    }
}
