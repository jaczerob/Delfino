/* Author: 		ThreeStep
	NPC Name: 		Eckhart (1101006)
	Description: 	Night Walker 3rd job advancement
	Quest: 			Shinsoo's Teardrop
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (status == 1 && mode == 0) {
            qm.sendNext("Come back when you are ready.");
            qm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            qm.sendNext("The jewel you brought back from the Master of Disguise is Shinsoo's Teardrop. It is the crystalization of Shinsoo's powers. If the Black Mage gets his hands on this, then this spells doom for all of us.");
        } else if (status == 1) {
            qm.sendYesNo("The Empress thought highly of your accomplishment and granted you a new title. Would you like to receive it?");
        } else if (status == 2) {
            nPSP = (qm.getPlayer().getLevel() - 70) * 3;
            if (qm.getPlayer().getRemainingSp() > nPSP) {
                qm.sendNext("You still have way too much #bSP#k with you. You can't earn a new title like that, I strongly urge you to use more SP on your 1st and 2nd level skills.");
            } else {
                if (!qm.canHold(1142068)) {
                    qm.sendNext("If you wish to receive the medal befitting the title, you may want to make some room in your equipment inventory.");
                } else {
                    qm.gainItem(1142068, 1);
                    const Job = Java.type('dev.jaczerob.delfino.maplestory.client.Job');
                    qm.getPlayer().changeJob(Job.NIGHTWALKER3);
                    qm.completeQuest();
                    qm.sendOk("#h #, from here on out, you are an Advanced Knight of Cygnus Knights. The title comes with a newfound broad view on everything. You may encounter temptations here and there, but I want you to keep your faith and beliefs intact and do not succumb to the darkness.");
                }
            }
        } else if (status == 3) {
            qm.dispose();
        }
    }
}