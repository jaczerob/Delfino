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

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.server.life.LifeFactory;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.Randomizer;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author AngelSL
 */
@Component
public final class UseSummonBagHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.USE_SUMMON_BAG;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        //[4A 00][6C 4C F2 02][02 00][63 0B 20 00]
        if (!client.getPlayer().isAlive()) {
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }
        packet.readInt();
        short slot = packet.readShort();
        int itemId = packet.readInt();
        Item toUse = client.getPlayer().getInventory(InventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemId) {
            InventoryManipulator.removeFromSlot(client, InventoryType.USE, slot, (short) 1, false);
            int[][] toSpawn = ItemInformationProvider.getInstance().getSummonMobs(itemId);
            for (int[] toSpawnChild : toSpawn) {
                if (Randomizer.nextInt(100) < toSpawnChild[1]) {
                    client.getPlayer().getMap().spawnMonsterOnGroundBelow(LifeFactory.getMonster(toSpawnChild[0]), client.getPlayer().getPosition());
                }
            }
        }
        client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
    }
}
