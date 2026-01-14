var status = -1;

function start() {
    cm.sendNext("Warriors possess an enormous power with stamina to back it up, and they shine the brightest in melee combat situation. Regular attacks are powerful to begin with, and armed with complex skills, the job is perfect for explosive attacks.");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0) {
            cm.sendNext("If you wish to experience what it's like to be a Warrior, come see me again.");
        }
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendYesNo("Would you like to experience what it's like to be a Warrior?");
    } else if (status == 1) {
        cm.lockUI();
        cm.warp(1020100, 0);
        cm.dispose();
    }
}