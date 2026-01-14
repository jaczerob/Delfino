package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.Reactor;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Lerk
 */
@Component
public final class ReactorHitHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.DAMAGE_REACTOR;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        //System.out.println(slea); //To see if there are any differences with packets
        //[CD 00] [6B 00 00 00] [01 00 00 00] [03 00] [00 00 20 03] [F7 03 00 00]
        //[CD 00] [66 00 00 00] [00 00 00 00] [02 00] [00 00 19 01] [00 00 00 00]
        int oid = packet.readInt();
        int charPos = packet.readInt();
        short stance = packet.readShort();
        packet.skip(4);
        int skillid = packet.readInt();
        Reactor reactor = client.getPlayer().getMap().getReactorByOid(oid);
        if (reactor != null) {
            reactor.hitReactor(true, charPos, stance, skillid, client);
        }
    }
}
