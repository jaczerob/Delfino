package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.Pet;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author BubblesDev
 * @author Ronan
 */
@Component
public final class PetExcludeItemsHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.PET_EXCLUDE_ITEMS;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        final int petId = packet.readInt();
        packet.skip(4); // timestamp

        Character chr = client.getPlayer();
        byte petIndex = chr.getPetIndex(petId);
        if (petIndex < 0) {
            return;
        }

        final Pet pet = chr.getPet(petIndex);
        if (pet == null) {
            return;
        }

        chr.resetExcluded(petId);
        byte amount = packet.readByte();
        for (int i = 0; i < amount; i++) {
            int itemId = packet.readInt();
            if (itemId >= 0) {
                chr.addExcluded(petId, itemId);
            } else {
                AutobanFactory.PACKET_EDIT.alert(chr, "negative item id value in PetExcludeItemsHandler (" + itemId + ")");
                return;
            }
        }
        chr.commitExcludedItems();
    }
}
