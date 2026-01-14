package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.Family;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public class FamilyPreceptsHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CHANGE_FAMILY_MESSAGE;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Family family = client.getPlayer().getFamily();
        if (family == null) {
            return;
        }
        if (family.getLeader().getChr() != client.getPlayer()) {
            return; //only the leader can set the precepts
        }
        String newPrecepts = packet.readString();
        if (newPrecepts.length() > 200) {
            return;
        }
        family.setMessage(newPrecepts, true);
        //family.broadcastFamilyInfoUpdate(); //probably don't need to broadcast for this?
        client.sendPacket(ChannelPacketCreator.getInstance().getFamilyInfo(client.getPlayer().getFamilyEntry()));
    }

}
