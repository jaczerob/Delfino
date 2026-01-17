package dev.jaczerob.delfino.elm.server;

import dev.jaczerob.delfino.common.config.DelfinoConfigurationProperties;
import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.network.packets.logging.InPacketLogger;
import dev.jaczerob.delfino.network.packets.logging.OutPacketLogger;
import dev.jaczerob.delfino.network.server.ServerChannelInitializer;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ServerInitializer extends ServerChannelInitializer {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final SessionCoordinator sessionCoordinator;

    public ServerInitializer(
            final OutPacketLogger sendPacketLogger,
            final InPacketLogger receivePacketLogger,
            final DelfinoConfigurationProperties delfinoConfigurationProperties,
            final ApplicationEventPublisher applicationEventPublisher,
            final SessionCoordinator sessionCoordinator
    ) {
        super(sendPacketLogger, receivePacketLogger, delfinoConfigurationProperties);
        this.applicationEventPublisher = applicationEventPublisher;
        this.sessionCoordinator = sessionCoordinator;
    }

    @Override
    protected ChannelInboundHandlerAdapter initClient() {
        return new Client(this.applicationEventPublisher, this.sessionCoordinator);
    }
}
