package dev.jaczerob.delfino.elm.server;

import dev.jaczerob.delfino.network.server.AbstractServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Server extends AbstractServer {
    public Server(
            final @Value("${server.port}") int port,
            final ServerInitializer serverInitializer
    ) {
        super(port, serverInitializer);
    }

    @PostConstruct
    public void start() {
        this.startServer();
    }

    @PreDestroy
    public void stop() {
        this.stopServer();
    }
}
