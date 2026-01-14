package dev.jaczerob.delfino.maplestory.net.netty;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.config.DelfinoConfigurationProperties;
import dev.jaczerob.delfino.maplestory.packets.ChannelPacketProcessor;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.packets.logging.InPacketLogger;
import dev.jaczerob.delfino.network.packets.logging.OutPacketLogger;
import dev.jaczerob.delfino.network.server.ServerChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class ChannelServerInitializer extends ServerChannelInitializer {
    private static final Logger log = LoggerFactory.getLogger(ChannelServerInitializer.class);

    private final AtomicLong sessionId = new AtomicLong(7777);

    public ChannelServerInitializer(
            final OutPacketLogger sendPacketLogger,
            final InPacketLogger receivePacketLogger,
            final DelfinoConfigurationProperties delfinoConfigurationProperties,
            final ChannelPacketCreator channelPacketCreator
    ) {
        super(
                sendPacketLogger,
                receivePacketLogger,
                delfinoConfigurationProperties.getNetty().getIdleTimeSeconds(),
                delfinoConfigurationProperties.getNetty().isLogPackets(),
                channelPacketCreator,
                delfinoConfigurationProperties.getServer().getVersion()
        );
    }

    @Override
    public void initChannel(SocketChannel socketChannel) {
        final String clientIp = socketChannel.remoteAddress().getHostString();
        log.debug("Client connected to channel server from {}", clientIp);

        final var packetProcessor = ChannelPacketProcessor.getInstance();
        final var clientSessionId = sessionId.getAndIncrement();
        final var remoteAddress = getRemoteAddress(socketChannel);
        final var client = Client.createChannelClient(clientSessionId, remoteAddress, packetProcessor, 0, 0);
        initPipeline(socketChannel, client);
    }
}
