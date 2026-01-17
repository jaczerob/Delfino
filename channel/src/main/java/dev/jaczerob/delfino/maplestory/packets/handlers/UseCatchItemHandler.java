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
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanManager;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.constants.id.MobId;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author kevintjuh93
 */
@Component
public final class UseCatchItemHandler extends AbstractPacketHandler {
    private static void killMonster(Monster mob) {
        mob.getMap().killMonster(mob, null, false, (short) 0);
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.USE_CATCH_ITEM;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        AutobanManager abm = chr.getAutobanManager();
        packet.readInt();
        abm.setTimestamp(5, Server.getInstance().getCurrentTimestamp(), 4);
        packet.readShort();
        int itemId = packet.readInt();
        int monsterid = packet.readInt();

        Monster mob = chr.getMap().getMonsterByOid(monsterid);
        if (chr.getInventory(ItemConstants.getInventoryType(itemId)).countById(itemId) <= 0) {
            return;
        }
        if (mob == null) {
            return;
        }
        switch (itemId) {
            case ItemId.PHEROMONE_PERFUME:
                if (mob.getId() == MobId.TAMABLE_HOG) {
                    chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().catchMonster(monsterid, itemId, (byte) 1));
                    killMonster(mob);
                    InventoryManipulator.removeById(client, InventoryType.USE, itemId, 1, true, true);
                    InventoryManipulator.addById(client, ItemId.HOG, (short) 1, "", -1);
                }
                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                break;
            case ItemId.POUCH:
                if (mob.getId() == MobId.GHOST) {
                    if ((abm.getLastSpam(10) + 1000) < currentServerTime()) {
                        if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                            chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().catchMonster(monsterid, itemId, (byte) 1));
                            killMonster(mob);
                            InventoryManipulator.removeById(client, InventoryType.USE, itemId, 1, true, true);
                            InventoryManipulator.addById(client, ItemId.GHOST_SACK, (short) 1, "", -1);
                        } else {
                            abm.spam(10);
                            context.writeAndFlush(ChannelPacketCreator.getInstance().catchMessage(0));
                        }
                    }
                    context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                }
                break;
            case ItemId.MAGIC_CANE:
                if (mob.getId() == MobId.LOST_RUDOLPH) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                        chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().catchMonster(monsterid, itemId, (byte) 1));
                        killMonster(mob);
                        InventoryManipulator.removeById(client, InventoryType.USE, itemId, 1, true, true);
                        InventoryManipulator.addById(client, ItemId.TAMED_RUDOLPH, (short) 1, "", -1);
                    } else {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().catchMessage(0));
                    }
                }
                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                break;
            case ItemId.TRANSPARENT_MARBLE_1:
                if (mob.getId() == MobId.KING_SLIME_DOJO) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                        chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().catchMonster(monsterid, itemId, (byte) 1));
                        killMonster(mob);
                        InventoryManipulator.removeById(client, InventoryType.USE, itemId, 1, true, true);
                        InventoryManipulator.addById(client, ItemId.MONSTER_MARBLE_1, (short) 1, "", -1);
                    } else {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().catchMessage(0));
                    }
                }
                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                break;
            case ItemId.TRANSPARENT_MARBLE_2:
                if (mob.getId() == MobId.FAUST_DOJO) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                        chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().catchMonster(monsterid, itemId, (byte) 1));
                        killMonster(mob);
                        InventoryManipulator.removeById(client, InventoryType.USE, itemId, 1, true, true);
                        InventoryManipulator.addById(client, ItemId.MONSTER_MARBLE_2, (short) 1, "", -1);
                    } else {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().catchMessage(0));
                    }
                }
                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                break;
            case ItemId.TRANSPARENT_MARBLE_3:
                if (mob.getId() == MobId.MUSHMOM_DOJO) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                        chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().catchMonster(monsterid, itemId, (byte) 1));
                        killMonster(mob);
                        InventoryManipulator.removeById(client, InventoryType.USE, itemId, 1, true, true);
                        InventoryManipulator.addById(client, ItemId.MONSTER_MARBLE_3, (short) 1, "", -1);
                    } else {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().catchMessage(0));
                    }
                }
                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                break;
            case ItemId.EPQ_PURIFICATION_MARBLE:
                if (mob.getId() == MobId.POISON_FLOWER) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                        chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().catchMonster(monsterid, itemId, (byte) 1));
                        killMonster(mob);
                        InventoryManipulator.removeById(client, InventoryType.USE, itemId, 1, true, true);
                        InventoryManipulator.addById(client, ItemId.EPQ_MONSTER_MARBLE, (short) 1, "", -1);
                    } else {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().catchMessage(0));
                    }
                }
                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                break;
            case ItemId.FISH_NET:
                if (mob.getId() == MobId.P_JUNIOR) {
                    if ((abm.getLastSpam(10) + 3000) < currentServerTime()) {
                        abm.spam(10);
                        chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().catchMonster(monsterid, itemId, (byte) 1));
                        killMonster(mob);
                        InventoryManipulator.removeById(client, InventoryType.USE, itemId, 1, true, true);
                        InventoryManipulator.addById(client, ItemId.FISH_NET_WITH_A_CATCH, (short) 1, "", -1);
                    } else {
                        chr.message("You cannot use the Fishing Net yet.");
                    }
                    context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                }
                break;
            default:
                // proper Fish catch, thanks to Dragohe4rt

                ItemInformationProvider ii = ItemInformationProvider.getInstance();
                int itemGanho = ii.getCreateItem(itemId);
                int mobItem = ii.getMobItem(itemId);

                if (itemGanho != 0 && mobItem == mob.getId()) {
                    int timeCatch = ii.getUseDelay(itemId);
                    int mobHp = ii.getMobHP(itemId);

                    if (timeCatch != 0 && (abm.getLastSpam(10) + timeCatch) < currentServerTime()) {
                        if (mobHp != 0 && mob.getHp() < ((mob.getMaxHp() / 100) * mobHp)) {
                            chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().catchMonster(monsterid, itemId, (byte) 1));
                            killMonster(mob);
                            InventoryManipulator.removeById(client, InventoryType.USE, itemId, 1, true, true);
                            InventoryManipulator.addById(client, itemGanho, (short) 1, "", -1);
                        } else if (mob.getId() != MobId.P_JUNIOR) {
                            if (mobHp != 0) {
                                abm.spam(10);
                                context.writeAndFlush(ChannelPacketCreator.getInstance().catchMessage(0));
                            }
                        } else {
                            chr.message("You cannot use the Fishing Net yet.");
                        }
                    }
                }
                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());

                // System.out.println("UseCatchItemHandler: \r\n" + slea.toString());
        }
    }
}
