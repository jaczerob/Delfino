var status = -1;

function start() {
    cm.sendYesNo("Are you done with your training? If you wish, I will send you out from this training camp.");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0) {
            cm.sendOk("Haven't you finished the training program yet? If you want to leave this place, please do not hesitate to tell me.");
        }
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendNext("Then, I will send you out from here. Good job.");
    } else {
        cm.warp(40000, 0);
        cm.dispose();
    }
}