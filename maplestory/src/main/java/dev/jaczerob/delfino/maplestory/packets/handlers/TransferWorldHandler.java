/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.sql.*;

/**
 * @author Ronan
 * @author Ubaware
 */
@Component
public final class TransferWorldHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.WORLD_TRANSFER;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        packet.readInt(); //cid
        int birthday = packet.readInt();
        if (!CashOperationHandler.checkBirthday(client, birthday)) {
            client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0xC4));
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }
        Character chr = client.getPlayer();
        if (!YamlConfig.config.server.ALLOW_CASHSHOP_WORLD_TRANSFER || Server.getInstance().getWorldsSize() <= 1) {
            client.sendPacket(ChannelPacketCreator.getInstance().sendWorldTransferRules(9, client));
            return;
        }
        int worldTransferError = chr.checkWorldTransferEligibility();
        if (worldTransferError != 0) {
            client.sendPacket(ChannelPacketCreator.getInstance().sendWorldTransferRules(worldTransferError, client));
            return;
        }
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("SELECT completionTime FROM worldtransfers WHERE characterid=?")) {
            ps.setInt(1, chr.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Timestamp completedTimestamp = rs.getTimestamp("completionTime");
                if (completedTimestamp == null) { //has pending world transfer
                    client.sendPacket(ChannelPacketCreator.getInstance().sendWorldTransferRules(6, client));
                    return;
                } else if (completedTimestamp.getTime() + YamlConfig.config.server.WORLD_TRANSFER_COOLDOWN > System.currentTimeMillis()) {
                    client.sendPacket(ChannelPacketCreator.getInstance().sendWorldTransferRules(7, client));
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        client.sendPacket(ChannelPacketCreator.getInstance().sendWorldTransferRules(0, client));
    }
}
