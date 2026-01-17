package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.net.server.world.World;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ChatLogger;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class MultiChatHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(MultiChatHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.MULTI_CHAT;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character player = client.getPlayer();
        if (player.getAutobanManager().getLastSpam(7) + 200 > currentServerTime()) {
            return;
        }

        int type = packet.readByte(); // 0 for buddys, 1 for partys
        int numRecipients = packet.readByte();
        int[] recipients = new int[numRecipients];
        for (int i = 0; i < numRecipients; i++) {
            recipients[i] = packet.readInt();
        }
        String chattext = packet.readString();
        if (chattext.length() > Byte.MAX_VALUE && !player.isGM()) {
            AutobanFactory.PACKET_EDIT.alert(client.getPlayer(), client.getPlayer().getName() + " tried to packet edit chats.");
            log.warn("Chr {} tried to send text with length of {}", client.getPlayer().getName(), chattext.length());
            client.disconnect(true, false);
            return;
        }
        World world = client.getWorldServer();
        if (type == 0) {
            world.buddyChat(recipients, player.getId(), player.getName(), chattext);
            ChatLogger.log(client, "Buddy", chattext);
        } else if (type == 1 && player.getParty() != null) {
            world.partyChat(player.getParty(), chattext, player.getName());
            ChatLogger.log(client, "Party", chattext);
        }
        player.getAutobanManager().spam(7);
    }
}
