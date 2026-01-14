/* 	Engine room - Bob
 * 	@author Ronan
*/

var eim;
var status = 0;

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

        eim = cm.getEventInstance();
        if (status == 0) {
            if (!eim.isEventCleared()) {
                cm.sendYesNo("Are you ready to leave this place?");
            } else {
                cm.sendYesNo("You have defeated Capt. Latanica, well done! Are you ready to leave this place?");
            }
        } else if (status == 1) {
            if (eim.isEventCleared()) {
                if (!eim.giveEventReward(cm.getPlayer())) {
                    cm.sendOk("Please make a room on your inventory to receive the loot.");
                    cm.dispose();
                    return;
                }
            }

            cm.warp(541010110, 0);
            cm.dispose();
        }
    }
}
