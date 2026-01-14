
var status = 0;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status == 0) {
        cm.sendYesNo("Do you wish to leave the genie?");
        status++;
    } else {
        if (mode < 1) {
            cm.dispose();
        } else {
            if (status == 1) {
                cm.sendNext("Alright, see you next time. Take care.");
                status++;
            } else if (status == 2) {
                cm.warp(200000151, 0);
                cm.dispose();
            }
        }
    }
}
