
/*
	Control Device
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
            cm.sendAcceptDecline("You can operate the automated security system using the control unit. Would you like to deactivate the automated security system?");
            return;
        } else if (status == 1) {
            cm.weakenAreaBoss(7090000, "The automated security system has been deactivated. The intruder alarm will shut down.");
        }

        cm.dispose();
    }
}