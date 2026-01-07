package dev.jaczerob.delfino.maplestory.net.server.channel.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Character.DelayedQuestUpdate;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.QuestStatus;
import dev.jaczerob.delfino.maplestory.net.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.net.packet.InPacket;
import dev.jaczerob.delfino.maplestory.scripting.quest.QuestScriptManager;
import dev.jaczerob.delfino.maplestory.server.quest.Quest;

/**
 * @author Xari
 */
public class RaiseUIStateHandler extends AbstractPacketHandler {

    @Override
    public final void handlePacket(InPacket p, Client c) {
        int infoNumber = p.readShort();

        if (c.tryacquireClient()) {
            try {
                Character chr = c.getPlayer();
                Quest quest = Quest.getInstanceFromInfoNumber(infoNumber);
                QuestStatus mqs = chr.getQuest(quest);

                QuestScriptManager.getInstance().raiseOpen(c, (short) infoNumber, mqs.getNpc());

                if (mqs.getStatus() == QuestStatus.Status.NOT_STARTED) {
                    quest.forceStart(chr, 22000);
                    c.getAbstractPlayerInteraction().setQuestProgress(quest.getId(), infoNumber, 0);
                } else if (mqs.getStatus() == QuestStatus.Status.STARTED) {
                    chr.announceUpdateQuest(DelayedQuestUpdate.UPDATE, mqs, mqs.getInfoNumber() > 0);
                }
            } finally {
                c.releaseClient();
            }
        }
    }
}