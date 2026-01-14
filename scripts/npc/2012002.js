
/**
 * @Author: Moogra
 * @NPC ID: 2012002
 * @NPC   : Erin (On Orbis Boat )
 */

var status = 0;

function start() {
    cm.sendYesNo("Do you wish to leave the boat?");
}

function action(mode, type, selection) {
    if (mode == 0 && status == 1) {
        cm.sendOk("Good choice");
        cm.dispose();
    }
    if (mode > 0) {
        status++;
    } else {
        cm.dispose();
    }

    if (status == 1) {
        cm.sendNext("Alright, see you next time. Take care.");
    } else if (status == 2) {
        cm.warp(200000111, 0);// back to Orbis jetty
        cm.dispose();
    }
}
