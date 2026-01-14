
/*@author Ronan
 *Reactor : OrbisPQ Bonus Reactor - 2002014.js
 * Drops all the Bonus Items
 */

function act() {
    rm.dropItems(true, 1, 100, 400, 15);

    var eim = rm.getEventInstance();
    if (eim.getProperty("statusStgBonus") != "1") {
        rm.spawnNpc(2013002, new java.awt.Point(46, 840));
        eim.setProperty("statusStgBonus", "1");
    }
}