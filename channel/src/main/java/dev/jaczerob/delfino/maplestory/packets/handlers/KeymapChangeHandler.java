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
import dev.jaczerob.delfino.maplestory.client.Skill;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.keybind.KeyBinding;
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class KeymapChangeHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CHANGE_KEYMAP;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (packet.available() >= 8) {
            int mode = packet.readInt();
            if (mode == 0) {
                int numChanges = packet.readInt();
                for (int i = 0; i < numChanges; i++) {
                    int key = packet.readInt();
                    int type = packet.readByte();
                    int action = packet.readInt();

                    if (type == 1) {
                        Skill skill = SkillFactory.getSkill(action);
                        boolean isBanndedSkill;
                        if (skill != null) {
                            isBanndedSkill = GameConstants.bannedBindSkills(skill.getId());
                            if (isBanndedSkill || (!client.getPlayer().isGM() && GameConstants.isGMSkills(skill.getId())) || (!GameConstants.isInJobTree(skill.getId(), client.getPlayer().getJob().getId()) && !client.getPlayer().isGM())) { //for those skills are are "technically" in the beginner tab, like bamboo rain in Dojo or skills you find in PYPQ
                                //AutobanFactory.PACKET_EDIT.alert(client.getPlayer(), client.getPlayer().getName() + " tried to packet edit keymapping.");
                                //FilePrinter.printError(FilePrinter.EXPLOITS + client.getPlayer().getName() + ".txt", client.getPlayer().getName() + " tried to use skill " + skill.getId());
                                //client.disconnect(true, false);
                                //return;

                                continue;   // fk that
                            }
                                                        /* if (client.getPlayer().getSkillLevel(skill) < 1) {    HOW WOULD A SKILL EVEN BE AVAILABLE TO KEYBINDING
                                                                continue;                                   IF THERE IS NOT EVEN A SINGLE POINT USED INTO IT??
                                                        } */
                        }
                    }

                    client.getPlayer().changeKeybinding(key, new KeyBinding(type, action));
                }
            } else if (mode == 1) { // Auto HP Potion
                int itemID = packet.readInt();
                if (itemID != 0 && client.getPlayer().getInventory(InventoryType.USE).findById(itemID) == null) {
                    client.disconnect(false, false); // Don't let them send a packet with a use item they dont have.
                    return;
                }
                client.getPlayer().changeKeybinding(91, new KeyBinding(7, itemID));
            } else if (mode == 2) { // Auto MP Potion
                int itemID = packet.readInt();
                if (itemID != 0 && client.getPlayer().getInventory(InventoryType.USE).findById(itemID) == null) {
                    client.disconnect(false, false); // Don't let them send a packet with a use item they dont have.
                    return;
                }
                client.getPlayer().changeKeybinding(92, new KeyBinding(7, itemID));
            }
        }
    }
}
