package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.inventory.Pet;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.MapItem;
import dev.jaczerob.delfino.maplestory.server.maps.MapObject;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author TheRamon
 * @author Ronan
 */
@Component
public final class PetLootHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.PET_LOOT;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();

        int petIndex = chr.getPetIndex(packet.readInt());
        Pet pet = chr.getPet(petIndex);
        if (pet == null || !pet.isSummoned()) {
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        packet.skip(13);
        int oid = packet.readInt();
        MapObject ob = chr.getMap().getMapObject(oid);
        try {
            MapItem mapitem = (MapItem) ob;
            if (mapitem.getMeso() > 0) {
                if (!chr.isEquippedMesoMagnet()) {
                    client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                if (chr.isEquippedPetItemIgnore()) {
                    final Set<Integer> petIgnore = chr.getExcludedItems();
                    if (!petIgnore.isEmpty() && petIgnore.contains(Integer.MAX_VALUE)) {
                        client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return;
                    }
                }
            } else {
                if (!chr.isEquippedItemPouch()) {
                    client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                if (chr.isEquippedPetItemIgnore()) {
                    final Set<Integer> petIgnore = chr.getExcludedItems();
                    if (!petIgnore.isEmpty() && petIgnore.contains(mapitem.getItem().getItemId())) {
                        client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return;
                    }
                }
            }

            chr.pickupItem(ob, petIndex);
        } catch (NullPointerException | ClassCastException e) {
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
        }
    }
}
