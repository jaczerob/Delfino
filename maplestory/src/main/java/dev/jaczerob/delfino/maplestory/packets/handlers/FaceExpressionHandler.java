package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class FaceExpressionHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.FACE_EXPRESSION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        int emote = packet.readInt();

        if (emote > 7) {
            int itemid = 5159992 + emote;   // thanks RajanGrewal (Darter) for reporting unchecked emote itemid
            if (!ItemId.isFaceExpression(itemid) || chr.getInventory(ItemConstants.getInventoryType(itemid)).findById(itemid) == null) {
                return;
            }
        } else if (emote < 1) {
            return;
        }

        if (client.tryacquireClient()) {
            try {   // expecting players never intends to wear the emote 0 (default face, that changes back after 5sec timeout)
                if (chr.isLoggedinWorld()) {
                    chr.changeFaceExpression(emote);
                }
            } finally {
                client.releaseClient();
            }
        }
    }
}
