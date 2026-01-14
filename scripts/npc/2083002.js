
/**
 *Crystal of Roots
 *@Author: Ronan
 *@NPC: Crystal of Roots
 */
function start() {
    cm.sendYesNo("Do you wish to leave?");
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        if (cm.getMapId() > 240050400) {
            cm.warp(240050600);
        } else {
            cm.warp(240040700, "out00");
        }

        cm.dispose();
    }
}