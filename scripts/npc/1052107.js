/* Small Street Light
	Kerning deep Subway areas
	Nothing at all.
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

        if (status == 0) {
            cm.sendAcceptDecline("This is a small lamp with a switch. Would you like to turn it on?");
            return;
        } else if (status == 1) {
            cm.weakenAreaBoss(5090000, "You have turned the lamp on. Shade's strength will rapidly weaken due to the light.");
        }

        cm.dispose();
    }
}
