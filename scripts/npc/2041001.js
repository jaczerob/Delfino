/**
 Rosey (On Train) 2041001
 **/

var status = 0;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status == 0) {
        cm.sendYesNo("Do you wish to leave the train?");
        status++;
    } else {
        if ((status == 1 && type == 1 && selection == -1 && mode == 0) || mode == -1) {
            cm.dispose();
        } else {
            if (status == 1) {
                cm.sendNext("Alright, see you next time. Take care.");
                status++;
            } else if (status == 2) {
                if (cm.getPlayer().getMapId() == 200000122) {
                    cm.warp(200000121, 0);
                }// back to orbis
                else {
                    cm.warp(220000110, 0);
                }// back to Ludi
                cm.dispose();
            }
        }
    }
}
