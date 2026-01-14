/* Konpei
 * 
 * @Author Ronan
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        var eim = cm.getEventInstance();
        if (!eim.isEventCleared()) {
            if (status == 0) {
                cm.sendYesNo("If you leave now, you won't be able to return. Are you sure you want to leave?");
            } else if (status == 1) {
                cm.warp(801040004, 1);
                cm.dispose();
            }
        } else {
            if (status == 0) {
                cm.sendNext("You guys did it, great job! Now our city is free from the tyranny of their mobs! As representative of the city, please accept this as a prize for your efforts, as I get you back to town.");
            }

            if (status == 1) {
                var eim = cm.getEventInstance();
                if (!eim.giveEventReward(cm.getPlayer())) {
                    cm.sendNext("Please make room on your inventory first...");
                } else {
                    cm.warp(801040101);
                }

                cm.dispose();
            }
        }
    }
}