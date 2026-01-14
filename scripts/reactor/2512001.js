
/*2512001.js
 *@Author Ronan
 *Pirate PQ Treasure chest
 */

function act() {
    var eim = rm.getPlayer().getEventInstance();
    var now = eim.getIntProperty("openedChests");
    var nextNum = now + 1;
    eim.setIntProperty("openedChests", nextNum);
    rm.sprayItems(true, 1, 50, 100, 15);
}