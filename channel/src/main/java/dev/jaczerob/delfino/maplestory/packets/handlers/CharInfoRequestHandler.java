package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.MapObject;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class CharInfoRequestHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CHAR_INFO_REQUEST;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        packet.skip(4);
        int cid = packet.readInt();
        MapObject target = client.getPlayer().getMap().getMapObject(cid);
        if (target != null) {
            if (target instanceof Character player) {

                if (client.getPlayer().getId() != player.getId()) {
                    player.exportExcludedItems(client);
                }
                context.writeAndFlush(ChannelPacketCreator.getInstance().charInfo(player));
            }
        }
    }
}
