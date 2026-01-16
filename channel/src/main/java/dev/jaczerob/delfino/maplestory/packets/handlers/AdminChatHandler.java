package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ChatLogger;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;


@Component
public class AdminChatHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.ADMIN_CHAT;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (!client.getPlayer().isGM()) {//if ( (signed int)CWvsContext::GetAdminLevel((void *)v294) > 2 )
            return;
        }
        byte mode = packet.readByte();
        String message = packet.readString();
        final var serverNoticePacket = ChannelPacketCreator.getInstance().serverNotice(packet.readByte(), message);//maybe I should make a check for the slea.readByte()... but I just hope gm's don't fuck things up :)
        switch (mode) {
            case 0 -> {// /alertall, /noticeall, /slideall
                client.getWorldServer().broadcastPacket(serverNoticePacket);
                ChatLogger.log(client, "Alert All", message);
            }
            case 1 -> {// /alertch, /noticech, /slidech
                client.getChannelServer().broadcastPacket(serverNoticePacket);
                ChatLogger.log(client, "Alert Ch", message);
            }
            case 2 -> {// /alertm /alertmap, /noticem /noticemap, /slidem /slidemap
                client.getPlayer().getMap().broadcastMessage(serverNoticePacket);
                ChatLogger.log(client, "Alert Map", message);
            }
        }
    }
}
