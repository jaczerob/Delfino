package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.processor.action.PetAutopotProcessor;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.server.StatEffect;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class PetAutoPotHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.PET_AUTO_POT;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        packet.readByte();
        packet.readLong();
        packet.readInt();
        short slot = packet.readShort();
        int itemId = packet.readInt();

        Character chr = client.getPlayer();
        StatEffect stat = ItemInformationProvider.getInstance().getItemEffect(itemId);
        if (stat.getHp() > 0 || stat.getHpRate() > 0.0) {
            float estimatedHp = ((float) chr.getHp()) / chr.getMaxHp();
            chr.setAutopotHpAlert(estimatedHp + 0.05f);
        }

        if (stat.getMp() > 0 || stat.getMpRate() > 0.0) {
            float estimatedMp = ((float) chr.getMp()) / chr.getMaxMp();
            chr.setAutopotMpAlert(estimatedMp + 0.05f);
        }

        PetAutopotProcessor.runAutopotAction(client, slot, itemId);
    }

}
