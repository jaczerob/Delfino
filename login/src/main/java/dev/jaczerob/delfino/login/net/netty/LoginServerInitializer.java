package dev.jaczerob.delfino.login.net.netty;

import dev.jaczerob.delfino.login.client.Client;
import io.netty.channel.socket.SocketChannel;
import dev.jaczerob.delfino.login.net.PacketProcessor;
import dev.jaczerob.delfino.login.net.server.coordinator.session.SessionCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginServerInitializer extends ServerChannelInitializer {
    private static final Logger log = LoggerFactory.getLogger(LoginServerInitializer.class);

    @Override
    public void initChannel(SocketChannel socketChannel) {
        final String clientIp = socketChannel.remoteAddress().getHostString();
        log.debug("Client connected to login server from {} ", clientIp);

        PacketProcessor packetProcessor = PacketProcessor.getLoginServerProcessor();
        final long clientSessionId = sessionId.getAndIncrement();
        final String remoteAddress = getRemoteAddress(socketChannel);
        final Client client = Client.createLoginClient(clientSessionId, remoteAddress, packetProcessor, LoginNettyServer.WORLD_ID, LoginNettyServer.CHANNEL_ID);

        if (!SessionCoordinator.getInstance().canStartLoginSession(client)) {
            socketChannel.close();
            return;
        }

        initPipeline(socketChannel, client);
    }
}
