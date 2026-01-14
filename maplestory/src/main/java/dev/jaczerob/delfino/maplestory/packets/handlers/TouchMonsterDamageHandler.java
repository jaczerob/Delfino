package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.BuffStat;
import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class TouchMonsterDamageHandler extends AbstractDealDamageHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.TOUCH_MONSTER_ATTACK;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        if (chr.getEnergyBar() == 15000 || chr.getBuffedValue(BuffStat.BODY_PRESSURE) != null) {
            applyAttack(parseDamage(packet, chr, false, false), client.getPlayer(), 1);
        }
    }
}
