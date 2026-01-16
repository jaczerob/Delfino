package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Matze
 */
@Component
public final class NPCShopHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(NPCShopHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.NPC_SHOP;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        byte bmode = packet.readByte();
        switch (bmode) {
            case 0: { // mode 0 = buy :)
                short slot = packet.readShort();// slot
                int itemId = packet.readInt();
                short quantity = packet.readShort();
                if (quantity < 1) {
                    AutobanFactory.PACKET_EDIT.alert(client.getPlayer(),
                            client.getPlayer().getName() + " tried to packet edit a npc shop.");
                    log.warn("Chr {} tried to buy quantity {} of itemid {}", client.getPlayer().getName(), quantity, itemId);
                    client.disconnect(true, false);
                    return;
                }
                client.getPlayer().getShop().buy(client, slot, itemId, quantity);
                break;
            }
            case 1: { // sell ;)
                short slot = packet.readShort();
                int itemId = packet.readInt();
                short quantity = packet.readShort();
                client.getPlayer().getShop().sell(client, ItemConstants.getInventoryType(itemId), slot, quantity);
                break;
            }
            case 2: { // recharge ;)

                byte slot = (byte) packet.readShort();
                client.getPlayer().getShop().recharge(client, slot);
                break;
            }
            case 3: // leaving :(
                client.getPlayer().setShop(null);
                break;
        }

    }
}
