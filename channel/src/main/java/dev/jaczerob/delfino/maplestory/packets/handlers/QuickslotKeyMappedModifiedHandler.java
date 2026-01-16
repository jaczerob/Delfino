package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.keybind.QuickslotBinding;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Shavit
 */
@Component
public class QuickslotKeyMappedModifiedHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CHANGE_QUICKSLOT;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        // Invalid size for the packet.
        if (packet.available() != QuickslotBinding.QUICKSLOT_SIZE * Integer.BYTES ||
                // not logged in-game
                client.getPlayer() == null) {
            return;
        }

        byte[] aQuickslotKeyMapped = new byte[QuickslotBinding.QUICKSLOT_SIZE];

        for (int i = 0; i < QuickslotBinding.QUICKSLOT_SIZE; i++) {
            aQuickslotKeyMapped[i] = (byte) packet.readInt();
        }

        client.getPlayer().changeQuickslotKeybinding(aQuickslotKeyMapped);
    }
}
