package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ChatLogger;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class PetChatHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(PetChatHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.PET_CHAT;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int petId = packet.readInt();
        packet.readInt();
        packet.readByte();
        int act = packet.readByte();
        byte pet = client.getPlayer().getPetIndex(petId);
        if ((pet < 0 || pet > 3) || (act < 0 || act > 9)) {
            return;
        }
        String text = packet.readString();
        if (text.length() > Byte.MAX_VALUE) {
            AutobanFactory.PACKET_EDIT.alert(client.getPlayer(), client.getPlayer().getName() + " tried to packet edit with pets.");
            log.warn("Chr {} tried to send text with length of {}", client.getPlayer().getName(), text.length());
            client.disconnect(true, false);
            return;
        }
        client.getPlayer().getMap().broadcastMessage(client.getPlayer(), ChannelPacketCreator.getInstance().petChat(client.getPlayer().getId(), pet, act, text), true);
        ChatLogger.log(client, "Pet", text);
    }
}
