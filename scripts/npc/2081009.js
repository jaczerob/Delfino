//Moose, Warps to exit

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode <= 0) {
        cm.dispose();
        return;
    }

    status++;
    if (status == 0) {
        if (cm.isQuestStarted(6180) && cm.getQuestProgressInt(6180, 9300096) < 200) {
            cm.sendYesNo("Pay attention: during the time you stay inside the training ground make sure you #bhave equipped your #t1092041##k, it is of the utmost importance. Are you ready to proceed to the training area?");
        } else {
            cm.sendOk("Only assigned personnel can access the training ground.");
            cm.dispose();
        }
    } else if (status == 1) {
        if (cm.getPlayer().haveItemEquipped(1092041)) {
            cm.sendNext("Have your shield equipped until the end of the quest, or else you will need to start all over again!");
        } else {
            cm.sendOk("Please equip the #r#t1092041##k before entering the training ground.");
            cm.dispose();
        }
    } else {
        cm.warp(924000001, 0);
        cm.dispose();
    }
}
