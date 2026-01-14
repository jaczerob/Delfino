
/* NPC Base
	Map Name (Map ID)
	Extra NPC info.
 */

function start() {
    status = -1;
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
        if (status == 0 && mode == 1) {
            cm.sendYesNo("Would you like to exit the drill hall?");
        } else if (status == 1) {
            cm.warp(130020000, 0);
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}