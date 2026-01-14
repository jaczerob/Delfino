/* Author: Xterminator
	NPC Name: 		Hawkeye
	Map(s): 		Empress' Road : Ereve (130000000)
	Description: 		Quest - Knighthood Exam: Thunder Breaker
*/
var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            qm.sendNext("Hmm? Why? What's holding you back?");
            qm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            qm.sendYesNo("Oh, you brought all #t4032100#s! Ahaha, I knew you'd be good at it. Tell you what, I now commend that you're now ready to become an official knight. Do you want to become one right now?");
        } else if (status == 1) {
            if (qm.getPlayer().getJob().getId() == 1500 && qm.getPlayer().getRemainingSp() > ((qm.getPlayer().getLevel() - 30) * 3)) {
                qm.sendNext("Hey, how did you manage to hunt all that? You have way too much #bSP#k lying around unused! You can't become an official knight like this! Use more SP on the 1st level skill.");
                qm.dispose();
            } else {
                if (qm.getPlayer().getJob().getId() != 1510) {
                    if (!qm.canHold(1142067)) {
                        qm.sendNext("If you wish to receive the medal befitting the title, you may want to make some room in your equipment inventory.");
                        qm.dispose();
                        return;
                    }
                    qm.gainItem(4032100, -30);
                    qm.gainItem(1142067, 1);
                    const Job = Java.type('dev.jaczerob.delfino.maplestory.client.Job');
                    qm.getPlayer().changeJob(Job.THUNDERBREAKER2);
                    qm.completeQuest();
                }
                qm.sendNext("You are now no longer a Knight-in-Training. You have now officially become a Cygnus Knight.");
            }
        } else if (status == 2) {
            qm.sendNextPrev("I have also given you some #bSP#k and the accompanying skills of a Thunder Breakers that are only available to the official knights. These skills are lightning-based, so use them wisely!");
        } else if (status == 3) {
            qm.sendPrev("Well, personally, I hope you don't lose your enthusiasm even after becoming the Cygnus Knights. Always seek out the positive even if you're in the midst of a barrage of negative items.");
        } else if (status == 4) {
            qm.dispose();
        }
    }
}