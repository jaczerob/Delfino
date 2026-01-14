/* NPC:     Thomas Swift
 * Maps:    100000000, 680000000
 * Author:  Moogra
 * Purpose: Amoria warper.
*/

status = -1;

function start() {
    if (cm.getPlayer().getMapId() == 100000000) {
        cm.sendYesNo("I can take you to the Amoria Village. Are you ready to go?");
    } else {
        cm.sendYesNo("I can take you back to Henesys. Are you ready to go?");
    }
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0) {
            cm.sendOk("Ok, feel free to hang around until you're ready to go!");
        }
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendNext("I hope you had a great time! See you around!");
    } else if (status == 1) {
        if (cm.getPlayer().getMapId() == 100000000) {
            cm.warp(680000000, 0);
        } else {
            cm.warp(100000000, 5);
        }
        cm.dispose();
    }
}