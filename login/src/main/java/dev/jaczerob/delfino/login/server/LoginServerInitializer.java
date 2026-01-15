package dev.jaczerob.delfino.login.server;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.config.DelfinoConfigurationProperties;
import dev.jaczerob.delfino.login.packets.LoginPacketProcessor;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.packets.logging.InPacketLogger;
import dev.jaczerob.delfino.network.packets.logging.OutPacketLogger;
import dev.jaczerob.delfino.network.server.ServerChannelInitializer;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

@Component
public class LoginServerInitializer extends ServerChannelInitializer {
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
                delfinoConfigurationProperties.getServer().getVersion()
        );

        this.loginPacketCreator = loginPacketCreator;
    }

    @Override
    protected ChannelInboundHandlerAdapter initClient() {
        return new LoginClient(LoginPacketProcessor.getInstance(), this.loginPacketCreator);
    }
}
