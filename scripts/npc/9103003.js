/*
 *@Author Ronan
 * Ludibrium Maze Party Quest
 */

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

        if (status == 0) {
            cm.sendNext("Come this way to return to Ludibrium.");
        } else {
            cm.warp(220000000, 0);
            cm.dispose();
        }
    }
}
