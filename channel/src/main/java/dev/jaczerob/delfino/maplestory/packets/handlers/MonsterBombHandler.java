package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.constants.id.MobId;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class MonsterBombHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.MONSTER_BOMB;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int oid = packet.readInt();
        Monster monster = client.getPlayer().getMap().getMonsterByOid(oid);
        if (!client.getPlayer().isAlive() || monster == null) {
            return;
        }
        if (monster.getId() == MobId.HIGH_DARKSTAR || monster.getId() == MobId.LOW_DARKSTAR) {
            monster.getMap().broadcastMessage(ChannelPacketCreator.getInstance().killMonster(monster.getObjectId(), 4));
            client.getPlayer().getMap().removeMapObject(oid);
        }
    }
}
