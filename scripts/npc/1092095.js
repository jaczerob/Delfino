
status = -1;

function start() {
    if (cm.haveItem(4031847)) {
        cm.sendNext("The hungry calf is drinking all the milk! The bottle remains empty...");
    } else if (cm.haveItem(4031848) || cm.haveItem(4031849) || cm.haveItem(4031850)) {
        cm.sendNext("The hungry calf is drinking all the milk! The bottle is now empty.");
        if (cm.haveItem(4031848)) {
            cm.gainItem(4031848, -1);
        } else if (cm.haveItem(4031849)) {
            cm.gainItem(4031849, -1);
        } else {
            cm.gainItem(4031850, -1);
        }
        cm.gainItem(4031847, 1);
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else if (mode == 0) {
        status--;
        start();
    } else {
        status++;
    }
    if (status == 0) {
        cm.sendPrev("The hungry calf isn't interested in the empty bottle.");
    } else if (status == 1) {
        cm.dispose();
    }
}