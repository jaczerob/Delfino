
/*2512000.js
 *@Author Jvlaple
 *Pirate PQ Reactor
 */

function passedGrindMode(map, eim) {
    if (eim.getIntProperty("grindMode") == 0) {
        return true;
    }
    return eim.activatedAllReactorsOnMap(map, 2511000, 2517999);
}

function act() {
    var eim = rm.getPlayer().getEventInstance();
    var now = eim.getIntProperty("openedBoxes");
    var nextNum = now + 1;
    eim.setIntProperty("openedBoxes", nextNum);

    rm.dropItems(true, 1, 30, 60, 15);

    var map = rm.getMap();
    if (map.getMonsters().size() == 0 && passedGrindMode(map, eim)) {
        eim.showClearEffect(map.getId());
    }
}