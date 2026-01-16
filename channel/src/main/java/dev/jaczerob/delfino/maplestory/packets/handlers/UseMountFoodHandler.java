package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.Mount;
import dev.jaczerob.delfino.maplestory.client.inventory.Inventory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.constants.game.ExpTable;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author PurpleMadness
 * @author Ronan
 */
@Component
public final class UseMountFoodHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.USE_MOUNT_FOOD;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        packet.skip(4);
        short pos = packet.readShort();
        int itemid = packet.readInt();

        Character chr = client.getPlayer();
        Mount mount = chr.getMount();
        Inventory useInv = chr.getInventory(InventoryType.USE);

        if (client.tryacquireClient()) {
            try {
                Boolean mountLevelup = null;

                useInv.lockInventory();
                try {
                    Item item = useInv.getItem(pos);
                    if (item != null && item.getItemId() == itemid && mount != null) {
                        int curTiredness = mount.getTiredness();
                        int healedTiredness = Math.min(curTiredness, 30);

                        float healedFactor = (float) healedTiredness / 30;
                        mount.setTiredness(curTiredness - healedTiredness);

                        if (healedFactor > 0.0f) {
                            mount.setExp(mount.getExp() + (int) Math.ceil(healedFactor * (2 * mount.getLevel() + 6)));
                            int level = mount.getLevel();
                            boolean levelup = mount.getExp() >= ExpTable.getMountExpNeededForLevel(level) && level < 31;
                            if (levelup) {
                                mount.setLevel(level + 1);
                            }

                            mountLevelup = levelup;
                        }

                        InventoryManipulator.removeById(client, InventoryType.USE, itemid, 1, true, false);
                    }
                } finally {
                    useInv.unlockInventory();
                }

                if (mountLevelup != null) {
                    chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().updateMount(chr.getId(), mount, mountLevelup));
                }
            } finally {
                client.releaseClient();
            }
        }
    }
}