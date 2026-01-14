/**
 *2013002.js - Minerva the Goddess
 *@author Ronan
 */
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        status++;
        if (cm.getPlayer().getMapId() == 920010100) { //Center tower
            if (status == 0) {
                cm.sendYesNo("I have lifted the spell that was blocking the access to the tower's jail storages. You may find some goodies down there... Alternatively, you may want to be leaving now. Are you ready to exit?");
            } else if (status == 1) {
                cm.warp(920011300, 0);
                cm.dispose();
            }

        } else if (cm.getPlayer().getMapId() == 920011100) {
            if (status == 0) {
                cm.sendYesNo("So, are you ready to exit?");
            } else if (status == 1) {
                cm.warp(920011300, 0);
                cm.dispose();
            }

        } else if (cm.getPlayer().getMapId() == 920011300) {
            if (status == 0) {
                cm.sendNext("Thank you for not only restoring the statue, but rescuing me, Minerva, from the entrapment. May the blessing of the goddess be with you till the end... As a token of gratitude, please accept this memento for your bravery.");
            } else if (status == 1) {
                if (cm.getEventInstance().giveEventReward(cm.getPlayer())) {
                    cm.warp(200080101, 0);
                    cm.dispose();
                } else {
                    cm.sendOk("Please make room on your inventory first.");
                    cm.dispose();
                }
            }
        }
    }
}