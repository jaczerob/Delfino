/*
	Author : 		Ronan
	NPC Name: 	        Yulete
	Map(s): 		Magatia
	Description: 		Quest - Yulete's Reward
	Quest ID: 		3382
*/

var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (qm.haveItem(4001159, 25) && qm.haveItem(4001160, 25) && !qm.haveItemWithId(1122010, true)) {
                if (qm.canHold(1122010)) {
                    qm.gainItem(4001159, -25);
                    qm.gainItem(4001160, -25);
                    qm.gainItem(1122010, 1);

                    qm.sendOk("Thank you for retrieving the marbles. Accept this pendant as a token of my appreciation.");
                } else {
                    qm.sendNext("Free a slot on your EQUIP tab before claiming a prize.");
                    return;
                }
            } else if (qm.haveItem(4001159, 10) && qm.haveItem(4001160, 10)) {
                if (qm.canHold(2041212)) {
                    qm.gainItem(4001159, -10);
                    qm.gainItem(4001160, -10);
                    qm.gainItem(2041212, 1);

                    qm.sendOk("Thank you for retrieving the marbles. This rock, that I am giving to you, can be used to improve the stats on the #b#t1122010##k. Take it as a token of my appreciation and use it wisely.");
                } else {
                    qm.sendNext("Free a slot on your USE tab before claiming a prize.");
                    return;
                }
            } else {
                qm.sendNext("I need at least #b10 of both #t4001159# and #t4001160##k to reward you appropriately. If you happen to come with #b25 of these#k instead, I can reward you with a valuable gear. Fare well.");
                return;
            }

            qm.forceCompleteQuest();
        } else if (status == 1) {
            qm.dispose();
        }
    }
}
