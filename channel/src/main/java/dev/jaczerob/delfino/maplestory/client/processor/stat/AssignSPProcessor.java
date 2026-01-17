package dev.jaczerob.delfino.maplestory.client.processor.stat;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.Skill;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssignSPProcessor {
    private static final Logger log = LoggerFactory.getLogger(AssignSPProcessor.class);

    public static boolean canSPAssign(Client c, int skillid) {
        Character player = c.getPlayer();
        if ((!player.isGM() && GameConstants.isGMSkills(skillid)) || (!GameConstants.isInJobTree(skillid, player.getJob().getId()) && !player.isGM())) {
            AutobanFactory.PACKET_EDIT.alert(player, "tried to packet edit in distributing sp.");
            log.warn("Chr {} tried to use skill {} without it being in their job.", c.getPlayer().getName(), skillid);

            c.disconnect(true, false);
            return false;
        }

        return true;
    }

    public static void SPAssignAction(Client c, int skillid) {
        c.lockClient();
        try {
            if (!canSPAssign(c, skillid)) {
                return;
            }

            Character player = c.getPlayer();
            int remainingSp = player.getRemainingSps()[GameConstants.getSkillBook(skillid / 10000)];
            boolean isBeginnerSkill = false;

            if (skillid % 10000000 > 999 && skillid % 10000000 < 1003) {
                int total = 0;
                for (int i = 0; i < 3; i++) {
                    total += player.getSkillLevel(SkillFactory.getSkill(player.getJobType() * 10000000 + 1000 + i));
                }
                remainingSp = Math.min((player.getLevel() - 1), 6) - total;
                isBeginnerSkill = true;
            }
            Skill skill = SkillFactory.getSkill(skillid);
            int curLevel = player.getSkillLevel(skill);
            if ((remainingSp > 0 && curLevel + 1 <= (skill.isFourthJob() ? player.getMasterLevel(skill) : skill.getMaxLevel()))) {
                if (!isBeginnerSkill) {
                    player.gainSp(-1, GameConstants.getSkillBook(skillid / 10000), false);
                } else {
                    player.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                }
                player.changeSkillLevel(skill, (byte) (curLevel + 1), player.getMasterLevel(skill), player.getSkillExpiration(skill));
            }
        } finally {
            c.unlockClient();
        }
    }
}
