/* Author: PurpleMadness
 * The sorcerer who sells emotions
*/

var status = -1;

function start(mode, type, selection) {
    if (qm.getPlayer().getMeso() >= 1000000) {
        if (qm.canHold(2022337, 1)) {
            qm.gainItem(2022337, 1);
            qm.gainMeso(-1000000);

            //qm.sendOk("Nice doing business with you~~.");
            qm.startQuest(3514);
        } else {
            qm.sendOk("Check out for a slot on your USE inventory first.");
        }
    } else {
        qm.sendOk("Oy, you don't have the money. I charge #r1,000,000 mesos#k for the emotion potion. No money, no deal.");
    }

    qm.dispose();
}

function usedPotion(ch) {
    const BuffStat = Java.type('dev.jaczerob.delfino.maplestory.client.BuffStat');
    return ch.getBuffSource(BuffStat.HPREC) == 2022337;
}

function end(mode, type, selection) {
    if (mode == 0 && type == 0) {
        status--;
    } else if (mode == -1) {
        qm.dispose();
        return;
    } else {
        status++;
    }

    if (status == 0) {
        if (!usedPotion(qm.getPlayer())) {
            if (qm.haveItem(2022337)) {
                qm.sendOk("Are you scared to drink the potion? I can assure you it has only a minor #rside effect#k.");
            } else {
                if (qm.canHold(2022337)) {
                    qm.gainItem(2022337, 1);
                    qm.sendOk("Lost it? Luckily for you I managed to recover it back. Take it.");
                } else {
                    qm.sendOk("Lost it? Luckily for you I managed to recover it back. Make a room to get it.");
                }
            }

            qm.dispose();

        } else {
            qm.sendOk("It seems the potion worked and your emotions are no longer frozen. And, oh, my... You're ailing bad, #bpurge#k that out quickly.");
        }
    } else if (status == 1) {
        qm.gainExp(891500);
        qm.completeQuest(3514);
        qm.dispose();
    }
}