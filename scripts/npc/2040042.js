/*
@	Author : Raz
@       Author : Ronan
@
@	NPC = Sky-Blue Balloon
@	Map = Hidden-Street <Stage 7>
@	NPC MapId = 922010700
@	Function = LPQ - 7 Stage
@
@	Description: You need a ranged person here. The ranged person must kill the three Ratz, and they'll trigger something. What's next is for you to find out! Get me 3 passes!
*/

var status = 0;
var curMap, stage;

function start() {
    curMap = cm.getMapId();
    stage = Math.floor((curMap - 922010100) / 100) + 1;

    status = -1;
    action(1, 0, 0);
}

function clearStage(stage, eim, curMap) {
    eim.setProperty(stage + "stageclear", "true");
    eim.showClearEffect(true);

    eim.linkToNextStage(stage, "lpq", curMap);  //opens the portal to the next map
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else if (mode == 0) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        var eim = cm.getPlayer().getEventInstance();

        if (eim.getProperty(stage.toString() + "stageclear") != null) {
            cm.sendNext("Hurry, goto the next stage, the portal is open!");
        } else {
            if (eim.isEventLeader(cm.getPlayer())) {
                var state = eim.getIntProperty("statusStg" + stage);

                if (state == -1) {           // preamble
                    cm.sendOk("Hi. Welcome to the #bstage " + stage + "#k. You need ranged personnel here. They must kill the three Ratz, which will trigger something. What's next is for you to find out! Get me 3 passes!");
                    eim.setProperty("statusStg" + stage, 0);
                } else if (state == 0) {       // check stage completion
                    if (cm.haveItem(4001022, 3)) {
                        cm.sendOk("Good job! You have collected all 3 #b#t4001022#'s.#k");
                        cm.gainItem(4001022, -3);

                        eim.setProperty("statusStg" + stage, 1);
                        clearStage(stage, eim, curMap);
                    } else {
                        cm.sendNext("Sorry you don't have all 3 #b#t4001022#'s.#k");
                    }
                }
            } else {
                cm.sendNext("Please tell your #bParty-Leader#k to come talk to me.");
            }
        }

        cm.dispose();
    }
}