package dev.jaczerob.delfino.login.netty;

import dev.jaczerob.delfino.login.PacketProcessor;
import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.server.coordinator.session.Hwid;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoginServerInitializer extends ServerChannelInitializer {
    private static final Logger log = LoggerFactory.getLogger(LoginServerInitializer.class);

    private final PacketProcessor packetProcessor;

    public LoginServerInitializer(final PacketProcessor packetProcessor) {
        this.packetProcessor = packetProcessor;
    }

    @Override
    public void initChannel(SocketChannel socketChannel) {
        final String clientIp = socketChannel.remoteAddress().getHostString();
        log.debug("Client connected to login server from {} ", clientIp);

        final long clientSessionId = sessionId.getAndIncrement();
        final var hwid = Hwid.fromHostString(clientIp);
        final LoginClient client = LoginClient.createLoginClient(this.packetProcessor, hwid, clientIp, clientSessionId);
        this.initPipeline(socketChannel, client);
    }
}
