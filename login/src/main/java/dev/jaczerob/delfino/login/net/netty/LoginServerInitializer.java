package dev.jaczerob.delfino.login.net.netty;

import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.net.PacketProcessor;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginServerInitializer extends ServerChannelInitializer {
    private static final Logger log = LoggerFactory.getLogger(LoginServerInitializer.class);

    @Override
    public void initChannel(SocketChannel socketChannel) {
        final var clientIp = socketChannel.remoteAddress().getHostString();
        log.debug("Client connected to login server from {} ", clientIp);

        final var packetProcessor = PacketProcessor.getLoginServerProcessor();
        final var clientSessionId = sessionId.getAndIncrement();
        final var remoteAddress = getRemoteAddress(socketChannel);
        final var client = Client.createLoginClient(clientSessionId, remoteAddress, packetProcessor, LoginNettyServer.WORLD_ID, LoginNettyServer.CHANNEL_ID);

        this.initPipeline(socketChannel, client);
    }
}
