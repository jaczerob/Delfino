package dev.jaczerob.delfino.maplestory.packets;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.network.packets.PacketHandler;
import dev.jaczerob.delfino.network.packets.PacketProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChannelPacketProcessor extends PacketProcessor<Client> {
    private static ChannelPacketProcessor INSTANCE;

    public ChannelPacketProcessor(final List<PacketHandler<Client>> packetHandlers) {
        super(packetHandlers);
        INSTANCE = this;
    }

    public static ChannelPacketProcessor getInstance() {
        return INSTANCE;
    }
}
