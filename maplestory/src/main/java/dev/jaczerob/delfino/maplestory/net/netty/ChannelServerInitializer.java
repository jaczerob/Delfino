package dev.jaczerob.delfino.maplestory.net.netty;

import dev.jaczerob.delfino.common.config.DelfinoConfigurationProperties;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.ChannelPacketProcessor;
import dev.jaczerob.delfino.network.packets.logging.InPacketLogger;
import dev.jaczerob.delfino.network.packets.logging.OutPacketLogger;
import dev.jaczerob.delfino.network.server.ServerChannelInitializer;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class ChannelServerInitializer extends ServerChannelInitializer {
    private final AtomicLong sessionId = new AtomicLong(7777);

    public ChannelServerInitializer(final OutPacketLogger sendPacketLogger, final InPacketLogger receivePacketLogger, final DelfinoConfigurationProperties delfinoConfigurationProperties) {
        super(sendPacketLogger, receivePacketLogger, delfinoConfigurationProperties);
    }

    @Override
    protected ChannelInboundHandlerAdapter initClient() {
        final var packetProcessor = ChannelPacketProcessor.getInstance();
        final var clientSessionId = sessionId.getAndIncrement();
        return Client.createChannelClient(clientSessionId, packetProcessor, 0, 0);
    }
}
