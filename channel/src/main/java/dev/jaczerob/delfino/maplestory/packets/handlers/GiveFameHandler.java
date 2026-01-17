package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Character.FameStatus;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class GiveFameHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(GiveFameHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.GIVE_FAME;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character target = (Character) client.getPlayer().getMap().getMapObject(packet.readInt());
        int mode = packet.readByte();
        int famechange = 2 * mode - 1;
        Character player = client.getPlayer();
        if (target == null || target.getId() == player.getId() || player.getLevel() < 15) {
            return;
        } else if (famechange != 1 && famechange != -1) {
            AutobanFactory.PACKET_EDIT.alert(client.getPlayer(), client.getPlayer().getName() + " tried to packet edit fame.");
            log.warn("Chr {} tried to fame hack with famechange {}", client.getPlayer().getName(), famechange);
            client.disconnect(true, false);
            return;
        }

        FameStatus status = player.canGiveFame(target);
        if (status == FameStatus.OK) {
            if (target.gainFame(famechange, player, mode)) {
                if (!player.isGM()) {
                    player.hasGivenFame(target);
                }
            } else {
                player.message("Could not process the request, since this character currently has the minimum/maximum level of fame.");
            }
        } else {
            context.writeAndFlush(ChannelPacketCreator.getInstance().giveFameErrorResponse(status == FameStatus.NOT_TODAY ? 3 : 4));
        }
    }
}