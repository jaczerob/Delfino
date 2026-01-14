/*
 *@Author:  Moogra
 *@NPC:     4th Job Bowman Advancement NPC
 *@Purpose: Handles 4th job.
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (cm.getLevel() < 120 || Math.floor(cm.getJobId() / 100) != 3) {
                cm.sendOk("Please don't bother me right now, I am trying to concentrate.");
                cm.dispose();
            } else if (!cm.isQuestCompleted(6924)) {
                cm.sendOk("You have not yet passed my trials. I can not advance you until you do so.");
                cm.dispose();
            } else if (cm.getJobId() % 100 % 10 != 2) {
                cm.sendYesNo("You did a marvellous job passing my test. Are you ready to advance to your 4th job?");
            } else {
                cm.sendSimple("If I must, I can teach you the art of your class.\r\n#b#L0#Teach me the skills of my class.#l");
                //cm.dispose();
            }
        } else if (status == 1) {
            if (mode >= 1 && cm.getJobId() % 100 % 10 != 2) {
                if (cm.canHold(2280003, 1)) {
                    cm.changeJobById(cm.getJobId() + 1);
                    if (cm.getJobId() == 312) {
                        cm.teachSkill(3121002, 0, 10, -1);
                        cm.teachSkill(3120005, 0, 10, -1);
                        cm.teachSkill(3121007, 0, 10, -1);
                    } else if (cm.getJobId() == 322) {
                        cm.teachSkill(3221002, 0, 10, -1);
                        cm.teachSkill(3220004, 0, 10, -1);
                        cm.teachSkill(3221006, 0, 10, -1);
                    }
                    cm.gainItem(2280003, 1);
                } else {
                    cm.sendOk("Please have one slot available on #bUSE#k inventory to receive a skill book.");
                }
            } else if (mode >= 0 && cm.getJobId() % 100 % 10 == 2) {
                if (cm.getJobId() == 312) {
                    if (cm.getPlayer().getSkillLevel(3121008) == 0) {
                        cm.teachSkill(3121008, 0, 10, -1);
                    }
                    if (cm.getPlayer().getSkillLevel(3121006) == 0) {
                        cm.teachSkill(3121006, 0, 10, -1);
                    }
                    if (cm.getPlayer().getSkillLevel(3121004) == 0) {
                        cm.teachSkill(3121004, 0, 10, -1);
                    }
                } else if (cm.getJobId() == 322) {
                    if (cm.getPlayer().getSkillLevel(3221007) == 0) {
                        cm.teachSkill(3221007, 0, 10, -1);
                    }
                    if (cm.getPlayer().getSkillLevel(3221005) == 0) {
                        cm.teachSkill(3221005, 0, 10, -1);
                    }
                    if (cm.getPlayer().getSkillLevel(3221001) == 0) {
                        cm.teachSkill(3221001, 0, 10, -1);
                    }
                }
                cm.sendOk("It is done. Leave me now.");
            }

            cm.dispose();
        }
    }
}
