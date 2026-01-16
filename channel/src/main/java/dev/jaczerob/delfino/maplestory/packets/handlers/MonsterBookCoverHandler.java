package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class MonsterBookCoverHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.MONSTER_BOOK_COVER;
    }

    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int id = packet.readInt();
        if (id == 0 || id / 10000 == 238) {
            client.getPlayer().setMonsterBookCover(id);
            client.sendPacket(ChannelPacketCreator.getInstance().changeCover(id));
        }
    }
}
