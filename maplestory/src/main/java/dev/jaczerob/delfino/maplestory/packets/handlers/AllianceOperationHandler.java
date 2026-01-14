/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.net.server.guild.Alliance;
import dev.jaczerob.delfino.maplestory.net.server.guild.Guild;
import dev.jaczerob.delfino.maplestory.net.server.guild.GuildCharacter;
import dev.jaczerob.delfino.maplestory.net.server.guild.GuildPackets;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author XoticStory, Ronan
 */
@Component
public final class AllianceOperationHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.ALLIANCE_OPERATION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Alliance alliance = null;
        Character chr = client.getPlayer();

        if (chr.getGuild() == null) {
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        if (chr.getGuild().getAllianceId() > 0) {
            alliance = chr.getAlliance();
        }

        byte b = packet.readByte();
        if (alliance == null) {
            if (b != 4) {
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                return;
            }
        } else {
            if (b == 4) {
                chr.dropMessage(5, "Your guild is already registered on a guild alliance.");
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                return;
            }

            if (chr.getMGC().getAllianceRank() > 2 || !alliance.getGuilds().contains(chr.getGuildId())) {
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                return;
            }
        }

        // "alliance" is only null at case 0x04
        switch (b) {
            case 0x01:
                Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.sendShowInfo(chr.getGuild().getAllianceId(), chr.getId()), -1, -1);
                break;
            case 0x02: { // Leave Alliance
                if (chr.getGuild().getAllianceId() == 0 || chr.getGuildId() < 1 || chr.getGuildRank() != 1) {
                    return;
                }

                Alliance.removeGuildFromAlliance(chr.getGuild().getAllianceId(), chr.getGuildId(), chr.getWorld());
                break;
            }
            case 0x03: // Send Invite
                String guildName = packet.readString();

                if (alliance.getGuilds().size() == alliance.getCapacity()) {
                    chr.dropMessage(5, "Your alliance cannot comport any more guilds at the moment.");
                } else {
                    Alliance.sendInvitation(client, guildName, alliance.getId());
                }

                break;
            case 0x04: { // Accept Invite
                Guild guild = chr.getGuild();
                if (guild.getAllianceId() != 0 || chr.getGuildRank() != 1 || chr.getGuildId() < 1) {
                    return;
                }

                int allianceid = packet.readInt();
                //slea.readMapleAsciiString();  //recruiter's guild name

                alliance = Server.getInstance().getAlliance(allianceid);
                if (alliance == null) {
                    return;
                }

                if (!Alliance.answerInvitation(client.getPlayer().getId(), guild.getName(), alliance.getId(), true)) {
                    return;
                }

                if (alliance.getGuilds().size() == alliance.getCapacity()) {
                    chr.dropMessage(5, "Your alliance cannot comport any more guilds at the moment.");
                    return;
                }

                int guildid = chr.getGuildId();

                Server.getInstance().addGuildtoAlliance(alliance.getId(), guildid);
                Server.getInstance().resetAllianceGuildPlayersRank(guildid);

                chr.getMGC().setAllianceRank(2);
                Guild g = Server.getInstance().getGuild(chr.getGuildId());
                if (g != null) {
                    g.getMGC(chr.getId()).setAllianceRank(2);
                }

                chr.saveGuildStatus();

                Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.addGuildToAlliance(alliance, guildid, client), -1, -1);
                Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.updateAllianceInfo(alliance, client.getWorld()), -1, -1);
                Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.allianceNotice(alliance.getId(), alliance.getNotice()), -1, -1);
                guild.dropMessage("Your guild has joined the [" + alliance.getName() + "] union.");

                break;
            }
            case 0x06: { // Expel Guild
                int guildid = packet.readInt();
                int allianceid = packet.readInt();
                if (chr.getGuild().getAllianceId() == 0 || chr.getGuild().getAllianceId() != allianceid) {
                    return;
                }

                Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.removeGuildFromAlliance(alliance, guildid, client.getWorld()), -1, -1);
                Server.getInstance().removeGuildFromAlliance(alliance.getId(), guildid);

                Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.getGuildAlliances(alliance, client.getWorld()), -1, -1);
                Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.allianceNotice(alliance.getId(), alliance.getNotice()), -1, -1);
                Server.getInstance().guildMessage(guildid, GuildPackets.disbandAlliance(allianceid));

                alliance.dropMessage("[" + Server.getInstance().getGuild(guildid).getName() + "] guild has been expelled from the union.");
                break;
            }
            case 0x07: { // Change Alliance Leader
                if (chr.getGuild().getAllianceId() == 0 || chr.getGuildId() < 1) {
                    return;
                }
                int victimid = packet.readInt();
                Character player = Server.getInstance().getWorld(client.getWorld()).getPlayerStorage().getCharacterById(victimid);
                if (player.getAllianceRank() != 2) {
                    return;
                }

                //Server.getInstance().allianceMessage(alliance.getId(), sendChangeLeader(chr.getGuild().getAllianceId(), chr.getId(), slea.readInt()), -1, -1);
                changeLeaderAllianceRank(alliance, player);
                break;
            }
            case 0x08:
                String[] ranks = new String[5];
                for (int i = 0; i < 5; i++) {
                    ranks[i] = packet.readString();
                }
                Server.getInstance().setAllianceRanks(alliance.getId(), ranks);
                Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.changeAllianceRankTitle(alliance.getId(), ranks), -1, -1);
                break;
            case 0x09: {
                int int1 = packet.readInt();
                byte byte1 = packet.readByte();

                //Server.getInstance().allianceMessage(alliance.getId(), sendChangeRank(chr.getGuild().getAllianceId(), chr.getId(), int1, byte1), -1, -1);
                Character player = Server.getInstance().getWorld(client.getWorld()).getPlayerStorage().getCharacterById(int1);
                changePlayerAllianceRank(alliance, player, (byte1 > 0));

                break;
            }
            case 0x0A:
                String notice = packet.readString();
                Server.getInstance().setAllianceNotice(alliance.getId(), notice);
                Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.allianceNotice(alliance.getId(), notice), -1, -1);

                alliance.dropMessage(5, "* Alliance Notice : " + notice);
                break;
            default:
                chr.dropMessage("Feature not available");
        }

        alliance.saveToDB();
    }

    private void changeLeaderAllianceRank(Alliance alliance, Character newLeader) {
        GuildCharacter lmgc = alliance.getLeader();
        Character leader = newLeader.getWorldServer().getPlayerStorage().getCharacterById(lmgc.getId());
        leader.getMGC().setAllianceRank(2);
        leader.saveGuildStatus();

        newLeader.getMGC().setAllianceRank(1);
        newLeader.saveGuildStatus();

        Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.getGuildAlliances(alliance, newLeader.getWorld()), -1, -1);
        alliance.dropMessage("'" + newLeader.getName() + "' has been appointed as the new head of this Alliance.");
    }

    private void changePlayerAllianceRank(Alliance alliance, Character chr, boolean raise) {
        int newRank = chr.getAllianceRank() + (raise ? -1 : 1);
        if (newRank < 3 || newRank > 5) {
            return;
        }

        chr.getMGC().setAllianceRank(newRank);
        chr.saveGuildStatus();

        Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.getGuildAlliances(alliance, chr.getWorld()), -1, -1);
        alliance.dropMessage("'" + chr.getName() + "' has been reassigned to '" + alliance.getRankTitle(newRank) + "' in this Alliance.");
    }

}
