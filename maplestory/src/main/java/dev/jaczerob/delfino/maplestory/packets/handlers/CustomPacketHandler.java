package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import dev.jaczerob.delfino.network.packets.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public class CustomPacketHandler implements PacketHandler<Client> {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CUSTOM_PACKET;
    }

    @Override
    public void handlePacket(InPacket p, Client c, ChannelHandlerContext context) {
        if (p.available() > 0 && c.getGMLevel() == 4) {//w/e
            c.sendPacket(ChannelPacketCreator.getInstance().customPacket(p.readBytes(p.available())));
        }
    }

    @Override
    public boolean validateState(Client c) {
        return true;
    }
}
