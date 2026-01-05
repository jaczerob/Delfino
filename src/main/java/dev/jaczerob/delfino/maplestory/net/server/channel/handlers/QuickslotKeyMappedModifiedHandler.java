package dev.jaczerob.delfino.maplestory.net.server.channel.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.keybind.QuickslotBinding;
import dev.jaczerob.delfino.maplestory.net.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.net.packet.InPacket;

/**
 * @author Shavit
 */
public class QuickslotKeyMappedModifiedHandler extends AbstractPacketHandler {
    @Override
    public void handlePacket(InPacket p, Client c) {
        // Invalid size for the packet.
        if (p.available() != QuickslotBinding.QUICKSLOT_SIZE * Integer.BYTES ||
                // not logged in-game
                c.getPlayer() == null) {
            return;
        }

        byte[] aQuickslotKeyMapped = new byte[QuickslotBinding.QUICKSLOT_SIZE];

        for (int i = 0; i < QuickslotBinding.QUICKSLOT_SIZE; i++) {
            aQuickslotKeyMapped[i] = (byte) p.readInt();
        }

        c.getPlayer().changeQuickslotKeybinding(aQuickslotKeyMapped);
    }
}
