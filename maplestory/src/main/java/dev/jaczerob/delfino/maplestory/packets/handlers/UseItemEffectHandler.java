package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class UseItemEffectHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.USE_ITEMEFFECT;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Item toUse;
        int itemId = packet.readInt();
        if (itemId == ItemId.BUMMER_EFFECT || itemId == ItemId.GOLDEN_CHICKEN_EFFECT) {
            toUse = client.getPlayer().getInventory(InventoryType.ETC).findById(itemId);
        } else {
            toUse = client.getPlayer().getInventory(InventoryType.CASH).findById(itemId);
        }
        if (toUse == null || toUse.getQuantity() < 1) {
            if (itemId != 0) {
                return;
            }
        }
        client.getPlayer().setItemEffect(itemId);
        client.getPlayer().getMap().broadcastMessage(client.getPlayer(), ChannelPacketCreator.getInstance().itemEffect(client.getPlayer().getId(), itemId), false);
    }
}
