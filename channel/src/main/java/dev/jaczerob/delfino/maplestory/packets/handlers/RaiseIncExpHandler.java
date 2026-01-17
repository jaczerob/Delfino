package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.QuestStatus;
import dev.jaczerob.delfino.maplestory.client.inventory.Inventory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider.QuestConsItem;
import dev.jaczerob.delfino.maplestory.server.quest.Quest;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Xari
 * @author Ronan - added concurrency protection and quest progress limit
 */
@Component
public class RaiseIncExpHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.USE_ITEMUI;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        byte inventorytype = packet.readByte();//nItemIT
        short slot = packet.readShort();//nSlotPosition
        int itemid = packet.readInt();//nItemID

        if (client.tryacquireClient()) {
            try {
                ItemInformationProvider ii = ItemInformationProvider.getInstance();
                QuestConsItem consItem = ii.getQuestConsumablesInfo(itemid);
                if (consItem == null) {
                    return;
                }

                int infoNumber = consItem.questid;
                Map<Integer, Integer> consumables = consItem.items;

                Character chr = client.getPlayer();
                Quest quest = Quest.getInstanceFromInfoNumber(infoNumber);
                if (!chr.getQuest(quest).getStatus().equals(QuestStatus.Status.STARTED)) {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                int consId;
                Inventory inv = chr.getInventory(InventoryType.getByType(inventorytype));
                inv.lockInventory();
                try {
                    consId = inv.getItem(slot).getItemId();
                    if (!consumables.containsKey(consId) || !chr.haveItem(consId)) {
                        return;
                    }

                    InventoryManipulator.removeFromSlot(client, InventoryType.getByType(inventorytype), slot, (short) 1, false, true);
                } finally {
                    inv.unlockInventory();
                }

                int questid = quest.getId();
                int nextValue = Math.min(consumables.get(consId) + client.getAbstractPlayerInteraction().getQuestProgressInt(questid, infoNumber), consItem.exp * consItem.grade);
                client.getAbstractPlayerInteraction().setQuestProgress(questid, infoNumber, nextValue);

                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
            } finally {
                client.releaseClient();
            }
        }
    }
}
