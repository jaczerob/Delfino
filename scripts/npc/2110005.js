/*
-- Odin JavaScript --------------------------------------------------------------------------------
    Camel Cab - Magatia (GMS Like)
-- Version Info -----------------------------------------------------------------------------------
    1.3 - Actually fixed by Alan (SharpAceX)
    1.2 - Fixed and recoded by Moogra
    1.1 - Shortened by Moogra
    1.0 - First Version by Maple4U - who stepped up and coded first version of several Magatia NPCs
---------------------------------------------------------------------------------------------------
*/

var toMagatia = "Would you like to take the #bCamel Cab#k to #bMagatia#k, the town of Alchemy? The fare is #b1500 mesos#k.";
var toAriant = "Would you like to take the #bCamel Cab#k to #bAriant#k, the town of Burning Roads? The fare is #b1500 mesos#k.";

function start() {
    cm.sendYesNo(cm.getPlayer().getMapId() == 260020000 ? toMagatia : toAriant);
}

function action(mode, type, selection) {
    if (mode == 1) {
        if (cm.getMeso() < 1500) {
            cm.sendNext("I am sorry, but I think you are short on mesos. I am afraid I can't let you ride this if you do not have enough money to do so. Please come back when you have enough money to use this.");
            cm.dispose();
        } else {
            cm.warp(cm.getPlayer().getMapId() == 260020000 ? 261000000 : 260000000, 0);
            cm.gainMeso(-1500);
            cm.dispose();
        }
    } else if (mode == 0) {
        cm.sendNext("Hmmm... too busy to do it right now? If you feel like doing it, though, come back and find me.");
    }
    cm.dispose();
}