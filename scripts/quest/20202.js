/* Author: Xterminator
	NPC Name: 		Oz
	Map(s): 		Empress' Road : Ereve (130000000)
	Description: 		Quest - Knighthood Exam: Blaze Wizard
*/
var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            qm.sendNext("Eh? Why? Is there something wrong?");
            qm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            qm.sendYesNo("You managed to bring all of #t4032097#!!! Okay, I believe that your are now qualified to become an official knight! Do you want to become one?");
        } else if (status == 1) {
            if (qm.getPlayer().getJob().getId() == 1200 && qm.getPlayer().getRemainingSp() > ((qm.getPlayer().getLevel() - 30) * 3)) {
                qm.sendNext("Wa, wait... you have way too much #bSP#k with you. You'll need to spend more SP on 1st-level skills to become an official knight.");
                qm.dispose();
            } else {
                if (qm.getPlayer().getJob().getId() != 1210) {
                    if (!qm.canHold(1142067)) {
                        qm.sendNext("If you wish to receive the medal befitting the title, you may want to make some room in your equipment inventory.");
                        qm.dispose();
                        return;
                    }
                    qm.gainItem(4032097, -30);
                    qm.gainItem(1142067, 1);
                    const Job = Java.type('dev.jaczerob.delfino.maplestory.client.Job');
                    qm.getPlayer().changeJob(Job.BLAZEWIZARD2);
                    qm.completeQuest();
                }
                qm.sendNext("You are no longer a Knight-in-Training. You are now an official Cygnus Knight!");
            }
        } else if (status == 2) {
            qm.sendNextPrev("I have given you some #bSP#k. I have also given you some skills of Blaze Wizards that are only available to official knights, so keep working!");
        } else if (status == 3) {
            qm.sendPrev("Now that you have officially become one, I want you to keep that fire in you that you had when you first started this journey, but this time, as a proud member of the Cygnus Knights!");
        } else if (status == 4) {
            qm.dispose();
        }
    }
}