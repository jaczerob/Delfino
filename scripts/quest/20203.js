/* Author: Xterminator
	NPC Name: 		Irena
	Map(s): 		Empress' Road : Ereve (130000000)
	Description: 		Quest - Knighthood Exam: Wind Archer
*/
var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            qm.sendNext("Is there actually a reason why you should stay as a Knight-in-Training?");
            qm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            qm.sendYesNo("#t4032098#... I checked them all. I can tell you are now ready to make the leap as an official knight. Do you want to become one?");
        } else if (status == 1) {
            if (qm.getPlayer().getJob().getId() == 1300 && qm.getPlayer().getRemainingSp() > ((qm.getPlayer().getLevel() - 30) * 3)) {
                qm.sendNext("You have way too much #bSP#k with you. You'll need to spend more SP on 1st-level skills to become an official knight.");
                qm.dispose();
            } else {
                if (qm.getPlayer().getJob().getId() != 1310) {
                    if (!qm.canHold(1142067)) {
                        qm.sendNext("If you wish to receive the medal befitting the title, you may want to make some room in your equipment inventory.");
                        qm.dispose();
                        return;
                    }
                    qm.gainItem(4032098, -30);
                    qm.gainItem(1142067, 1);
                    const Job = Java.type('dev.jaczerob.delfino.maplestory.client.Job');
                    qm.getPlayer().changeJob(Job.WINDARCHER2);
                    qm.completeQuest();
                }
                qm.sendNext("You are no longer a Knight-in-Training. You are now officially a Cygnus Knight.");
            }
        } else if (status == 2) {
            qm.sendNextPrev("I have given you some #bSP#k. I have also given you some skills of Wind Archer that are only available to official knights, so keep working!");
        } else if (status == 3) {
            qm.sendPrev("As an official Cygnus Knight, you should always keep yourself level-headed.");
        } else if (status == 4) {
            qm.dispose();
        }
    }
}