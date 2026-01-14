var status = -1;

function start() {
    cm.sendNext("Bowmen are blessed with dexterity and power, taking charge of long-distance attacks, providing support for those at the front line of the battle. Very adept at using landscape as part of the arsenal.");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0) {
            cm.sendNext("If you wish to experience what it's like to be a Bowman, come see me again.");
        }
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendYesNo("Would you like to experience what it's like to be a Bowman?");
    } else if (status == 1) {
        cm.lockUI();
        cm.warp(1020300, 0);
        cm.dispose();
    }
}