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
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

/*
 *
 * @author BubblesDev
 */
@Component
public final class ReportHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.REPORT;
    }

    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int type = packet.readByte(); //00 = Illegal program claim, 01 = Conversation claim
        String victim = packet.readString();
        int reason = packet.readByte();
        String description = packet.readString();
        if (type == 0) {
            if (client.getPlayer().getPossibleReports() > 0) {
                if (client.getPlayer().getMeso() > 299) {
                    client.getPlayer().decreaseReports();
                    client.getPlayer().gainMeso(-300, true);
                } else {
                    client.sendPacket(ChannelPacketCreator.getInstance().reportResponse((byte) 4));
                    return;
                }
            } else {
                client.sendPacket(ChannelPacketCreator.getInstance().reportResponse((byte) 2));
                return;
            }
            Server.getInstance().broadcastGMMessage(client.getWorld(), ChannelPacketCreator.getInstance().serverNotice(6, victim + " was reported for: " + description));
            addReport(client.getPlayer().getId(), Character.getIdByName(victim), 0, description, "");
        } else if (type == 1) {
            String chatlog = packet.readString();
            if (chatlog == null) {
                return;
            }
            if (client.getPlayer().getPossibleReports() > 0) {
                if (client.getPlayer().getMeso() > 299) {
                    client.getPlayer().decreaseReports();
                    client.getPlayer().gainMeso(-300, true);
                } else {
                    client.sendPacket(ChannelPacketCreator.getInstance().reportResponse((byte) 4));
                    return;
                }
            }
            Server.getInstance().broadcastGMMessage(client.getWorld(), ChannelPacketCreator.getInstance().serverNotice(6, victim + " was reported for: " + description));
            addReport(client.getPlayer().getId(), Character.getIdByName(victim), reason, description, chatlog);
        } else {
            Server.getInstance().broadcastGMMessage(client.getWorld(), ChannelPacketCreator.getInstance().serverNotice(6, client.getPlayer().getName() + " is probably packet editing. Got unknown report type, which is impossible."));
        }
    }

    private void addReport(int reporterid, int victimid, int reason, String description, String chatlog) {
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO reports (`reporttime`, `reporterid`, `victimid`, `reason`, `chatlog`, `description`) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            ps.setInt(2, reporterid);
            ps.setInt(3, victimid);
            ps.setInt(4, reason);
            ps.setString(5, chatlog);
            ps.setString(6, description);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
