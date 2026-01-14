function start() {
    cm.sendYesNo("Would you like to exit the Guild Quest?");
}

function action(mode, type, selection) {
    if (mode == 1) {
        cm.warp(101030104);
    }
    cm.dispose();
}
