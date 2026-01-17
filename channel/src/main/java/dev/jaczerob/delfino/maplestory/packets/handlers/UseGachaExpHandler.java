package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author kevintjuh93
 * <packet>
 * Modified by -- Ronan - concurrency protection
 */
@Component
public class UseGachaExpHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.USE_GACHA_EXP;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {

        if (client.tryacquireClient()) {
            try {
                if (client.getPlayer().getGachaExp() <= 0) {
                    AutobanFactory.GACHA_EXP.autoban(client.getPlayer(), "Player tried to redeem GachaEXP, but had none to redeem.");
                }
                client.getPlayer().gainGachaExp();
            } finally {
                client.releaseClient();
            }
        }

        context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
    }
}
