package dev.jaczerob.delfino.maplestory.net.netty;

import dev.jaczerob.delfino.network.server.AbstractServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChannelServer extends AbstractServer {
    public ChannelServer(
            final @Value("${server.port}") int port,
            final ChannelServerInitializer channelServerInitializer
    ) {
        super(port, channelServerInitializer);
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
