package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import dev.jaczerob.delfino.network.packets.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public class KeepAliveHandler implements PacketHandler<Client> {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.PONG;
    }

    @Override
    public void handlePacket(InPacket packet, Client client, ChannelHandlerContext context) {
        client.pongReceived();
    }

    @Override
    public boolean validateState(Client c) {
        return true;
    }
}
