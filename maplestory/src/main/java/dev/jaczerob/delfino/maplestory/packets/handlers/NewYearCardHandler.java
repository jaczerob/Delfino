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
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.newyear.NewYearCardRecord;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
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
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Ronan
 * <packet>
 * Header layout thanks to Eric
 */
@Component
public final class NewYearCardHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.NEW_YEAR_CARD_REQUEST;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        final Character player = client.getPlayer();
        byte reqMode = packet.readByte();                 //[00] -> NewYearReq (0 = Send)

        if (reqMode == 0) {  // card has been sent
            if (player.haveItem(ItemId.NEW_YEARS_CARD)) {  // new year's card
                short slot = packet.readShort();                      //[00 2C] -> nPOS (Item Slot Pos)
                int itemid = packet.readInt();                        //[00 20 F5 E5] -> nItemID (item id)

                int status = getValidNewYearCardStatus(itemid, player, slot);
                if (status == 0) {
                    if (player.canHold(ItemId.NEW_YEARS_CARD_SEND, 1)) {
                        String receiver = packet.readString();  //[04 00 54 65 73 74] -> sReceiverName (person to send to)

                        int receiverid = getReceiverId(receiver, client.getWorld());
                        if (receiverid != -1) {
                            if (receiverid != client.getPlayer().getId()) {
                                String message = packet.readString();   //[06 00 4C 65 74 74 65 72] -> sContent (message)

                                NewYearCardRecord newyear = new NewYearCardRecord(player.getId(), player.getName(), receiverid, receiver, message);
                                NewYearCardRecord.saveNewYearCard(newyear);
                                player.addNewYearRecord(newyear);

                                player.getAbstractPlayerInteraction().gainItem(ItemId.NEW_YEARS_CARD, (short) -1);
                                player.getAbstractPlayerInteraction().gainItem(ItemId.NEW_YEARS_CARD_SEND, (short) 1);

                                Server.getInstance().setNewYearCard(newyear);
                                newyear.startNewYearCardTask();
                                player.sendPacket(ChannelPacketCreator.getInstance().onNewYearCardRes(player, newyear, 4, 0));    // successfully sent
                            } else {
                                player.sendPacket(ChannelPacketCreator.getInstance().onNewYearCardRes(player, -1, 5, 0xF));   // cannot send to yourself
                            }
                        } else {
                            player.sendPacket(ChannelPacketCreator.getInstance().onNewYearCardRes(player, -1, 5, 0x13));  // cannot find such character
                        }
                    } else {
                        player.sendPacket(ChannelPacketCreator.getInstance().onNewYearCardRes(player, -1, 5, 0x10));  // inventory full
                    }
                } else {
                    player.sendPacket(ChannelPacketCreator.getInstance().onNewYearCardRes(player, -1, 5, status));  // item and inventory errors
                }
            } else {
                player.sendPacket(ChannelPacketCreator.getInstance().onNewYearCardRes(player, -1, 5, 0x11));  // have no card to send
            }
        } else {    //receiver accepted the card
            int cardid = packet.readInt();

            NewYearCardRecord newyear = NewYearCardRecord.loadNewYearCard(cardid);

            if (newyear != null && newyear.getReceiverId() == player.getId() && !newyear.isReceiverCardReceived()) {
                if (!newyear.isSenderCardDiscarded()) {
                    if (player.canHold(ItemId.NEW_YEARS_CARD_RECEIVED, 1)) {
                        newyear.stopNewYearCardTask();
                        NewYearCardRecord.updateNewYearCard(newyear);

                        player.getAbstractPlayerInteraction().gainItem(ItemId.NEW_YEARS_CARD_RECEIVED, (short) 1);
                        if (!newyear.getMessage().isEmpty()) {
                            player.dropMessage(6, "[New Year] " + newyear.getSenderName() + ": " + newyear.getMessage());
                        }

                        player.addNewYearRecord(newyear);
                        player.sendPacket(ChannelPacketCreator.getInstance().onNewYearCardRes(player, newyear, 6, 0));    // successfully rcvd

                        player.getMap().broadcastMessage(ChannelPacketCreator.getInstance().onNewYearCardRes(player, newyear, 0xD, 0));

                        Character sender = client.getWorldServer().getPlayerStorage().getCharacterById(newyear.getSenderId());
                        if (sender != null && sender.isLoggedinWorld()) {
                            sender.getMap().broadcastMessage(ChannelPacketCreator.getInstance().onNewYearCardRes(sender, newyear, 0xD, 0));
                            sender.dropMessage(6, "[New Year] Your addressee successfully received the New Year card.");
                        }
                    } else {
                        player.sendPacket(ChannelPacketCreator.getInstance().onNewYearCardRes(player, -1, 5, 0x10));  // inventory full
                    }
                } else {
                    player.dropMessage(6, "[New Year] The sender of the New Year card already dropped it. Nothing to receive.");
                }
            } else {
                if (newyear == null) {
                    player.dropMessage(6, "[New Year] The sender of the New Year card already dropped it. Nothing to receive.");
                }
            }
        }
    }

    private static int getReceiverId(String receiver, int world) {
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT id, world FROM characters WHERE name LIKE ?")) {
                ps.setString(1, receiver);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt("world") == world) {
                            return rs.getInt("id");
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return -1;
    }

    private static int getValidNewYearCardStatus(int itemid, Character player, short slot) {
        if (!ItemConstants.isNewYearCardUse(itemid)) {
            return 0x14;
        }

        Item it = player.getInventory(ItemConstants.getInventoryType(itemid)).getItem(slot);
        return (it != null && it.getItemId() == itemid) ? 0 : 0x12;
    }
}
