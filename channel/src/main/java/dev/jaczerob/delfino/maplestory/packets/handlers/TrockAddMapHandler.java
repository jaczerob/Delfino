package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.FieldLimit;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author kevintjuh93
 */
@Component
public final class TrockAddMapHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.TROCK_ADD_MAP;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        byte type = packet.readByte();
        boolean vip = packet.readByte() == 1;
        if (type == 0x00) {
            int mapId = packet.readInt();
            if (vip) {
                chr.deleteFromVipTrocks(mapId);
            } else {
                chr.deleteFromTrocks(mapId);
            }
            client.sendPacket(ChannelPacketCreator.getInstance().trockRefreshMapList(chr, true, vip));
        } else if (type == 0x01) {
            if (!FieldLimit.CANNOTVIPROCK.check(chr.getMap().getFieldLimit())) {
                if (vip) {
                    chr.addVipTrockMap();
                } else {
                    chr.addTrockMap();
                }

                client.sendPacket(ChannelPacketCreator.getInstance().trockRefreshMapList(chr, false, vip));
            } else {
                chr.message("You may not save this map.");
            }
        }
    }
}
