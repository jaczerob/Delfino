
/**
 Rupi- Happyville Warp NPC
 **/

function start() {
    cm.sendYesNo("Do you want to get out of Happyville?");
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        var map = cm.getPlayer().getSavedLocation("HAPPYVILLE");
        if (map == -1) {
            map = 101000000;
        }

        cm.warp(map, 0);
    }

    cm.dispose();
}