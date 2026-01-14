
/* Mark the Toy Soldier
*/

var greeting;
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
            var greeting = "Thank you for finding the pendulum. Are you ready to return to Eos Tower?";
            if (cm.isQuestStarted(3230)) {
                if (cm.haveItem(4031094)) {
                    cm.completeQuest(3230);
                    cm.gainItem(4031094, -1);
                } else {
                    greeting = "You haven't found the pendulum yet. Do you want to go back to Eos Tower?";
                }
            }
            cm.sendYesNo(greeting);
        } else if (status == 1) {
            cm.warp(221024400, 4);
            cm.dispose();
        }
    }
}