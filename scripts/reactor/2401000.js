
/* @Author Aexr, Ronan
 * 2401000.js: Horntail's Cave - Summons Horntail.
*/

function act() {
    rm.changeMusic("Bgm14/HonTale");
    if (rm.getReactor().getMap().getMonsterById(8810026) == null) {
        rm.getReactor().getMap().spawnHorntailOnGroundBelow(new java.awt.Point(71, 260));

        var eim = rm.getEventInstance();
        eim.restartEventTimer(60 * 60000);
    }
    rm.mapMessage(6, "From the depths of his cave, here comes Horntail!");
}