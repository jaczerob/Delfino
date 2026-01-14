package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.events.gm.Coconut;
import dev.jaczerob.delfino.maplestory.server.events.gm.Coconuts;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class CoconutHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.COCONUT;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        /*CB 00 A6 00 06 01
         * A6 00 = coconut id
         * 06 01 = ?
         */
        int id = packet.readShort();
        MapleMap map = client.getPlayer().getMap();
        Coconut event = map.getCoconut();
        Coconuts nut = event.getCoconut(id);
        if (!nut.isHittable()) {
            return;
        }
        if (event == null) {
            return;
        }
        if (currentServerTime() < nut.getHitTime()) {
            return;
        }
        if (nut.getHits() > 2 && Math.random() < 0.4) {
            if (Math.random() < 0.01 && event.getStopped() > 0) {
                nut.setHittable(false);
                event.stopCoconut();
                map.broadcastMessage(ChannelPacketCreator.getInstance().hitCoconut(false, id, 1));
                return;
            }
            nut.setHittable(false); // for sure :)
            nut.resetHits(); // For next event (without restarts)
            if (Math.random() < 0.05 && event.getBombings() > 0) {
                map.broadcastMessage(ChannelPacketCreator.getInstance().hitCoconut(false, id, 2));
                event.bombCoconut();
            } else if (event.getFalling() > 0) {
                map.broadcastMessage(ChannelPacketCreator.getInstance().hitCoconut(false, id, 3));
                event.fallCoconut();
                if (client.getPlayer().getTeam() == 0) {
                    event.addMapleScore();
                    map.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(5, client.getPlayer().getName() + " of Team Maple knocks down a coconut."));
                } else {
                    event.addStoryScore();
                    map.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(5, client.getPlayer().getName() + " of Team Story knocks down a coconut."));
                }
                map.broadcastMessage(ChannelPacketCreator.getInstance().coconutScore(event.getMapleScore(), event.getStoryScore()));
            }
        } else {
            nut.hit();
            map.broadcastMessage(ChannelPacketCreator.getInstance().hitCoconut(false, id, 1));
        }
    }
}  
