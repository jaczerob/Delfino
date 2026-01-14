/*
@	Author : Ronan
@
@	NPC = Nuris (9040001)
@	Map = Sharenian - Returning Path
@	NPC MapId = 990001100
@	NPC Exit-MapId = 101030104
@
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        cm.dispose();
        return;
    }

    if (status == 0) {
        var outText = "It seems you have finished exploring Sharenian Keep, yes? Are you going to return to the recruitment map now?";
        cm.sendYesNo(outText);
    } else if (mode == 1) {
        var eim = cm.getEventInstance();

        if (eim != null && eim.isEventCleared()) {
            if (!eim.giveEventReward(cm.getPlayer())) {
                cm.sendNext("It seems you don't have a free slot in either your #rEquip#k, #rUse#k or #rEtc#k inventories. Please make some room first.");
            } else {
                cm.warp(101030104);
            }

            cm.dispose();
        } else {
            cm.warp(101030104);
            cm.dispose();
        }
    }
}