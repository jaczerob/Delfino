/*
	Author : Ronan Lana
*/

var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (!qm.haveItem(4031619, 1)) {
                qm.sendOk("Please bring me the box with the supplies that lies with #b#p2012019##k...");
            } else {
                qm.gainItem(4031619, -1);
                qm.sendOk("Oh, you brought #p2012019#'s box! Thank you.");
                qm.forceCompleteQuest();
            }
        } else if (status == 1) {
            qm.dispose();
        }
    }
}