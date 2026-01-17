package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.constants.id.MapId;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.matchchecker.MatchCheckerListenerFactory.MatchCheckerType;
import dev.jaczerob.delfino.maplestory.net.server.guild.Alliance;
import dev.jaczerob.delfino.maplestory.net.server.guild.Guild;
import dev.jaczerob.delfino.maplestory.net.server.guild.GuildPackets;
import dev.jaczerob.delfino.maplestory.net.server.guild.GuildResponse;
import dev.jaczerob.delfino.maplestory.net.server.world.Party;
import dev.jaczerob.delfino.maplestory.net.server.world.World;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public final class GuildOperationHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(GuildOperationHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.GUILD_OPERATION;
    }

    private boolean isGuildNameAcceptable(String name) {
        if (name.length() < 3 || name.length() > 12) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            if (!java.lang.Character.isLowerCase(name.charAt(i)) && !java.lang.Character.isUpperCase(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character mc = client.getPlayer();
        byte type = packet.readByte();
        int allianceId = -1;
        switch (type) {
            case 0x00:
                //context.writeAndFlush(PacketCreator.showGuildInfo(mc));
                break;
            case 0x02:
                if (mc.getGuildId() > 0) {
                    mc.dropMessage(1, "You cannot create a new Guild while in one.");
                    return;
                }
                if (mc.getMeso() < YamlConfig.config.server.CREATE_GUILD_COST) {
                    mc.dropMessage(1, "You do not have " + GameConstants.numberWithCommas(YamlConfig.config.server.CREATE_GUILD_COST) + " mesos to create a Guild.");
                    return;
                }
                String guildName = packet.readString();
                if (!isGuildNameAcceptable(guildName)) {
                    mc.dropMessage(1, "The Guild name you have chosen is not accepted.");
                    return;
                }

                Set<Character> eligibleMembers = new HashSet<>(Guild.getEligiblePlayersForGuild(mc));
                if (eligibleMembers.size() < YamlConfig.config.server.CREATE_GUILD_MIN_PARTNERS) {
                    if (mc.getMap().getAllPlayers().size() < YamlConfig.config.server.CREATE_GUILD_MIN_PARTNERS) {
                        // thanks NovaStory for noticing message in need of smoother info
                        mc.dropMessage(1, "Your Guild doesn't have enough cofounders present here and therefore cannot be created at this time.");
                    } else {
                        // players may be unaware of not belonging on a party in order to become eligible, thanks Hair (Legalize) for pointing this out
                        mc.dropMessage(1, "Please make sure everyone you are trying to invite is neither on a guild nor on a party.");
                    }

                    return;
                }

                if (!Party.createParty(mc, true)) {
                    mc.dropMessage(1, "You cannot create a new Guild while in a party.");
                    return;
                }

                Set<Integer> eligibleCids = new HashSet<>();
                for (Character chr : eligibleMembers) {
                    eligibleCids.add(chr.getId());
                }

                client.getWorldServer().getMatchCheckerCoordinator().createMatchConfirmation(MatchCheckerType.GUILD_CREATION, client.getWorld(), mc.getId(), eligibleCids, guildName);
                break;
            case 0x05:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() > 2) {
                    return;
                }

                String targetName = packet.readString();
                GuildResponse mgr = Guild.sendInvitation(client, targetName);
                if (mgr != null) {
                    context.writeAndFlush(mgr.getPacket(targetName));
                } else {
                } // already sent invitation, do nothing

                break;
            case 0x06:
                if (mc.getGuildId() > 0) {
                    log.warn("[Hack] Chr {} attempted to join a guild when s/he is already in one.", mc.getName());
                    return;
                }
                int gid = packet.readInt();
                int cid = packet.readInt();
                if (cid != mc.getId()) {
                    log.warn("[Hack] Chr {} attempted to join a guild with a different chrId", mc.getName());
                    return;
                }

                if (!Guild.answerInvitation(cid, mc.getName(), gid, true)) {
                    return;
                }

                mc.getMGC().setGuildId(gid); // joins the guild
                mc.getMGC().setGuildRank(5); // start at lowest rank
                mc.getMGC().setAllianceRank(5);

                int s = Server.getInstance().addGuildMember(mc.getMGC(), mc);
                if (s == 0) {
                    mc.dropMessage(1, "The guild you are trying to join is already full.");
                    mc.getMGC().setGuildId(0);
                    return;
                }

                context.writeAndFlush(GuildPackets.showGuildInfo(mc));

                allianceId = mc.getGuild().getAllianceId();
                if (allianceId > 0) {
                    Server.getInstance().getAlliance(allianceId).updateAlliancePackets(mc);
                }

                mc.saveGuildStatus(); // update database
                mc.getMap().broadcastPacket(mc, GuildPackets.guildNameChanged(mc.getId(), mc.getGuild().getName())); // thanks Vcoc for pointing out an issue with updating guild tooltip to players in the map
                mc.getMap().broadcastPacket(mc, GuildPackets.guildMarkChanged(mc.getId(), mc.getGuild()));
                break;
            case 0x07:
                cid = packet.readInt();
                String name = packet.readString();
                if (cid != mc.getId() || !name.equals(mc.getName()) || mc.getGuildId() <= 0) {
                    log.warn("[Hack] Chr {} tried to quit guild under the name {} and current guild id of {}", mc.getName(), name, mc.getGuildId());
                    return;
                }

                allianceId = mc.getGuild().getAllianceId();

                context.writeAndFlush(GuildPackets.updateGP(mc.getGuildId(), 0));
                Server.getInstance().leaveGuild(mc.getMGC());

                context.writeAndFlush(GuildPackets.showGuildInfo(null));
                if (allianceId > 0) {
                    Server.getInstance().getAlliance(allianceId).updateAlliancePackets(mc);
                }

                mc.getMGC().setGuildId(0);
                mc.getMGC().setGuildRank(5);
                mc.saveGuildStatus();
                mc.getMap().broadcastPacket(mc, GuildPackets.guildNameChanged(mc.getId(), ""));
                break;
            case 0x08:
                allianceId = mc.getGuild().getAllianceId();

                cid = packet.readInt();
                name = packet.readString();
                if (mc.getGuildRank() > 2 || mc.getGuildId() <= 0) {
                    log.warn("[Hack] Chr {} is trying to expel without rank 1 or 2", mc.getName());
                    return;
                }

                Server.getInstance().expelMember(mc.getMGC(), name, cid);
                if (allianceId > 0) {
                    Server.getInstance().getAlliance(allianceId).updateAlliancePackets(mc);
                }
                break;
            case 0x0d:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() != 1) {
                    log.warn("[Hack] Chr {} tried to change guild rank titles when s/he does not have permission", mc.getName());
                    return;
                }
                String[] ranks = new String[5];
                for (int i = 0; i < 5; i++) {
                    ranks[i] = packet.readString();
                }

                Server.getInstance().changeRankTitle(mc.getGuildId(), ranks);
                break;
            case 0x0e:
                cid = packet.readInt();
                byte newRank = packet.readByte();
                if (mc.getGuildRank() > 2 || (newRank <= 2 && mc.getGuildRank() != 1) || mc.getGuildId() <= 0) {
                    log.warn("[Hack] Chr {} is trying to change rank outside of his/her permissions.", mc.getName());
                    return;
                }
                if (newRank <= 1 || newRank > 5) {
                    return;
                }
                Server.getInstance().changeRank(mc.getGuildId(), cid, newRank);
                break;
            case 0x0f:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() != 1 || mc.getMapId() != MapId.GUILD_HQ) {
                    log.warn("[Hack] Chr {} tried to change guild emblem without being the guild leader", mc.getName());
                    return;
                }
                if (mc.getMeso() < YamlConfig.config.server.CHANGE_EMBLEM_COST) {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "You do not have " + GameConstants.numberWithCommas(YamlConfig.config.server.CHANGE_EMBLEM_COST) + " mesos to change the Guild emblem."));
                    return;
                }
                short bg = packet.readShort();
                byte bgcolor = packet.readByte();
                short logo = packet.readShort();
                byte logocolor = packet.readByte();
                Server.getInstance().setGuildEmblem(mc.getGuildId(), bg, bgcolor, logo, logocolor);

                if (mc.getGuild() != null && mc.getGuild().getAllianceId() > 0) {
                    Alliance alliance = mc.getAlliance();
                    Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.getGuildAlliances(alliance, client.getWorld()), -1, -1);
                }

                mc.gainMeso(-YamlConfig.config.server.CHANGE_EMBLEM_COST, true, false, true);
                mc.getGuild().broadcastNameChanged();
                mc.getGuild().broadcastEmblemChanged();
                break;
            case 0x10:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() > 2) {
                    if (mc.getGuildId() <= 0) {
                        log.warn("[Hack] Chr {} tried to change guild notice while not in a guild", mc.getName());
                    }
                    return;
                }
                String notice = packet.readString();
                if (notice.length() > 100) {
                    return;
                }
                Server.getInstance().setGuildNotice(mc.getGuildId(), notice);
                break;
            case 0x1E:
                packet.readInt();
                World wserv = client.getWorldServer();

                if (mc.getParty() != null) {
                    wserv.getMatchCheckerCoordinator().dismissMatchConfirmation(mc.getId());
                    return;
                }

                int leaderid = wserv.getMatchCheckerCoordinator().getMatchConfirmationLeaderid(mc.getId());
                if (leaderid != -1) {
                    boolean result = packet.readByte() != 0;
                    if (result && wserv.getMatchCheckerCoordinator().isMatchConfirmationActive(mc.getId())) {
                        Character leader = wserv.getPlayerStorage().getCharacterById(leaderid);
                        if (leader != null) {
                            int partyid = leader.getPartyId();
                            if (partyid != -1) {
                                Party.joinParty(mc, partyid, true);    // GMS gimmick "party to form guild" recalled thanks to Vcoc
                            }
                        }
                    }

                    wserv.getMatchCheckerCoordinator().answerMatchConfirmation(mc.getId(), result);
                }

                break;
            default:
                log.warn("Unhandled GUILD_OPERATION packet: {}", packet);
        }
    }
}
