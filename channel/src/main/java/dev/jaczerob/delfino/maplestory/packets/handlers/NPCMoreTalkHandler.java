package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.scripting.npc.NPCScriptManager;
import dev.jaczerob.delfino.maplestory.scripting.quest.QuestScriptManager;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Matze
 */
@Component
public final class NPCMoreTalkHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.NPC_TALK_MORE;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        byte lastMsg = packet.readByte(); // 00 (last msg type I think)
        byte action = packet.readByte(); // 00 = end chat, 01 == follow
        if (lastMsg == 2) {
            if (action != 0) {
                String returnText = packet.readString();
                if (client.getQM() != null) {
                    client.getQM().setGetText(returnText);
                    if (client.getQM().isStart()) {
                        QuestScriptManager.getInstance().start(client, action, lastMsg, -1);
                    } else {
                        QuestScriptManager.getInstance().end(client, action, lastMsg, -1);
                    }
                } else {
                    client.getCM().setGetText(returnText);
                    NPCScriptManager.getInstance().action(client, action, lastMsg, -1);
                }
            } else if (client.getQM() != null) {
                client.getQM().dispose();
            } else {
                client.getCM().dispose();
            }
        } else {
            int selection = -1;
            if (packet.available() >= 4) {
                selection = packet.readInt();
            } else if (packet.available() > 0) {
                selection = packet.readUnsignedByte();
            }
            if (client.getQM() != null) {
                if (client.getQM().isStart()) {
                    QuestScriptManager.getInstance().start(client, action, lastMsg, selection);
                } else {
                    QuestScriptManager.getInstance().end(client, action, lastMsg, selection);
                }
            } else if (client.getCM() != null) {
                NPCScriptManager.getInstance().action(client, action, lastMsg, selection);
            }
        }
    }
}