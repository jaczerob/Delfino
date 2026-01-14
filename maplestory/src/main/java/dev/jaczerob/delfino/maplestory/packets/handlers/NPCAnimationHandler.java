package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.net.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class NPCAnimationHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.NPC_ACTION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (client.getPlayer().isChangingMaps()) {   // possible cause of error 38 in some map transition scenarios, thanks Arnah
            return;
        }

        OutPacket op = OutPacket.create(SendOpcode.NPC_ACTION);
        int length = packet.available();
        if (length == 6) { // NPC Talk
            op.writeInt(packet.readInt());
            op.writeByte(packet.readByte());   // 2 bytes, thanks resinate
            op.writeByte(packet.readByte());
        } else if (length > 6) { // NPC Move
            byte[] bytes = packet.readBytes(length - 9);
            op.writeBytes(bytes);
        }
        client.sendPacket(op);
    }
}
