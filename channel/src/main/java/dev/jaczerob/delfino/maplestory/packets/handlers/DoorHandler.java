package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.DoorObject;
import dev.jaczerob.delfino.maplestory.server.maps.MapObject;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Matze
 */
@Component
public final class DoorHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.USE_DOOR;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int ownerid = packet.readInt();
        packet.readByte(); // specifies if backwarp or not, 1 town to target, 0 target to town

        Character chr = client.getPlayer();
        if (chr.isChangingMaps() || chr.isBanned()) {
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        for (MapObject obj : chr.getMap().getMapObjects()) {
            if (obj instanceof DoorObject door) {
                if (door.getOwnerId() == ownerid) {
                    door.warp(chr);
                    return;
                }
            }
        }

        client.sendPacket(ChannelPacketCreator.getInstance().blockedMessage(6));
        client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
    }
}
