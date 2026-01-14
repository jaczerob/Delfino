package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.FamilyEntitlement;
import dev.jaczerob.delfino.maplestory.client.FamilyEntry;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteResult;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteResultType;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteType;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public class FamilySummonResponseHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.FAMILY_SUMMON_RESPONSE;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (!YamlConfig.config.server.USE_FAMILY_SYSTEM) {
            return;
        }
        packet.readString(); //family name
        boolean accept = packet.readByte() != 0;
        InviteResult inviteResult = InviteCoordinator.answerInvite(InviteType.FAMILY_SUMMON, client.getPlayer().getId(), client.getPlayer(), accept);
        if (inviteResult.result == InviteResultType.NOT_FOUND) {
            return;
        }
        Character inviter = inviteResult.from;
        FamilyEntry inviterEntry = inviter.getFamilyEntry();
        if (inviterEntry == null) {
            return;
        }
        MapleMap map = (MapleMap) inviteResult.params[0];
        if (accept && inviter.getMap() == map) { //cancel if inviter has changed maps
            client.getPlayer().changeMap(map, map.getPortal(0));
        } else {
            inviterEntry.refundEntitlement(FamilyEntitlement.SUMMON_FAMILY);
            inviterEntry.gainReputation(FamilyEntitlement.SUMMON_FAMILY.getRepCost(), false); //refund rep cost if declined
            inviter.sendPacket(ChannelPacketCreator.getInstance().getFamilyInfo(inviterEntry));
            inviter.dropMessage(5, client.getPlayer().getName() + " has denied the summon request.");
        }
    }

}
