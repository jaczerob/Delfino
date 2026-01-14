
/*
	Incomplete Magic Square
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
            cm.sendAcceptDecline("This Magic Pentagram is incomplete. Would you like to finish off the drawing of the Magic Pentagram?");
            return;
        } else if (status == 1) {
            cm.weakenAreaBoss(8090000, "The Magic Pentagram has been completed. The spell to eliminate Deet and Roi has been summoned.");
        }

        cm.dispose();
    }
}