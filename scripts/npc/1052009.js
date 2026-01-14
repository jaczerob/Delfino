
/* Shumi JQ Chest #2
*/

function start() {
    prizes = [4020005, 4020006, 4020007, 4020008, 4010000];
    if (cm.isQuestStarted(2056)) {
        cm.gainItem(4031040, 1);
    } else {
        cm.gainItem(prizes[parseInt(Math.random() * prizes.length)], 1);
    }
    cm.warp(103000100, 0);
    cm.dispose();
}