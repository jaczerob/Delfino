
/*
@	Author : Raz
@       Author : Ronan
@
@	NPC = Pink Balloon
@	Map = Hidden-Street <Stage B>
@	NPC MapId = 922011000
@	Function = LPQ - B Stage
@
*/

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();//ExitChat
    } else if (mode == 0) {
        cm.sendOk("Wise choice. Who wouldn't want free mesos from the #bBonus Stage#k?");
        cm.dispose();//No
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            cm.sendYesNo("Would you like to leave the bonus stage?");
        } else {
            cm.warp(922011100, "st00");
            cm.dispose();
        }
    }
}