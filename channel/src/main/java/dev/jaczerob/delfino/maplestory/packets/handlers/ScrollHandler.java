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
import dev.jaczerob.delfino.maplestory.client.Skill;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.*;
import dev.jaczerob.delfino.maplestory.client.inventory.Equip.ScrollResult;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matze
 * @author Frz
 */
@Component
public final class ScrollHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.USE_UPGRADE_SCROLL;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (client.tryacquireClient()) {
            try {
                packet.readInt(); // whatever...
                short scrollSlot = packet.readShort();
                short equipSlot = packet.readShort();
                byte ws = (byte) packet.readShort();
                boolean whiteScroll = false; // white scroll being used?
                boolean legendarySpirit = false; // legendary spirit skill
                if ((ws & 2) == 2) {
                    whiteScroll = true;
                }

                ItemInformationProvider ii = ItemInformationProvider.getInstance();
                Character chr = client.getPlayer();
                Equip toScroll = (Equip) chr.getInventory(InventoryType.EQUIPPED).getItem(equipSlot);
                Skill LegendarySpirit = SkillFactory.getSkill(1003);
                if (chr.getSkillLevel(LegendarySpirit) > 0 && equipSlot >= 0) {
                    legendarySpirit = true;
                    toScroll = (Equip) chr.getInventory(InventoryType.EQUIP).getItem(equipSlot);
                }
                byte oldLevel = toScroll.getLevel();
                byte oldSlots = toScroll.getUpgradeSlots();
                Inventory useInventory = chr.getInventory(InventoryType.USE);
                Item scroll = useInventory.getItem(scrollSlot);
                Item wscroll = null;

                if (ItemConstants.isCleanSlate(scroll.getItemId()) && !ii.canUseCleanSlate(toScroll)) {
                    announceCannotScroll(client, legendarySpirit);
                    return;
                } else if (!ItemConstants.isModifierScroll(scroll.getItemId()) && toScroll.getUpgradeSlots() < 1) {
                    announceCannotScroll(client, legendarySpirit);   // thanks onechord for noticing zero upgrade slots freezing Legendary Scroll UI
                    return;
                }

                List<Integer> scrollReqs = ii.getScrollReqs(scroll.getItemId());
                if (scrollReqs.size() > 0 && !scrollReqs.contains(toScroll.getItemId())) {
                    announceCannotScroll(client, legendarySpirit);
                    return;
                }
                if (whiteScroll) {
                    wscroll = useInventory.findById(ItemId.WHITE_SCROLL);
                    if (wscroll == null) {
                        whiteScroll = false;
                    }
                }

                if (!ItemConstants.isChaosScroll(scroll.getItemId()) && !ItemConstants.isCleanSlate(scroll.getItemId())) {
                    if (!canScroll(scroll.getItemId(), toScroll.getItemId())) {
                        announceCannotScroll(client, legendarySpirit);
                        return;
                    }
                }

                Equip scrolled = (Equip) ii.scrollEquipWithId(toScroll, scroll.getItemId(), whiteScroll, 0, chr.isGM());
                ScrollResult scrollSuccess = ScrollResult.FAIL; // fail
                if (scrolled == null) {
                    scrollSuccess = ScrollResult.CURSE;
                } else if (scrolled.getLevel() > oldLevel || (ItemConstants.isCleanSlate(scroll.getItemId()) && scrolled.getUpgradeSlots() == oldSlots + 1) || ItemConstants.isFlagModifier(scroll.getItemId(), scrolled.getFlag())) {
                    scrollSuccess = ScrollResult.SUCCESS;
                }

                useInventory.lockInventory();
                try {
                    if (scroll.getQuantity() < 1) {
                        announceCannotScroll(client, legendarySpirit);
                        return;
                    }

                    if (whiteScroll && !ItemConstants.isCleanSlate(scroll.getItemId())) {
                        if (wscroll.getQuantity() < 1) {
                            announceCannotScroll(client, legendarySpirit);
                            return;
                        }

                        InventoryManipulator.removeFromSlot(client, InventoryType.USE, wscroll.getPosition(), (short) 1, false, false);
                    }

                    InventoryManipulator.removeFromSlot(client, InventoryType.USE, scroll.getPosition(), (short) 1, false);
                } finally {
                    useInventory.unlockInventory();
                }

                final List<ModifyInventory> mods = new ArrayList<>();
                if (scrollSuccess == ScrollResult.CURSE) {
                    if (!ItemId.isWeddingRing(toScroll.getItemId())) {
                        mods.add(new ModifyInventory(3, toScroll));
                        if (equipSlot < 0) {
                            Inventory inv = chr.getInventory(InventoryType.EQUIPPED);

                            inv.lockInventory();
                            try {
                                chr.unequippedItem(toScroll);
                                inv.removeItem(toScroll.getPosition());
                            } finally {
                                inv.unlockInventory();
                            }
                        } else {
                            Inventory inv = chr.getInventory(InventoryType.EQUIP);

                            inv.lockInventory();
                            try {
                                inv.removeItem(toScroll.getPosition());
                            } finally {
                                inv.unlockInventory();
                            }
                        }
                    } else {
                        scrolled = toScroll;
                        scrollSuccess = ScrollResult.FAIL;

                        mods.add(new ModifyInventory(3, scrolled));
                        mods.add(new ModifyInventory(0, scrolled));
                    }
                } else {
                    mods.add(new ModifyInventory(3, scrolled));
                    mods.add(new ModifyInventory(0, scrolled));
                }
                client.sendPacket(ChannelPacketCreator.getInstance().modifyInventory(true, mods));
                chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().getScrollEffect(chr.getId(), scrollSuccess, legendarySpirit, whiteScroll));
                if (equipSlot < 0 && (scrollSuccess == ScrollResult.SUCCESS || scrollSuccess == ScrollResult.CURSE)) {
                    chr.equipChanged();
                }
            } finally {
                client.releaseClient();
            }
        }
    }

    private static void announceCannotScroll(Client client, boolean legendarySpirit) {
        if (legendarySpirit) {
            client.sendPacket(ChannelPacketCreator.getInstance().getScrollEffect(client.getPlayer().getId(), ScrollResult.FAIL, false, false));
        } else {
            client.sendPacket(ChannelPacketCreator.getInstance().getInventoryFull());
        }
    }

    private static boolean canScroll(int scrollid, int itemid) {
        int sid = scrollid / 100;

        switch (sid) {
            case 20492: //scroll for accessory (pendant, belt, ring)
                return canScroll(ItemId.RING_STR_100_SCROLL, itemid) || canScroll(ItemId.DRAGON_STONE_SCROLL, itemid) ||
                        canScroll(ItemId.BELT_STR_100_SCROLL, itemid);

            default:
                return (scrollid / 100) % 100 == (itemid / 10000) % 100;
        }
    }
}
