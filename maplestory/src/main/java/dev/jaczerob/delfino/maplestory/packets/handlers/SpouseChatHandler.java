package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ChatLogger;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class SpouseChatHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.SPOUSE_CHAT;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        packet.readString();//recipient
        String msg = packet.readString();

        int partnerId = client.getPlayer().getPartnerId();
        if (partnerId > 0) { // yay marriage
            Character spouse = client.getWorldServer().getPlayerStorage().getCharacterById(partnerId);
            if (spouse != null) {
                spouse.sendPacket(ChannelPacketCreator.getInstance().OnCoupleMessage(client.getPlayer().getName(), msg, true));
                client.sendPacket(ChannelPacketCreator.getInstance().OnCoupleMessage(client.getPlayer().getName(), msg, true));
                ChatLogger.log(client, "Spouse", msg);
            } else {
                client.getPlayer().dropMessage(5, "Your spouse is currently offline.");
            }
        } else {
            client.getPlayer().dropMessage(5, "You don't have a spouse.");
        }
    }
}
