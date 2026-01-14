var status = -1;

function start() {
    cm.sendNext("Thieves are a perfect blend of luck, dexterity, and power that are adept at the surprise attacks against helpless enemies. A high level of avoidability and speed allows Thieves to attack enemies from various angles.");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0) {
            cm.sendNext("If you wish to experience what it's like to be a Thief, come see me again.");
        }
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendYesNo("Would you like to experience what it's like to be a Thief?");
    } else if (status == 1) {
        cm.lockUI();
        cm.warp(1020400, 0);
        cm.dispose();
    }
}