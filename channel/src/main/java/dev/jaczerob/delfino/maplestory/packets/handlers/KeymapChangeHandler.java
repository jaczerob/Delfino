package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.keybind.KeyBinding;
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
