package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author kevintjuh93
 */
@Component
public class LeftKnockbackHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.LEFT_KNOCKBACK;
    }

    public void handlePacket(InPacket packet, final Client client, final ChannelHandlerContext context) {
        client.sendPacket(ChannelPacketCreator.getInstance().leftKnockBack());
        client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
    }
}
