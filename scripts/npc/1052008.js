
/* Shumi JQ Chest #1
*/

function start() {
    prizes = [4020000, 4020001, 4020002, 4020003, 4020004];
    if (cm.isQuestStarted(2055)) {
        cm.gainItem(4031039, 1);
    } else {
        cm.gainItem(4020000 + ((Math.random() * 5) | 0), 1);
    }
    cm.warp(103000100, 0);
    cm.dispose();
}