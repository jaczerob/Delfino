package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteResult;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteResultType;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteType;
import dev.jaczerob.delfino.maplestory.net.server.world.Party;
import dev.jaczerob.delfino.maplestory.net.server.world.PartyCharacter;
import dev.jaczerob.delfino.maplestory.net.server.world.PartyOperation;
import dev.jaczerob.delfino.maplestory.net.server.world.World;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class PartyOperationHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.PARTY_OPERATION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int operation = packet.readByte();
        Character player = client.getPlayer();
        World world = client.getWorldServer();
        Party party = player.getParty();
        switch (operation) {
            case 1: { // create
                Party.createParty(player, false);
                break;
            }
            case 2: { // leave/disband
                if (party != null) {
                    List<Character> partymembers = player.getPartyMembersOnline();

                    Party.leaveParty(party, client);
                    player.updatePartySearchAvailability(true);
                    player.partyOperationUpdate(party, partymembers);
                }
                break;
            }
            case 3: { // join
                int partyid = packet.readInt();

                InviteResult inviteRes = InviteCoordinator.answerInvite(InviteType.PARTY, player.getId(), partyid, true);
                InviteResultType res = inviteRes.result;
                if (res == InviteResultType.ACCEPTED) {
                    Party.joinParty(player, partyid, false);
                } else {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(5, "You couldn't join the party due to an expired invitation request."));
                }
                break;
            }
            case 4: { // invite
                String name = packet.readString();
                Character invited = world.getPlayerStorage().getCharacterByName(name);
                if (invited != null) {
                    if (invited.getLevel() < 10 && (!YamlConfig.config.server.USE_PARTY_FOR_STARTERS || player.getLevel() >= 10)) { //min requirement is level 10
                        context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(5, "The player you have invited does not meet the requirements."));
                        return;
                    }
                    if (YamlConfig.config.server.USE_PARTY_FOR_STARTERS && invited.getLevel() >= 10 && player.getLevel() < 10) {    //trying to invite high level
                        context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(5, "The player you have invited does not meet the requirements."));
                        return;
                    }

                    if (invited.getParty() == null) {
                        if (party == null) {
                            if (!Party.createParty(player, false)) {
                                return;
                            }

                            party = player.getParty();
                        }
                        if (party.getMembers().size() < 6) {
                            if (InviteCoordinator.createInvite(InviteType.PARTY, player, party.getId(), invited.getId())) {
                                invited.sendPacket(ChannelPacketCreator.getInstance().partyInvite(player));
                            } else {
                                context.writeAndFlush(ChannelPacketCreator.getInstance().partyStatusMessage(22, invited.getName()));
                            }
                        } else {
                            context.writeAndFlush(ChannelPacketCreator.getInstance().partyStatusMessage(17));
                        }
                    } else {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().partyStatusMessage(16));
                    }
                } else {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().partyStatusMessage(19));
                }
                break;
            }
            case 5: { // expel
                int cid = packet.readInt();
                Party.expelFromParty(party, client, cid);
                break;
            }
            case 6: { // change leader
                int newLeader = packet.readInt();
                PartyCharacter newLeadr = party.getMemberById(newLeader);
                world.updateParty(party.getId(), PartyOperation.CHANGE_LEADER, newLeadr);
                break;
            }
        }
    }
}