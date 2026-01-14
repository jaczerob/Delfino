package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.scripting.quest.QuestScriptManager;
import dev.jaczerob.delfino.maplestory.server.life.NPC;
import dev.jaczerob.delfino.maplestory.server.quest.Quest;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.awt.*;

/**
 * @author Matze
 */
@Component
public final class QuestActionHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.QUEST_ACTION;
    }

    // isNpcNearby thanks to GabrielSin
    private static boolean isNpcNearby(InPacket packet, Character player, Quest quest, int npcId) {
        Point playerP;
        Point pos = player.getPosition();

        if (packet.available() >= 4) {
            playerP = new Point(packet.readShort(), packet.readShort());
            if (playerP.distance(pos) > 1000) {     // thanks Darter (YungMoozi) for reporting unchecked player position
                playerP = pos;
            }
        } else {
            playerP = pos;
        }

        if (!quest.isAutoStart() && !quest.isAutoComplete()) {
            NPC npc = player.getMap().getNPCById(npcId);
            if (npc == null) {
                return false;
            }

            Point npcP = npc.getPosition();
            if (Math.abs(npcP.getX() - playerP.getX()) > 1200 || Math.abs(npcP.getY() - playerP.getY()) > 800) {
                player.dropMessage(5, "Approach the NPC to fulfill this quest operation.");
                return false;
            }
        }

        return true;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        byte action = packet.readByte();
        short questid = packet.readShort();
        Character player = client.getPlayer();
        Quest quest = Quest.getInstance(questid);

        switch (action) {
            case 0: // Restore lost item, Credits Darter ( Rajan )
                packet.readInt();
                int itemid = packet.readInt();
                quest.restoreLostItem(player, itemid);
                break;
            case 1: { // Start Quest
                int npc = packet.readInt();
                if (!isNpcNearby(packet, player, quest, npc)) {
                    return;
                }
                if (quest.canStart(player, npc)) {
                    quest.start(player, npc);
                }
                break;
            }
            case 2: { // Complete Quest
                int npc = packet.readInt();
                if (!isNpcNearby(packet, player, quest, npc)) {
                    return;
                }
                if (quest.canComplete(player, npc)) {
                    if (packet.available() >= 2) {
                        int selection = packet.readShort();
                        quest.complete(player, npc, selection);
                    } else {
                        quest.complete(player, npc);
                    }
                }
                break;
            }
            case 3: // forfeit quest
                quest.forfeit(player);
                break;
            case 4: { // scripted start quest
                int npc = packet.readInt();
                if (!isNpcNearby(packet, player, quest, npc)) {
                    return;
                }
                if (quest.canStart(player, npc)) {
                    QuestScriptManager.getInstance().start(client, questid, npc);
                }
                break;
            }
            case 5: { // scripted end quests
                int npc = packet.readInt();
                if (!isNpcNearby(packet, player, quest, npc)) {
                    return;
                }
                if (quest.canComplete(player, npc)) {
                    QuestScriptManager.getInstance().end(client, questid, npc);
                }
                break;
            }
        }
    }
}
