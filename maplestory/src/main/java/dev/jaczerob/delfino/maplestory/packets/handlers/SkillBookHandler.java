package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.Skill;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.Inventory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public final class SkillBookHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.USE_SKILL_BOOK;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (!client.getPlayer().isAlive()) {
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        packet.readInt();
        short slot = packet.readShort();
        int itemId = packet.readInt();

        boolean canuse;
        boolean success = false;
        int skill = 0;
        int maxlevel = 0;

        Character player = client.getPlayer();
        if (client.tryacquireClient()) {
            try {
                Inventory inv = client.getPlayer().getInventory(InventoryType.USE);
                Item toUse = inv.getItem(slot);
                if (toUse == null || toUse.getItemId() != itemId) {
                    return;
                }
                Map<String, Integer> skilldata = ItemInformationProvider.getInstance().getSkillStats(toUse.getItemId(), client.getPlayer().getJob().getId());
                if (skilldata == null) {
                    return;
                }
                Skill skill2 = SkillFactory.getSkill(skilldata.get("skillid"));
                if (skilldata.get("skillid") == 0) {
                    canuse = false;
                } else if ((player.getSkillLevel(skill2) >= skilldata.get("reqSkillLevel") || skilldata.get("reqSkillLevel") == 0) && player.getMasterLevel(skill2) < skilldata.get("masterLevel")) {
                    inv.lockInventory();
                    try {
                        Item used = inv.getItem(slot);
                        if (used != toUse || toUse.getQuantity() < 1) {    // thanks ClouD for noticing skillbooks not being usable when stacked
                            return;
                        }

                        InventoryManipulator.removeFromSlot(client, InventoryType.USE, slot, (short) 1, false);
                    } finally {
                        inv.unlockInventory();
                    }

                    canuse = true;
                    if (ItemInformationProvider.rollSuccessChance(skilldata.get("success"))) {
                        success = true;
                        player.changeSkillLevel(skill2, player.getSkillLevel(skill2), Math.max(skilldata.get("masterLevel"), player.getMasterLevel(skill2)), -1);
                    } else {
                        success = false;
                        //player.dropMessage("The skill book lights up, but the skill winds up as if nothing happened.");
                    }
                } else {
                    canuse = false;
                }
            } finally {
                client.releaseClient();
            }

            // thanks Vcoc for noting skill book result not showing for all in area
            player.getMap().broadcastMessage(ChannelPacketCreator.getInstance().skillBookResult(player, skill, maxlevel, canuse, success));
        }
    }
}
