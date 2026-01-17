package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.FamilyEntitlement;
import dev.jaczerob.delfino.maplestory.client.FamilyEntry;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.id.MapId;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteType;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.FieldLimit;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Moogra
 * @author Ubaware
 */
@Component
public final class FamilyUseHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.USE_FAMILY;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (!YamlConfig.config.server.USE_FAMILY_SYSTEM) {
            return;
        }
        FamilyEntitlement type = FamilyEntitlement.values()[packet.readInt()];
        int cost = type.getRepCost();
        FamilyEntry entry = client.getPlayer().getFamilyEntry();
        if (entry.getReputation() < cost || entry.isEntitlementUsed(type)) {
            return; // shouldn't even be able to request it
        }
        context.writeAndFlush(ChannelPacketCreator.getInstance().getFamilyInfo(entry));
        Character victim;
        if (type == FamilyEntitlement.FAMILY_REUINION || type == FamilyEntitlement.SUMMON_FAMILY) {
            victim = client.getChannelServer().getPlayerStorage().getCharacterByName(packet.readString());
            if (victim != null && victim != client.getPlayer()) {
                if (victim.getFamily() == client.getPlayer().getFamily()) {
                    MapleMap targetMap = victim.getMap();
                    MapleMap ownMap = client.getPlayer().getMap();
                    if (targetMap != null) {
                        if (type == FamilyEntitlement.FAMILY_REUINION) {
                            if (!FieldLimit.CANNOTMIGRATE.check(ownMap.getFieldLimit()) && !FieldLimit.CANNOTVIPROCK.check(targetMap.getFieldLimit())
                                    && (targetMap.getForcedReturnId() == MapId.NONE || MapId.isMapleIsland(targetMap.getId()))) {

                                client.getPlayer().changeMap(victim.getMap(), victim.getMap().getPortal(0));
                                useEntitlement(entry, type);
                            } else {
                                context.writeAndFlush(ChannelPacketCreator.getInstance().sendFamilyMessage(75, 0)); // wrong message, but close enough. (client should check this first anyway)
                                return;
                            }
                        } else {
                            if (!FieldLimit.CANNOTMIGRATE.check(targetMap.getFieldLimit()) && !FieldLimit.CANNOTVIPROCK.check(ownMap.getFieldLimit())
                                    && (ownMap.getForcedReturnId() == MapId.NONE || MapId.isMapleIsland(ownMap.getId()))) {

                                if (InviteCoordinator.hasInvite(InviteType.FAMILY_SUMMON, victim.getId())) {
                                    context.writeAndFlush(ChannelPacketCreator.getInstance().sendFamilyMessage(74, 0));
                                    return;
                                }
                                InviteCoordinator.createInvite(InviteType.FAMILY_SUMMON, client.getPlayer(), victim, victim.getId(), client.getPlayer().getMap());
                                victim.sendPacket(ChannelPacketCreator.getInstance().sendFamilySummonRequest(client.getPlayer().getFamily().getName(), client.getPlayer().getName()));
                                useEntitlement(entry, type);
                            } else {
                                context.writeAndFlush(ChannelPacketCreator.getInstance().sendFamilyMessage(75, 0));
                                return;
                            }
                        }
                    }
                } else {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().sendFamilyMessage(67, 0));
                }
            }
        } else if (type == FamilyEntitlement.FAMILY_BONDING) {
            //not implemented
        } else {
            boolean party = false;
            boolean isExp = false;
            float rate = 1.5f;
            int duration = 15;
            do {
                switch (type) {
                    case PARTY_EXP_2_30MIN:
                        party = true;
                        isExp = true;
                        type = FamilyEntitlement.SELF_EXP_2_30MIN;
                        continue;
                    case PARTY_DROP_2_30MIN:
                        party = true;
                        type = FamilyEntitlement.SELF_DROP_2_30MIN;
                        continue;
                    case SELF_DROP_2_30MIN:
                        duration = 30;
                    case SELF_DROP_2:
                        rate = 2.0f;
                    case SELF_DROP_1_5:
                        break;
                    case SELF_EXP_2_30MIN:
                        duration = 30;
                    case SELF_EXP_2:
                        rate = 2.0f;
                    case SELF_EXP_1_5:
                        isExp = true;
                    default:
                        break;
                }
                break;
            } while (true);
            //not implemented
        }
    }

    private boolean useEntitlement(FamilyEntry entry, FamilyEntitlement entitlement) {
        if (entry.useEntitlement(entitlement)) {
            entry.gainReputation(-entitlement.getRepCost(), false);
            entry.getChr().sendPacket(ChannelPacketCreator.getInstance().getFamilyInfo(entry));
            return true;
        }
        return false;
    }
}
