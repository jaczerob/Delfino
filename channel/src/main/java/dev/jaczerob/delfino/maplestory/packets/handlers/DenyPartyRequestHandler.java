package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteResultType;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteType;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class DenyPartyRequestHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.DENY_PARTY_REQUEST;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        packet.readByte();
        String[] cname = packet.readString().split("PS: ");

        Character cfrom = client.getChannelServer().getPlayerStorage().getCharacterByName(cname[cname.length - 1]);
        if (cfrom != null) {
            Character chr = client.getPlayer();

            if (InviteCoordinator.answerInvite(InviteType.PARTY, chr.getId(), cfrom.getPartyId(), false).result == InviteResultType.DENIED) {
                chr.updatePartySearchAvailability(chr.getParty() == null);
                cfrom.sendPacket(ChannelPacketCreator.getInstance().partyStatusMessage(23, chr.getName()));
            }
        }
    }
}
