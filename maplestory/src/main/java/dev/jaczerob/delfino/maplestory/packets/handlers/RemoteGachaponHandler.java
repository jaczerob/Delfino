package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.constants.id.NpcId;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.scripting.npc.NPCScriptManager;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Generic
 */
@Component
public final class RemoteGachaponHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.USE_REMOTE;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int ticket = packet.readInt();
        int gacha = packet.readInt();
        if (ticket != ItemId.REMOTE_GACHAPON_TICKET) {
            AutobanFactory.GENERAL.alert(client.getPlayer(), " Tried to use RemoteGachaponHandler with item id: " + ticket);
            client.disconnect(false, false);
            return;
        } else if (gacha < 0 || gacha > 11) {
            AutobanFactory.GENERAL.alert(client.getPlayer(), " Tried to use RemoteGachaponHandler with mode: " + gacha);
            client.disconnect(false, false);
            return;
        } else if (client.getPlayer().getInventory(ItemConstants.getInventoryType(ticket)).countById(ticket) < 1) {
            AutobanFactory.GENERAL.alert(client.getPlayer(), " Tried to use RemoteGachaponHandler without a ticket.");
            client.disconnect(false, false);
            return;
        }
        int npcId = NpcId.GACHAPON_HENESYS;
        if (gacha != 8 && gacha != 9) {
            npcId += gacha;
        } else {
            npcId = gacha == 8 ? NpcId.GACHAPON_NLC : NpcId.GACHAPON_NAUTILUS;
        }
        NPCScriptManager.getInstance().start(client, npcId, "gachaponRemote", null);
    }
}
