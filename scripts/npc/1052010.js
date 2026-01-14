
/* Shumi JQ Chest #3
*/

function start() {
    prizes = [4010001, 4010002, 4010003, 4010004, 4010005, 4010006, 4010007];
    if (cm.isQuestStarted(2057)) {
        cm.gainItem(4031041, 1);
    } else {
        cm.gainItem(prizes[parseInt(Math.random() * prizes.length)], 1);
    }
    cm.warp(103000100, 0);
    cm.dispose();
}