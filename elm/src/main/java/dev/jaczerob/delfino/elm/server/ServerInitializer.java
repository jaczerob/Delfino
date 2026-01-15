package dev.jaczerob.delfino.elm.server;

import dev.jaczerob.delfino.common.config.DelfinoConfigurationProperties;
import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.network.packets.logging.InPacketLogger;
import dev.jaczerob.delfino.network.packets.logging.OutPacketLogger;
import dev.jaczerob.delfino.network.server.ServerChannelInitializer;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ServerInitializer extends ServerChannelInitializer {
    private final ApplicationEventPublisher applicationEventPublisher;

    public ServerInitializer(
            final OutPacketLogger sendPacketLogger,
            final InPacketLogger receivePacketLogger,
            final DelfinoConfigurationProperties delfinoConfigurationProperties,
            final ApplicationEventPublisher applicationEventPublisher
    ) {
        super(
                sendPacketLogger,
                receivePacketLogger,
                delfinoConfigurationProperties.getNetty().getIdleTimeSeconds(),
                delfinoConfigurationProperties.getNetty().isLogPackets(),
                delfinoConfigurationProperties.getServer().getVersion()
        );

        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    protected ChannelInboundHandlerAdapter initClient() {
        return new Client(this.applicationEventPublisher);
    }
}
