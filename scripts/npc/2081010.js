/*
Moose
Warps to exit map etc.
*/

var status;
var exitMap = 240010400;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode <= 0) {
        cm.dispose();
        return;
    }

    status++;
    if (status == 0) {
        cm.sendYesNo("Do you want to exit the area? If you quit, you will need to start this task from the scratch.");
    } else if (status == 1) {
        cm.warp(exitMap, "st00");
        cm.dispose();
    }
}
