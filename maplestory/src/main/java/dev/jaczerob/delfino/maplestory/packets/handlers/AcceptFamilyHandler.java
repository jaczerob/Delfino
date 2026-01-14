package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.Family;
import dev.jaczerob.delfino.maplestory.client.FamilyEntry;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteResult;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteResultType;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteType;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Jay Estrella
 * @author Ubaware
 */
@Component
public final class AcceptFamilyHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(AcceptFamilyHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.ACCEPT_FAMILY;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (!YamlConfig.config.server.USE_FAMILY_SYSTEM) {
            return;
        }
        Character chr = client.getPlayer();
        int inviterId = packet.readInt();
        packet.readString();
        boolean accept = packet.readByte() != 0;
        // String inviterName = slea.readMapleAsciiString();
        Character inviter = client.getWorldServer().getPlayerStorage().getCharacterById(inviterId);
        if (inviter != null) {
            InviteResult inviteResult = InviteCoordinator.answerInvite(InviteType.FAMILY, client.getPlayer().getId(), client.getPlayer(), accept);
            if (inviteResult.result == InviteResultType.NOT_FOUND) {
                return; //was never invited. (or expired on server only somehow?)
            }
            if (accept) {
                if (inviter.getFamily() != null) {
                    if (chr.getFamily() == null) {
                        FamilyEntry newEntry = new FamilyEntry(inviter.getFamily(), chr.getId(), chr.getName(), chr.getLevel(), chr.getJob());
                        newEntry.setCharacter(chr);
                        if (!newEntry.setSenior(inviter.getFamilyEntry(), true)) {
                            inviter.sendPacket(ChannelPacketCreator.getInstance().sendFamilyMessage(1, 0));
                            return;
                        } else {
                            // save
                            inviter.getFamily().addEntry(newEntry);
                            insertNewFamilyRecord(chr.getId(), inviter.getFamily().getID(), inviter.getId(), false);
                        }
                    } else { //absorb target family
                        FamilyEntry targetEntry = chr.getFamilyEntry();
                        Family targetFamily = targetEntry.getFamily();
                        if (targetFamily.getLeader() != targetEntry) {
                            return;
                        }
                        if (inviter.getFamily().getTotalGenerations() + targetFamily.getTotalGenerations() <= YamlConfig.config.server.FAMILY_MAX_GENERATIONS) {
                            targetEntry.join(inviter.getFamilyEntry());
                        } else {
                            inviter.sendPacket(ChannelPacketCreator.getInstance().sendFamilyMessage(76, 0));
                            chr.sendPacket(ChannelPacketCreator.getInstance().sendFamilyMessage(76, 0));
                            return;
                        }
                    }
                } else { // create new family
                    if (chr.getFamily() != null && inviter.getFamily() != null && chr.getFamily().getTotalGenerations() + inviter.getFamily().getTotalGenerations() >= YamlConfig.config.server.FAMILY_MAX_GENERATIONS) {
                        inviter.sendPacket(ChannelPacketCreator.getInstance().sendFamilyMessage(76, 0));
                        chr.sendPacket(ChannelPacketCreator.getInstance().sendFamilyMessage(76, 0));
                        return;
                    }
                    Family newFamily = new Family(-1, client.getWorld());
                    client.getWorldServer().addFamily(newFamily.getID(), newFamily);
                    FamilyEntry inviterEntry = new FamilyEntry(newFamily, inviter.getId(), inviter.getName(), inviter.getLevel(), inviter.getJob());
                    inviterEntry.setCharacter(inviter);
                    newFamily.setLeader(inviter.getFamilyEntry());
                    newFamily.addEntry(inviterEntry);
                    if (chr.getFamily() == null) { //completely new family
                        FamilyEntry newEntry = new FamilyEntry(newFamily, chr.getId(), chr.getName(), chr.getLevel(), chr.getJob());
                        newEntry.setCharacter(chr);
                        newEntry.setSenior(inviterEntry, true);
                        // save new family
                        insertNewFamilyRecord(inviter.getId(), newFamily.getID(), 0, true);
                        insertNewFamilyRecord(chr.getId(), newFamily.getID(), inviter.getId(), false); // char was already saved from setSenior() above
                        newFamily.setMessage("", true);
                    } else { //new family for inviter, absorb invitee family
                        insertNewFamilyRecord(inviter.getId(), newFamily.getID(), 0, true);
                        newFamily.setMessage("", true);
                        chr.getFamilyEntry().join(inviterEntry);
                    }
                }
                client.getPlayer().getFamily().broadcast(ChannelPacketCreator.getInstance().sendFamilyJoinResponse(true, client.getPlayer().getName()), client.getPlayer().getId());
                client.sendPacket(ChannelPacketCreator.getInstance().getSeniorMessage(inviter.getName()));
                client.sendPacket(ChannelPacketCreator.getInstance().getFamilyInfo(chr.getFamilyEntry()));
                chr.getFamilyEntry().updateSeniorFamilyInfo(true);
            } else {
                inviter.sendPacket(ChannelPacketCreator.getInstance().sendFamilyJoinResponse(false, client.getPlayer().getName()));
            }
        }
        client.sendPacket(ChannelPacketCreator.getInstance().sendFamilyMessage(0, 0));
    }

    private static void insertNewFamilyRecord(int characterID, int familyID, int seniorID, boolean updateChar) {
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO family_character (cid, familyid, seniorid) VALUES (?, ?, ?)")) {
                ps.setInt(1, characterID);
                ps.setInt(2, familyID);
                ps.setInt(3, seniorID);
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("Could not save new family record for chrId {}", characterID, e);
            }
            if (updateChar) {
                try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET familyid = ? WHERE id = ?")) {
                    ps.setInt(1, familyID);
                    ps.setInt(2, characterID);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    log.error("Could not update 'characters' 'familyid' record for chrId {}", characterID, e);
                }
            }
        } catch (SQLException e) {
            log.error("Could not get connection to DB while inserting new family record", e);
        }
    }
}
