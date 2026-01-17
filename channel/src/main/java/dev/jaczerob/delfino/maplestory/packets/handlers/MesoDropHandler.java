package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Matze
 * @author Ronan - concurrency protection
 */
@Component
public final class MesoDropHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.MESO_DROP;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character player = client.getPlayer();
        if (!player.isAlive()) {
            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
            return;
        }
        packet.skip(4);
        int meso = packet.readInt();

        if (player.isGM() && player.gmLevel() < YamlConfig.config.server.MINIMUM_GM_LEVEL_TO_DROP) {
            player.message("You cannot drop mesos at your GM level.");
            return;
        }

        if (client.tryacquireClient()) {     // thanks imbee for noticing players not being able to throw mesos too fast
            try {
                if (meso <= player.getMeso() && meso > 9 && meso < 50001) {
                    player.gainMeso(-meso, false, true, false);
                } else {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }
            } finally {
                client.releaseClient();
            }
        } else {
            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        if (player.attemptCatchFish(meso)) {
            player.getMap().disappearingMesoDrop(meso, player, player, player.getPosition());
        } else {
            player.getMap().spawnMesoDrop(meso, player.getPosition(), player, player, true, (byte) 2,
                    (short) 0);
        }
    }
}
