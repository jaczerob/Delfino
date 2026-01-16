package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Character.DelayedQuestUpdate;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.QuestStatus;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.scripting.quest.QuestScriptManager;
import dev.jaczerob.delfino.maplestory.server.quest.Quest;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Xari
 */
@Component
public class RaiseUIStateHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.OPEN_ITEMUI;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int infoNumber = packet.readShort();

        if (client.tryacquireClient()) {
            try {
                Character chr = client.getPlayer();
                Quest quest = Quest.getInstanceFromInfoNumber(infoNumber);
                QuestStatus mqs = chr.getQuest(quest);

                QuestScriptManager.getInstance().raiseOpen(client, (short) infoNumber, mqs.getNpc());

                if (mqs.getStatus() == QuestStatus.Status.NOT_STARTED) {
                    quest.forceStart(chr, 22000);
                    client.getAbstractPlayerInteraction().setQuestProgress(quest.getId(), infoNumber, 0);
                } else if (mqs.getStatus() == QuestStatus.Status.STARTED) {
                    chr.announceUpdateQuest(DelayedQuestUpdate.UPDATE, mqs, mqs.getInfoNumber() > 0);
                }
            } finally {
                client.releaseClient();
            }
        }
    }
}