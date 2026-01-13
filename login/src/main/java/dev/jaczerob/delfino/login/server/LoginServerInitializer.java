package dev.jaczerob.delfino.login.server;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.config.DelfinoConfigurationProperties;
import dev.jaczerob.delfino.login.packets.PacketProcessor;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.packets.logging.InPacketLogger;
import dev.jaczerob.delfino.network.packets.logging.OutPacketLogger;
import dev.jaczerob.delfino.network.server.ServerChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoginServerInitializer extends ServerChannelInitializer {
    private static final Logger log = LoggerFactory.getLogger(LoginServerInitializer.class);

    private final LoginPacketCreator loginPacketCreator;

    public LoginServerInitializer(
            final OutPacketLogger sendPacketLogger,
            final InPacketLogger receivePacketLogger,
            final DelfinoConfigurationProperties delfinoConfigurationProperties,
            final LoginPacketCreator loginPacketCreator
    ) {
        super(
                sendPacketLogger,
                receivePacketLogger,
                delfinoConfigurationProperties.getNetty().getIdleTimeSeconds(),
                delfinoConfigurationProperties.getNetty().isLogPackets(),
                loginPacketCreator,
                delfinoConfigurationProperties.getServer().getVersion()
        );

        this.loginPacketCreator = loginPacketCreator;
    }

    @Override
    public void initChannel(SocketChannel socketChannel) {
        final var clientIp = socketChannel.remoteAddress().getHostString();
        log.debug("Client connected to login server from {} ", clientIp);

        final var remoteAddress = this.getRemoteAddress(socketChannel);
        final var client = new LoginClient(remoteAddress, PacketProcessor.getInstance(), this.loginPacketCreator);
        this.initPipeline(socketChannel, client);
    }
}
