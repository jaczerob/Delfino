
/* 2001004 - Scarf Snowman
    @author Ronan
 */

var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            cm.sendYesNo("So, are you ready to head out of here?");
        } else if (status == 1) {
            cm.warp(209000000, 3);
            cm.dispose();
        }
    }
} 