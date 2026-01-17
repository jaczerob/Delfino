package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Matze
 */
@Component
public final class ItemMoveHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.ITEM_MOVE;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        packet.skip(4);
        if (client.getPlayer().getAutobanManager().getLastSpam(6) + 300 > currentServerTime()) {
            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        InventoryType type = InventoryType.getByType(packet.readByte());
        short src = packet.readShort();     //is there any reason to use byte instead of short in src and action?
        short action = packet.readShort();
        short quantity = packet.readShort();

        if (src < 0 && action > 0) {
            InventoryManipulator.unequip(client, src, action);
        } else if (action < 0) {
            InventoryManipulator.equip(client, src, action);
        } else if (action == 0) {
            InventoryManipulator.drop(client, type, src, quantity);
        } else {
            InventoryManipulator.move(client, type, src, action);
        }

        client.getPlayer().getAutobanManager().spam(6);
    }
}