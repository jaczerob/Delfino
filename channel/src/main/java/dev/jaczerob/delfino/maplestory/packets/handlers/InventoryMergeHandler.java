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
import dev.jaczerob.delfino.maplestory.client.inventory.Inventory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class InventoryMergeHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.ITEM_SORT;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        packet.readInt();
        chr.getAutobanManager().setTimestamp(2, Server.getInstance().getCurrentTimestamp(), 4);

        if (!YamlConfig.config.server.USE_ITEM_SORT) {
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        byte invType = packet.readByte();
        if (invType < 1 || invType > 5) {
            client.disconnect(false, false);
            return;
        }

        InventoryType inventoryType = InventoryType.getByType(invType);
        Inventory inventory = client.getPlayer().getInventory(inventoryType);
        inventory.lockInventory();
        try {
            //------------------- RonanLana's SLOT MERGER -----------------

            ItemInformationProvider ii = ItemInformationProvider.getInstance();
            Item srcItem, dstItem;

            for (short dst = 1; dst <= inventory.getSlotLimit(); dst++) {
                dstItem = inventory.getItem(dst);
                if (dstItem == null) {
                    continue;
                }

                for (short src = (short) (dst + 1); src <= inventory.getSlotLimit(); src++) {
                    srcItem = inventory.getItem(src);
                    if (srcItem == null) {
                        continue;
                    }

                    if (dstItem.getItemId() != srcItem.getItemId()) {
                        continue;
                    }
                    if (dstItem.getQuantity() == ii.getSlotMax(client, inventory.getItem(dst).getItemId())) {
                        break;
                    }

                    InventoryManipulator.move(client, inventoryType, src, dst);
                }
            }

            //------------------------------------------------------------

            inventory = client.getPlayer().getInventory(inventoryType);
            boolean sorted = false;

            while (!sorted) {
                short freeSlot = inventory.getNextFreeSlot();

                if (freeSlot != -1) {
                    short itemSlot = -1;
                    for (short i = (short) (freeSlot + 1); i <= inventory.getSlotLimit(); i = (short) (i + 1)) {
                        if (inventory.getItem(i) != null) {
                            itemSlot = i;
                            break;
                        }
                    }
                    if (itemSlot > 0) {
                        InventoryManipulator.move(client, inventoryType, itemSlot, freeSlot);
                    } else {
                        sorted = true;
                    }
                } else {
                    sorted = true;
                }
            }
        } finally {
            inventory.unlockInventory();
        }

        client.sendPacket(ChannelPacketCreator.getInstance().finishedSort(inventoryType.getType()));
        client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
    }
}