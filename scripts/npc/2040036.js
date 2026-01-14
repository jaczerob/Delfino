/*
@	Author : Raz
@       Author : Ronan
@
@	NPC = Red Balloon
@	Map = Hidden-Street <Stage 1>
@	NPC MapId = 922010100
@	Function = LPQ - 1st Stage
@
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
                    cm.sendOk("Hi. Welcome to the #bstage " + stage + "#k. Collect 25 #t4001022#'s scattered across the map, then talk to me.");
                    eim.setProperty("statusStg" + stage, 0);
                } else {       // check stage completion
                    if (cm.haveItem(4001022, 25)) {
                        cm.sendOk("Good job! You have collected all 25 #b#t4001022#'s.#k");
                        cm.gainItem(4001022, -25);

                        eim.setProperty("statusStg" + stage, 1);
                        clearStage(stage, eim, curMap);
                    } else {
                        cm.sendNext("Sorry you don't have all 25 #b#t4001022#'s.#k");
                    }
                }
            } else {
                cm.sendNext("Please tell your #bParty-Leader#k to come talk to me.");
            }
        }

        cm.dispose();
    }
}