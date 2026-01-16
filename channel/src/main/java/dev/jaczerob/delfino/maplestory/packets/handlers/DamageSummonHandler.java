package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.BuffStat;
import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.MapObject;
import dev.jaczerob.delfino.maplestory.server.maps.Summon;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class DamageSummonHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.DAMAGE_SUMMON;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int oid = packet.readInt();
        packet.skip(1);   // -1
        int damage = packet.readInt();
        int monsterIdFrom = packet.readInt();

        Character player = client.getPlayer();
        MapObject mmo = player.getMap().getMapObject(oid);

        if (mmo != null && mmo instanceof Summon) {
            Summon summon = (Summon) mmo;

            summon.addHP(-damage);
            if (summon.getHP() <= 0) {
                player.cancelEffectFromBuffStat(BuffStat.PUPPET);
            }
            player.getMap().broadcastMessage(player, ChannelPacketCreator.getInstance().damageSummon(player.getId(), oid, damage, monsterIdFrom), summon.getPosition());
        }
    }
}
