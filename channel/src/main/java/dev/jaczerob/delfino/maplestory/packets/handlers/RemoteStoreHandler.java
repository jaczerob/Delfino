package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.HiredMerchant;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author kevintjuh93 - :3
 */
@Component
public class RemoteStoreHandler extends AbstractPacketHandler {
    private static HiredMerchant getMerchant(Client client) {
        if (client.getPlayer().hasMerchant()) {
            return client.getWorldServer().getHiredMerchant(client.getPlayer().getId());
        }
        return null;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.REMOTE_STORE;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        HiredMerchant hm = getMerchant(client);
        if (hm != null && hm.isOwner(chr)) {
            if (hm.getChannel() == chr.getClient().getChannel()) {
                hm.visitShop(chr);
            } else {
                context.writeAndFlush(ChannelPacketCreator.getInstance().remoteChannelChange((byte) (hm.getChannel() - 1)));
            }
            return;
        } else {
            chr.dropMessage(1, "You don't have a Merchant open.");
        }
        context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
    }
}
