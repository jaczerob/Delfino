
/*
2511000- Reactor for PPQ [Pirate PQ]
@author Jvlaple
*/

function act() {
    var eim = rm.getPlayer().getEventInstance();
    var now = eim.getIntProperty("openedBoxes");
    var nextNum = now + 1;
    eim.setIntProperty("openedBoxes", nextNum);

    rm.spawnMonster(9300109, 3);
    rm.spawnMonster(9300110, 5);
}