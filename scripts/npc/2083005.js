/**
 Fountain of Life 2083005
 **/

function start() {
    if (cm.isQuestStarted(6280)) {
        if (cm.hasItem(4031454)) {
            cm.sendOk("(You poured some water from the fountain into the cup.)");
            cm.gainItem(4031454, -1);
            cm.gainItem(4031455, 1);
        }
    }

    cm.dispose();
}
