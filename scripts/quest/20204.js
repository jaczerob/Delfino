/* Author: Xterminator
	NPC Name: 		Eckhart
	Map(s): 		Empress' Road : Ereve (130000000)
	Description: 		Quest - Knighthood Exam: Night Walker
*/
var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            qm.sendNext("What's holding you back?");
            qm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            qm.sendYesNo("So you brought all the #t4032099#s with you. This is much b... way, I shouldn't congratulate you for doing something that you should be doing. At least, I can tell that you are now qualified to become an official knight. Do you want to become one right now?");
        } else if (status == 1) {
            if (qm.getPlayer().getJob().getId() == 1400 && qm.getPlayer().getRemainingSp() > ((qm.getPlayer().getLevel() - 30) * 3)) {
                qm.sendNext("What's with all this #bSP#k lying around? Use more SP on your 1st-level skills.");
                qm.dispose();
            } else {
                if (qm.getPlayer().getJob().getId() != 1410) {
                    if (!qm.canHold(1142067)) {
                        qm.sendNext("If you wish to receive the medal befitting the title, you may want to make some room in your equipment inventory.");
                        qm.dispose();
                        return;
                    }
                    qm.gainItem(4032099, -30);
                    qm.gainItem(1142067, 1);
                    const Job = Java.type('dev.jaczerob.delfino.maplestory.client.Job');
                    qm.getPlayer().changeJob(Job.NIGHTWALKER2);
                    qm.completeQuest();
                }
                qm.sendNext("You are no longer a Knight-in-Training. You have officially become a Cygnus Knight.");
            }
        } else if (status == 2) {
            qm.sendNextPrev("I have given you some #bSP#k. I have also given you some skills of Night Walker that are only available to official knights, so keep working!");
        } else if (status == 3) {
            qm.sendPrev("As a member of the Cygnus Knights, I hope you remain unaffected by temptations and stay strong.");
        } else if (status == 4) {
            qm.dispose();
        }
    }
}