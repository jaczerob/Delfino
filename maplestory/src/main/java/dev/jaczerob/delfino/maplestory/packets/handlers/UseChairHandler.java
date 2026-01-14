package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class UseChairHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.USE_CHAIR;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int itemId = packet.readInt();

        // thanks Darter (YungMoozi) for reporting unchecked chair item
        if (!ItemId.isChair(itemId) || client.getPlayer().getInventory(InventoryType.SETUP).findById(itemId) == null) {
            return;
        }

        if (client.tryacquireClient()) {
            try {
                client.getPlayer().sitChair(itemId);
            } finally {
                client.releaseClient();
            }
        }
    }
}
