package dev.jaczerob.delfino.login.packets;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.network.packets.PacketHandler;
import dev.jaczerob.delfino.network.packets.PacketProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoginPacketProcessor extends PacketProcessor<LoginClient> {
    private static LoginPacketProcessor INSTANCE;

    public LoginPacketProcessor(final List<PacketHandler<LoginClient>> packetHandlers) {
        super(packetHandlers);
        INSTANCE = this;
    }

    public static LoginPacketProcessor getInstance() {
        return INSTANCE;
    }
}
