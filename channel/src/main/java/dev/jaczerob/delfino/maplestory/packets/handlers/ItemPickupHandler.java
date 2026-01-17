package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.MapObject;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public final class ItemPickupHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(ItemPickupHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.ITEM_PICKUP;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        packet.readInt(); //Timestamp
        packet.readByte();
        packet.readPos(); //cpos
        int oid = packet.readInt();
        Character chr = client.getPlayer();
        MapObject ob = chr.getMap().getMapObject(oid);
        if (ob == null) {
            return;
        }

        Point charPos = chr.getPosition();
        Point obPos = ob.getPosition();
        if (Math.abs(charPos.getX() - obPos.getX()) > 800 || Math.abs(charPos.getY() - obPos.getY()) > 600) {
            log.warn("Chr {} tried to pick up an item too far away. Mapid: {}, player pos: {}, object pos: {}",
                    client.getPlayer().getName(), chr.getMapId(), charPos, obPos);
            return;
        }

        chr.pickupItem(ob);
    }
}
