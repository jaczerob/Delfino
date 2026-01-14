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
import dev.jaczerob.delfino.maplestory.client.inventory.Equip;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.network.packets.Packet;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.net.server.channel.Channel;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.CashShop;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.server.MTSItemInfo;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;
import dev.jaczerob.delfino.maplestory.tools.Pair;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public final class MTSHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(MTSHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.MTS_OPERATION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        // TODO add karma-to-untradeable flag on sold items here

        if (!client.getPlayer().getCashShop().isOpened()) {
            return;
        }
        if (packet.available() > 0) {
            byte op = packet.readByte();
            switch (op) {
                case 2: { //put item up for sale
                    byte itemtype = packet.readByte();
                    int itemid = packet.readInt();
                    packet.readShort();
                    packet.skip(7);
                    short stars = 1;
                    if (itemtype == 1) {
                        packet.skip(32);
                    } else {
                        stars = packet.readShort();
                    }
                    packet.readString(); // another useless thing (owner)
                    if (itemtype == 1) {
                        packet.skip(32);
                    } else {
                        packet.readShort();
                    }
                    short slot;
                    short quantity;
                    if (itemtype != 1) {
                        if (itemid / 10000 == 207 || itemid / 10000 == 233) {
                            packet.skip(8);
                        }
                        slot = (short) packet.readInt();
                    } else {
                        slot = (short) packet.readInt();
                    }
                    if (itemtype != 1) {
                        if (itemid / 10000 == 207 || itemid / 10000 == 233) {
                            quantity = stars;
                            packet.skip(4);
                        } else {
                            quantity = (short) packet.readInt();
                        }
                    } else {
                        quantity = (byte) packet.readInt();
                    }
                    int price = packet.readInt();
                    if (itemtype == 1) {
                        quantity = 1;
                    }
                    if (quantity < 0 || price < 110 || client.getPlayer().getItemQuantity(itemid, false) < quantity) {
                        return;
                    }
                    InventoryType invType = ItemConstants.getInventoryType(itemid);
                    Item i = client.getPlayer().getInventory(invType).getItem(slot).copy();
                    if (i != null && client.getPlayer().getMeso() >= 5000) {
                        try (Connection con = DatabaseConnection.getStaticConnection();
                             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mts_items WHERE seller = ?");) {
                            ps.setInt(1, client.getPlayer().getId());
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                if (rs.getInt(1) > 10) { // They have more than 10 items up for sale already!
                                    client.getPlayer().dropMessage(1, "You already have 10 items up for auction!");
                                    client.sendPacket(getMTS(1, 0, 0));
                                    client.sendPacket(ChannelPacketCreator.getInstance().transferInventory(getTransfer(client.getPlayer().getId())));
                                    client.sendPacket(ChannelPacketCreator.getInstance().notYetSoldInv(getNotYetSold(client.getPlayer().getId())));
                                    return;
                                }
                            }

                            LocalDate now = LocalDate.now();
                            LocalDate sellEnd = now.plusDays(7);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            String date = sellEnd.format(formatter);

                            if (!i.getInventoryType().equals(InventoryType.EQUIP)) {
                                Item item = i;
                                try (PreparedStatement pse = con.prepareStatement("INSERT INTO mts_items (tab, type, itemid, quantity, expiration, giftFrom, seller, price, owner, sellername, sell_ends) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                                    pse.setInt(1, 1);
                                    pse.setInt(2, invType.getType());
                                    pse.setInt(3, item.getItemId());
                                    pse.setInt(4, quantity);
                                    pse.setLong(5, item.getExpiration());
                                    pse.setString(6, item.getGiftFrom());
                                    pse.setInt(7, client.getPlayer().getId());
                                    pse.setInt(8, price);
                                    pse.setString(9, item.getOwner());
                                    pse.setString(10, client.getPlayer().getName());
                                    pse.setString(11, date);
                                    pse.executeUpdate();
                                }
                            } else {
                                Equip equip = (Equip) i;
                                try (PreparedStatement pse = con.prepareStatement("INSERT INTO mts_items (tab, type, itemid, quantity, expiration, giftFrom, seller, price, upgradeslots, level, str, dex, `int`, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, locked, owner, sellername, sell_ends, vicious, flag, itemexp, itemlevel, ringid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                                    pse.setInt(1, 1);
                                    pse.setInt(2, invType.getType());
                                    pse.setInt(3, equip.getItemId());
                                    pse.setInt(4, quantity);
                                    pse.setLong(5, equip.getExpiration());
                                    pse.setString(6, equip.getGiftFrom());
                                    pse.setInt(7, client.getPlayer().getId());
                                    pse.setInt(8, price);
                                    pse.setInt(9, equip.getUpgradeSlots());
                                    pse.setInt(10, equip.getLevel());
                                    pse.setInt(11, equip.getStr());
                                    pse.setInt(12, equip.getDex());
                                    pse.setInt(13, equip.getInt());
                                    pse.setInt(14, equip.getLuk());
                                    pse.setInt(15, equip.getHp());
                                    pse.setInt(16, equip.getMp());
                                    pse.setInt(17, equip.getWatk());
                                    pse.setInt(18, equip.getMatk());
                                    pse.setInt(19, equip.getWdef());
                                    pse.setInt(20, equip.getMdef());
                                    pse.setInt(21, equip.getAcc());
                                    pse.setInt(22, equip.getAvoid());
                                    pse.setInt(23, equip.getHands());
                                    pse.setInt(24, equip.getSpeed());
                                    pse.setInt(25, equip.getJump());
                                    pse.setInt(26, 0);
                                    pse.setString(27, equip.getOwner());
                                    pse.setString(28, client.getPlayer().getName());
                                    pse.setString(29, date);
                                    pse.setInt(30, equip.getVicious());
                                    pse.setInt(31, equip.getFlag());
                                    pse.setInt(32, equip.getItemExp());
                                    pse.setByte(33, equip.getItemLevel()); // thanks Jefe for noticing missing itemlevel labels
                                    pse.setInt(34, equip.getRingId());
                                    pse.executeUpdate();
                                }
                            }
                            InventoryManipulator.removeFromSlot(client, invType, slot, quantity, false);

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        client.getPlayer().gainMeso(-5000, false);
                        client.sendPacket(ChannelPacketCreator.getInstance().MTSConfirmSell());
                        client.sendPacket(getMTS(1, 0, 0));
                        client.enableCSActions();
                        client.sendPacket(ChannelPacketCreator.getInstance().transferInventory(getTransfer(client.getPlayer().getId())));
                        client.sendPacket(ChannelPacketCreator.getInstance().notYetSoldInv(getNotYetSold(client.getPlayer().getId())));
                    }
                    break;
                }
                case 3: //send offer for wanted item
                    break;
                case 4: //list wanted item
                    packet.readInt();
                    packet.readInt();
                    packet.readInt();
                    packet.readShort();
                    packet.readString();
                    break;
                case 5: { //change page
                    int tab = packet.readInt();
                    int type = packet.readInt();
                    int page = packet.readInt();
                    client.getPlayer().changePage(page);
                    if (tab == 4 && type == 0) {
                        client.sendPacket(getCart(client.getPlayer().getId()));
                    } else if (tab == client.getPlayer().getCurrentTab() && type == client.getPlayer().getCurrentType() && client.getPlayer().getSearch() != null) {
                        client.sendPacket(getMTSSearch(tab, type, client.getPlayer().getCurrentCI(), client.getPlayer().getSearch(), page));
                    } else {
                        client.getPlayer().setSearch(null);
                        client.sendPacket(getMTS(tab, type, page));
                    }
                    client.getPlayer().changeTab(tab);
                    client.getPlayer().changeType(type);
                    client.enableCSActions();
                    client.sendPacket(ChannelPacketCreator.getInstance().transferInventory(getTransfer(client.getPlayer().getId())));
                    client.sendPacket(ChannelPacketCreator.getInstance().notYetSoldInv(getNotYetSold(client.getPlayer().getId())));
                    break;
                }
                case 6: { //search
                    int tab = packet.readInt();
                    int type = packet.readInt();
                    packet.readInt();
                    int ci = packet.readInt();
                    String search = packet.readString();
                    client.getPlayer().setSearch(search);
                    client.getPlayer().changeTab(tab);
                    client.getPlayer().changeType(type);
                    client.getPlayer().changeCI(ci);
                    client.enableCSActions();
                    client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    client.sendPacket(getMTSSearch(tab, type, ci, search, client.getPlayer().getCurrentPage()));
                    client.sendPacket(ChannelPacketCreator.getInstance().showMTSCash(client.getPlayer()));
                    client.sendPacket(ChannelPacketCreator.getInstance().transferInventory(getTransfer(client.getPlayer().getId())));
                    client.sendPacket(ChannelPacketCreator.getInstance().notYetSoldInv(getNotYetSold(client.getPlayer().getId())));
                    break;
                }
                case 7: { //cancel sale
                    int id = packet.readInt(); // id of the item
                    try (Connection con = DatabaseConnection.getStaticConnection()) {
                        try (PreparedStatement ps = con.prepareStatement("UPDATE mts_items SET transfer = 1 WHERE id = ? AND seller = ?")) {
                            ps.setInt(1, id);
                            ps.setInt(2, client.getPlayer().getId());
                            ps.executeUpdate();
                        }

                        try (PreparedStatement ps = con.prepareStatement("DELETE FROM mts_cart WHERE itemid = ?")) {
                            ps.setInt(1, id);
                            ps.executeUpdate();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    client.enableCSActions();
                    client.sendPacket(getMTS(client.getPlayer().getCurrentTab(), client.getPlayer().getCurrentType(),
                            client.getPlayer().getCurrentPage()));
                    client.sendPacket(ChannelPacketCreator.getInstance().notYetSoldInv(getNotYetSold(client.getPlayer().getId())));
                    client.sendPacket(ChannelPacketCreator.getInstance().transferInventory(getTransfer(client.getPlayer().getId())));
                    break;
                }
                case 8: { // transfer item from transfer inv.
                    int id = packet.readInt(); // id of the item
                    try (Connection con = DatabaseConnection.getStaticConnection()) {
                        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_items WHERE seller = ? AND transfer = 1  AND id= ? ORDER BY id DESC")) {
                            ps.setInt(1, client.getPlayer().getId());
                            ps.setInt(2, id);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                Item i;
                                if (rs.getInt("type") != 1) {
                                    Item ii = new Item(rs.getInt("itemid"), (short) 0, (short) rs.getInt("quantity"));
                                    ii.setOwner(rs.getString("owner"));
                                    ii.setPosition(
                                            client.getPlayer().getInventory(ItemConstants.getInventoryType(rs.getInt("itemid")))
                                                    .getNextFreeSlot());
                                    i = ii.copy();
                                } else {
                                    Equip equip = new Equip(rs.getInt("itemid"), (byte) rs.getInt("position"), -1);
                                    equip.setOwner(rs.getString("owner"));
                                    equip.setQuantity((short) 1);
                                    equip.setAcc((short) rs.getInt("acc"));
                                    equip.setAvoid((short) rs.getInt("avoid"));
                                    equip.setDex((short) rs.getInt("dex"));
                                    equip.setHands((short) rs.getInt("hands"));
                                    equip.setHp((short) rs.getInt("hp"));
                                    equip.setInt((short) rs.getInt("int"));
                                    equip.setJump((short) rs.getInt("jump"));
                                    equip.setLuk((short) rs.getInt("luk"));
                                    equip.setMatk((short) rs.getInt("matk"));
                                    equip.setMdef((short) rs.getInt("mdef"));
                                    equip.setMp((short) rs.getInt("mp"));
                                    equip.setSpeed((short) rs.getInt("speed"));
                                    equip.setStr((short) rs.getInt("str"));
                                    equip.setWatk((short) rs.getInt("watk"));
                                    equip.setWdef((short) rs.getInt("wdef"));
                                    equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                                    equip.setLevel((byte) rs.getInt("level"));
                                    equip.setItemLevel(rs.getByte("itemlevel"));
                                    equip.setItemExp(rs.getInt("itemexp"));
                                    equip.setRingId(rs.getInt("ringid"));
                                    equip.setVicious((byte) rs.getInt("vicious"));
                                    equip.setFlag((short) rs.getInt("flag"));
                                    equip.setExpiration(rs.getLong("expiration"));
                                    equip.setGiftFrom(rs.getString("giftFrom"));
                                    equip.setPosition(
                                            client.getPlayer().getInventory(ItemConstants.getInventoryType(rs.getInt("itemid")))
                                                    .getNextFreeSlot());
                                    i = equip.copy();
                                }
                                try (PreparedStatement pse = con.prepareStatement(
                                        "DELETE FROM mts_items WHERE id = ? AND seller = ? AND transfer = 1")) {
                                    pse.setInt(1, id);
                                    pse.setInt(2, client.getPlayer().getId());
                                    pse.executeUpdate();
                                }
                                InventoryManipulator.addFromDrop(client, i, false);
                                client.enableCSActions();
                                client.sendPacket(getCart(client.getPlayer().getId()));
                                client.sendPacket(getMTS(client.getPlayer().getCurrentTab(), client.getPlayer().getCurrentType(),
                                        client.getPlayer().getCurrentPage()));
                                client.sendPacket(ChannelPacketCreator.getInstance().MTSConfirmTransfer(i.getQuantity(), i.getPosition()));
                                client.sendPacket(ChannelPacketCreator.getInstance().transferInventory(getTransfer(client.getPlayer().getId())));
                            }
                        }
                    } catch (SQLException e) {
                        log.error("MTS Transfer error", e);
                    }
                    break;
                }
                case 9: { //add to cart
                    int id = packet.readInt(); // id of the item
                    try (Connection con = DatabaseConnection.getStaticConnection()) {
                        try (PreparedStatement ps1 = con.prepareStatement("SELECT id FROM mts_items WHERE id = ? AND seller <> ?")) {
                            ps1.setInt(1, id); // Dummy query, prevents adding to cart self owned items
                            ps1.setInt(2, client.getPlayer().getId());
                            try (ResultSet rs1 = ps1.executeQuery()) {
                                if (rs1.next()) {
                                    PreparedStatement ps = con.prepareStatement("SELECT cid FROM mts_cart WHERE cid = ? AND itemid = ?");
                                    ps.setInt(1, client.getPlayer().getId());
                                    ps.setInt(2, id);
                                    try (ResultSet rs = ps.executeQuery()) {
                                        if (!rs.next()) {
                                            try (PreparedStatement pse = con.prepareStatement("INSERT INTO mts_cart (cid, itemid) VALUES (?, ?)")) {
                                                pse.setInt(1, client.getPlayer().getId());
                                                pse.setInt(2, id);
                                                pse.executeUpdate();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    client.sendPacket(getMTS(client.getPlayer().getCurrentTab(), client.getPlayer().getCurrentType(), client.getPlayer().getCurrentPage()));
                    client.enableCSActions();
                    client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    client.sendPacket(ChannelPacketCreator.getInstance().transferInventory(getTransfer(client.getPlayer().getId())));
                    client.sendPacket(ChannelPacketCreator.getInstance().notYetSoldInv(getNotYetSold(client.getPlayer().getId())));
                    break;
                }
                case 10: { //delete from cart
                    int id = packet.readInt(); // id of the item
                    try (Connection con = DatabaseConnection.getStaticConnection()) {
                        try (PreparedStatement ps = con.prepareStatement("DELETE FROM mts_cart WHERE itemid = ? AND cid = ?")) {
                            ps.setInt(1, id);
                            ps.setInt(2, client.getPlayer().getId());
                            ps.executeUpdate();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    client.sendPacket(getCart(client.getPlayer().getId()));
                    client.enableCSActions();
                    client.sendPacket(ChannelPacketCreator.getInstance().transferInventory(getTransfer(client.getPlayer().getId())));
                    client.sendPacket(ChannelPacketCreator.getInstance().notYetSoldInv(getNotYetSold(client.getPlayer().getId())));
                    break;
                }
                case 12: //put item up for auction
                    break;
                case 13: //cancel wanted cart thing
                    break;
                case 14: //buy auction item now
                    break;
                case 16: { //buy
                    int id = packet.readInt(); // id of the item
                    try (Connection con = DatabaseConnection.getStaticConnection();
                         PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_items WHERE id = ? ORDER BY id DESC")) {
                        ps.setInt(1, id);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            int price = rs.getInt("price") + 100 + (int) (rs.getInt("price") * 0.1); // taxes
                            if (client.getPlayer().getCashShop().getCash(CashShop.NX_PREPAID) >= price) { // FIX
                                boolean alwaysnull = true;
                                for (Channel cserv : Server.getInstance().getAllChannels()) {
                                    Character victim = cserv.getPlayerStorage().getCharacterById(rs.getInt("seller"));
                                    if (victim != null) {
                                        victim.getCashShop().gainCash(4, rs.getInt("price"));
                                        alwaysnull = false;
                                    }
                                }
                                if (alwaysnull) {
                                    try (PreparedStatement pse = con.prepareStatement("SELECT accountid FROM characters WHERE id = ?")) {
                                        pse.setInt(1, rs.getInt("seller"));
                                        ResultSet rse = pse.executeQuery();
                                        if (rse.next()) {
                                            try (PreparedStatement psee = con.prepareStatement("UPDATE accounts SET nxPrepaid = nxPrepaid + ? WHERE id = ?")) {
                                                psee.setInt(1, rs.getInt("price"));
                                                psee.setInt(2, rse.getInt("accountid"));
                                                psee.executeUpdate();
                                            }
                                        }
                                    }
                                }
                                try (PreparedStatement pse = con.prepareStatement("UPDATE mts_items SET seller = ?, transfer = 1 WHERE id = ?")) {
                                    pse.setInt(1, client.getPlayer().getId());
                                    pse.setInt(2, id);
                                    pse.executeUpdate();
                                }
                                try (PreparedStatement pse = con.prepareStatement("DELETE FROM mts_cart WHERE itemid = ?")) {
                                    pse.setInt(1, id);
                                    pse.executeUpdate();
                                }
                                client.getPlayer().getCashShop().gainCash(4, -price);
                                client.enableCSActions();
                                client.sendPacket(getMTS(client.getPlayer().getCurrentTab(), client.getPlayer().getCurrentType(), client.getPlayer().getCurrentPage()));
                                client.sendPacket(ChannelPacketCreator.getInstance().MTSConfirmBuy());
                                client.sendPacket(ChannelPacketCreator.getInstance().showMTSCash(client.getPlayer()));
                                client.sendPacket(ChannelPacketCreator.getInstance().transferInventory(getTransfer(client.getPlayer().getId())));
                                client.sendPacket(ChannelPacketCreator.getInstance().notYetSoldInv(getNotYetSold(client.getPlayer().getId())));
                                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                            } else {
                                client.sendPacket(ChannelPacketCreator.getInstance().MTSFailBuy());
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        client.sendPacket(ChannelPacketCreator.getInstance().MTSFailBuy());
                    }
                    break;
                }
                case 17: { //buy from cart
                    int id = packet.readInt(); // id of the item
                    try (Connection con = DatabaseConnection.getStaticConnection();
                         PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_items WHERE id = ? ORDER BY id DESC")) {
                        ps.setInt(1, id);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            int price = rs.getInt("price") + 100 + (int) (rs.getInt("price") * 0.1);
                            if (client.getPlayer().getCashShop().getCash(CashShop.NX_PREPAID) >= price) {
                                for (Channel cserv : Server.getInstance().getAllChannels()) {
                                    Character victim = cserv.getPlayerStorage().getCharacterById(rs.getInt("seller"));
                                    if (victim != null) {
                                        victim.getCashShop().gainCash(CashShop.NX_PREPAID, rs.getInt("price"));
                                    } else {
                                        try (PreparedStatement pse = con.prepareStatement("SELECT accountid FROM characters WHERE id = ?")) {
                                            pse.setInt(1, rs.getInt("seller"));
                                            ResultSet rse = pse.executeQuery();
                                            if (rse.next()) {
                                                try (PreparedStatement psee = con.prepareStatement("UPDATE accounts SET nxPrepaid = nxPrepaid + ? WHERE id = ?")) {
                                                    psee.setInt(1, rs.getInt("price"));
                                                    psee.setInt(2, rse.getInt("accountid"));
                                                    psee.executeUpdate();
                                                }
                                            }
                                        }
                                    }
                                }
                                try (PreparedStatement pse = con.prepareStatement("UPDATE mts_items SET seller = ?, transfer = 1 WHERE id = ?")) {
                                    pse.setInt(1, client.getPlayer().getId());
                                    pse.setInt(2, id);
                                    pse.executeUpdate();
                                }
                                try (PreparedStatement pse = con.prepareStatement("DELETE FROM mts_cart WHERE itemid = ?")) {
                                    pse.setInt(1, id);
                                    pse.executeUpdate();
                                }
                                client.getPlayer().getCashShop().gainCash(4, -price);
                                client.sendPacket(getCart(client.getPlayer().getId()));
                                client.enableCSActions();
                                client.sendPacket(ChannelPacketCreator.getInstance().MTSConfirmBuy());
                                client.sendPacket(ChannelPacketCreator.getInstance().showMTSCash(client.getPlayer()));
                                client.sendPacket(ChannelPacketCreator.getInstance().transferInventory(getTransfer(client.getPlayer().getId())));
                                client.sendPacket(ChannelPacketCreator.getInstance().notYetSoldInv(getNotYetSold(client.getPlayer().getId())));
                            } else {
                                client.sendPacket(ChannelPacketCreator.getInstance().MTSFailBuy());
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        client.sendPacket(ChannelPacketCreator.getInstance().MTSFailBuy());
                    }
                    break;
                }
                default:
                    log.warn("Unhandled OP (MTS): {}, packet: {}", op, packet);
                    break;
            }
        } else {
            client.sendPacket(ChannelPacketCreator.getInstance().showMTSCash(client.getPlayer()));
        }
    }

    public List<MTSItemInfo> getNotYetSold(int cid) {
        List<MTSItemInfo> items = new ArrayList<>();
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_items WHERE seller = ? AND transfer = 0 ORDER BY id DESC")) {
            ps.setInt(1, cid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("type") != 1) {
                    Item i = new Item(rs.getInt("itemid"), (byte) 0, (short) rs.getInt("quantity"));
                    i.setOwner(rs.getString("owner"));
                    items.add(new MTSItemInfo(i, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                } else {
                    Equip equip = new Equip(rs.getInt("itemid"), (byte) rs.getInt("position"), -1);
                    equip.setOwner(rs.getString("owner"));
                    equip.setQuantity((short) 1);
                    equip.setAcc((short) rs.getInt("acc"));
                    equip.setAvoid((short) rs.getInt("avoid"));
                    equip.setDex((short) rs.getInt("dex"));
                    equip.setHands((short) rs.getInt("hands"));
                    equip.setHp((short) rs.getInt("hp"));
                    equip.setInt((short) rs.getInt("int"));
                    equip.setJump((short) rs.getInt("jump"));
                    equip.setVicious((short) rs.getInt("vicious"));
                    equip.setLuk((short) rs.getInt("luk"));
                    equip.setMatk((short) rs.getInt("matk"));
                    equip.setMdef((short) rs.getInt("mdef"));
                    equip.setMp((short) rs.getInt("mp"));
                    equip.setSpeed((short) rs.getInt("speed"));
                    equip.setStr((short) rs.getInt("str"));
                    equip.setWatk((short) rs.getInt("watk"));
                    equip.setWdef((short) rs.getInt("wdef"));
                    equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                    equip.setLevel((byte) rs.getInt("level"));
                    equip.setFlag((short) rs.getInt("flag"));
                    equip.setItemLevel(rs.getByte("itemlevel"));
                    equip.setItemExp(rs.getInt("itemexp"));
                    equip.setRingId(rs.getInt("ringid"));
                    equip.setExpiration(rs.getLong("expiration"));
                    equip.setGiftFrom(rs.getString("giftFrom"));
                    items.add(new MTSItemInfo(equip, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public Packet getCart(int cid) {
        List<MTSItemInfo> items = new ArrayList<>();
        int pages = 0;
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_cart WHERE cid = ? ORDER BY id DESC")) {
                ps.setInt(1, cid);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    try (PreparedStatement pse = con.prepareStatement("SELECT * FROM mts_items WHERE id = ?")) {
                        pse.setInt(1, rs.getInt("itemid"));
                        ResultSet rse = pse.executeQuery();
                        if (rse.next()) {
                            if (rse.getInt("type") != 1) {
                                Item i = new Item(rse.getInt("itemid"), (short) 0, (short) rse.getInt("quantity"));
                                i.setOwner(rse.getString("owner"));
                                items.add(new MTSItemInfo(i, rse.getInt("price"), rse.getInt("id"),
                                        rse.getInt("seller"), rse.getString("sellername"), rse.getString("sell_ends")));
                            } else {
                                Equip equip = new Equip(rse.getInt("itemid"), (byte) rse.getInt("position"), -1);
                                equip.setOwner(rse.getString("owner"));
                                equip.setQuantity((short) 1);
                                equip.setAcc((short) rse.getInt("acc"));
                                equip.setAvoid((short) rse.getInt("avoid"));
                                equip.setDex((short) rse.getInt("dex"));
                                equip.setHands((short) rse.getInt("hands"));
                                equip.setHp((short) rse.getInt("hp"));
                                equip.setInt((short) rse.getInt("int"));
                                equip.setJump((short) rse.getInt("jump"));
                                equip.setVicious((short) rse.getInt("vicious"));
                                equip.setLuk((short) rse.getInt("luk"));
                                equip.setMatk((short) rse.getInt("matk"));
                                equip.setMdef((short) rse.getInt("mdef"));
                                equip.setMp((short) rse.getInt("mp"));
                                equip.setSpeed((short) rse.getInt("speed"));
                                equip.setStr((short) rse.getInt("str"));
                                equip.setWatk((short) rse.getInt("watk"));
                                equip.setWdef((short) rse.getInt("wdef"));
                                equip.setUpgradeSlots((byte) rse.getInt("upgradeslots"));
                                equip.setLevel((byte) rse.getInt("level"));
                                equip.setItemLevel(rs.getByte("itemlevel"));
                                equip.setItemExp(rs.getInt("itemexp"));
                                equip.setRingId(rs.getInt("ringid"));
                                equip.setFlag((short) rs.getInt("flag"));
                                equip.setExpiration(rs.getLong("expiration"));
                                equip.setGiftFrom(rs.getString("giftFrom"));
                                items.add(new MTSItemInfo(equip, rse.getInt("price"), rse.getInt("id"),
                                        rse.getInt("seller"), rse.getString("sellername"), rse.getString("sell_ends")));
                            }
                        }
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mts_cart WHERE cid = ?")) {
                ps.setInt(1, cid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    pages = rs.getInt(1) / 16;
                    if (rs.getInt(1) % 16 > 0) {
                        pages += 1;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ChannelPacketCreator.getInstance().sendMTS(items, 4, 0, 0, pages);
    }

    public List<MTSItemInfo> getTransfer(int cid) {
        List<MTSItemInfo> items = new ArrayList<>();
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_items WHERE transfer = 1 AND seller = ? ORDER BY id DESC")) {
            ps.setInt(1, cid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("type") != 1) {
                    Item i = new Item(rs.getInt("itemid"), (short) 0, (short) rs.getInt("quantity"));
                    i.setOwner(rs.getString("owner"));
                    items.add(new MTSItemInfo(i, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                } else {
                    Equip equip = new Equip(rs.getInt("itemid"), (byte) rs.getInt("position"), -1);
                    equip.setOwner(rs.getString("owner"));
                    equip.setQuantity((short) 1);
                    equip.setAcc((short) rs.getInt("acc"));
                    equip.setAvoid((short) rs.getInt("avoid"));
                    equip.setDex((short) rs.getInt("dex"));
                    equip.setHands((short) rs.getInt("hands"));
                    equip.setHp((short) rs.getInt("hp"));
                    equip.setInt((short) rs.getInt("int"));
                    equip.setJump((short) rs.getInt("jump"));
                    equip.setVicious((short) rs.getInt("vicious"));
                    equip.setLuk((short) rs.getInt("luk"));
                    equip.setMatk((short) rs.getInt("matk"));
                    equip.setMdef((short) rs.getInt("mdef"));
                    equip.setMp((short) rs.getInt("mp"));
                    equip.setSpeed((short) rs.getInt("speed"));
                    equip.setStr((short) rs.getInt("str"));
                    equip.setWatk((short) rs.getInt("watk"));
                    equip.setWdef((short) rs.getInt("wdef"));
                    equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                    equip.setLevel((byte) rs.getInt("level"));
                    equip.setItemLevel(rs.getByte("itemlevel"));
                    equip.setItemExp(rs.getInt("itemexp"));
                    equip.setRingId(rs.getInt("ringid"));
                    equip.setFlag((short) rs.getInt("flag"));
                    equip.setExpiration(rs.getLong("expiration"));
                    equip.setGiftFrom(rs.getString("giftFrom"));
                    items.add(new MTSItemInfo(equip, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private static Packet getMTS(int tab, int type, int page) {
        List<MTSItemInfo> items = new ArrayList<>();
        int pages = 0;
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            String sql;
            if (type != 0) {
                sql = "SELECT * FROM mts_items WHERE tab = ? AND type = ? AND transfer = 0 ORDER BY id DESC LIMIT ?, 16";
            } else {
                sql = "SELECT * FROM mts_items WHERE tab = ? AND transfer = 0 ORDER BY id DESC LIMIT ?, 16";
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, tab);
                if (type != 0) {
                    ps.setInt(2, type);
                    ps.setInt(3, page * 16);
                } else {
                    ps.setInt(2, page * 16);
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getInt("type") != 1) {
                        Item i = new Item(rs.getInt("itemid"), (short) 0, (short) rs.getInt("quantity"));
                        i.setOwner(rs.getString("owner"));
                        items.add(new MTSItemInfo(i, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"),
                                rs.getString("sellername"), rs.getString("sell_ends")));
                    } else {
                        Equip equip = new Equip(rs.getInt("itemid"), (byte) rs.getInt("position"), -1);
                        equip.setOwner(rs.getString("owner"));
                        equip.setQuantity((short) 1);
                        equip.setAcc((short) rs.getInt("acc"));
                        equip.setAvoid((short) rs.getInt("avoid"));
                        equip.setDex((short) rs.getInt("dex"));
                        equip.setHands((short) rs.getInt("hands"));
                        equip.setHp((short) rs.getInt("hp"));
                        equip.setInt((short) rs.getInt("int"));
                        equip.setJump((short) rs.getInt("jump"));
                        equip.setVicious((short) rs.getInt("vicious"));
                        equip.setLuk((short) rs.getInt("luk"));
                        equip.setMatk((short) rs.getInt("matk"));
                        equip.setMdef((short) rs.getInt("mdef"));
                        equip.setMp((short) rs.getInt("mp"));
                        equip.setSpeed((short) rs.getInt("speed"));
                        equip.setStr((short) rs.getInt("str"));
                        equip.setWatk((short) rs.getInt("watk"));
                        equip.setWdef((short) rs.getInt("wdef"));
                        equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                        equip.setLevel((byte) rs.getInt("level"));
                        equip.setItemLevel(rs.getByte("itemlevel"));
                        equip.setItemExp(rs.getInt("itemexp"));
                        equip.setRingId(rs.getInt("ringid"));
                        equip.setFlag((short) rs.getInt("flag"));
                        equip.setExpiration(rs.getLong("expiration"));
                        equip.setGiftFrom(rs.getString("giftFrom"));
                        items.add(new MTSItemInfo(equip, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mts_items WHERE tab = ? " + (type != 0 ? "AND type = ?" : "") + " AND transfer = 0")) {
                ps.setInt(1, tab);
                if (type != 0) {
                    ps.setInt(2, type);
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    pages = rs.getInt(1) / 16;
                    if (rs.getInt(1) % 16 > 0) {
                        pages++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ChannelPacketCreator.getInstance().sendMTS(items, tab, type, page, pages); // resniff
    }

    public Packet getMTSSearch(int tab, int type, int cOi, String search, int page) {
        List<MTSItemInfo> items = new ArrayList<>();
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        String listaitems = "";
        if (cOi != 0) {
            List<String> retItems = new ArrayList<>();
            for (Pair<Integer, String> itemPair : ii.getAllItems()) {
                if (itemPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                    retItems.add(" itemid=" + itemPair.getLeft() + " OR ");
                }
            }
            listaitems += " AND (";
            if (retItems != null && retItems.size() > 0) {
                for (String singleRetItem : retItems) {
                    listaitems += singleRetItem;
                }
                listaitems += " itemid=0 )";
            }
        } else {
            listaitems = " AND sellername LIKE CONCAT('%','" + search + "', '%')";
        }
        int pages = 0;
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            String sql;
            if (type != 0) {
                sql = "SELECT * FROM mts_items WHERE tab = ? " + listaitems + " AND type = ? AND transfer = 0 ORDER BY id DESC LIMIT ?, 16";
            } else {
                sql = "SELECT * FROM mts_items WHERE tab = ? " + listaitems + " AND transfer = 0 ORDER BY id DESC LIMIT ?, 16";
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, tab);
                if (type != 0) {
                    ps.setInt(2, type);
                    ps.setInt(3, page * 16);
                } else {
                    ps.setInt(2, page * 16);
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getInt("type") != 1) {
                        Item i = new Item(rs.getInt("itemid"), (short) 0, (short) rs.getInt("quantity"));
                        i.setOwner(rs.getString("owner"));
                        items.add(new MTSItemInfo(i, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                    } else {
                        Equip equip = new Equip(rs.getInt("itemid"), (byte) rs.getInt("position"), -1);
                        equip.setOwner(rs.getString("owner"));
                        equip.setQuantity((short) 1);
                        equip.setAcc((short) rs.getInt("acc"));
                        equip.setAvoid((short) rs.getInt("avoid"));
                        equip.setDex((short) rs.getInt("dex"));
                        equip.setHands((short) rs.getInt("hands"));
                        equip.setHp((short) rs.getInt("hp"));
                        equip.setInt((short) rs.getInt("int"));
                        equip.setJump((short) rs.getInt("jump"));
                        equip.setVicious((short) rs.getInt("vicious"));
                        equip.setLuk((short) rs.getInt("luk"));
                        equip.setMatk((short) rs.getInt("matk"));
                        equip.setMdef((short) rs.getInt("mdef"));
                        equip.setMp((short) rs.getInt("mp"));
                        equip.setSpeed((short) rs.getInt("speed"));
                        equip.setStr((short) rs.getInt("str"));
                        equip.setWatk((short) rs.getInt("watk"));
                        equip.setWdef((short) rs.getInt("wdef"));
                        equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                        equip.setLevel((byte) rs.getInt("level"));
                        equip.setItemLevel(rs.getByte("itemlevel"));
                        equip.setItemExp(rs.getInt("itemexp"));
                        equip.setRingId(rs.getInt("ringid"));
                        equip.setFlag((short) rs.getInt("flag"));
                        equip.setExpiration(rs.getLong("expiration"));
                        equip.setGiftFrom(rs.getString("giftFrom"));
                        items.add(new MTSItemInfo(equip, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                    }
                }
            }
            if (type == 0) {
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mts_items WHERE tab = ? " + listaitems + " AND transfer = 0")) {
                    ps.setInt(1, tab);
                    if (type != 0) {
                        ps.setInt(2, type);
                    }
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        pages = rs.getInt(1) / 16;
                        if (rs.getInt(1) % 16 > 0) {
                            pages++;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ChannelPacketCreator.getInstance().sendMTS(items, tab, type, page, pages);
    }
}
