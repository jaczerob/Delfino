
/* @Author Ronan
 * 
 * 2001002.js: Either spawns a PQ mob or drops the Statue piece.
*/

function act() {
    if (rm.getEventInstance().getIntProperty("statusStg2") == -1) {
        var rnd = Math.max(Math.floor(Math.random() * 14), 4);

        rm.getEventInstance().setProperty("statusStg2", "" + rnd);
        rm.getEventInstance().setProperty("statusStg2_c", "0");
    }

    var limit = rm.getEventInstance().getIntProperty("statusStg2");
    var count = rm.getEventInstance().getIntProperty("statusStg2_c");
    if (count >= limit) {
        rm.dropItems();

        var eim = rm.getEventInstance();
        eim.giveEventPlayersExp(3500);

        eim.setProperty("statusStg2", "1");
        eim.showClearEffect(true);
    } else {
        count++;
        rm.getEventInstance().setProperty("statusStg2_c", count);

        var nextHashed = (11 * (count)) % 14;

        var nextPos = rm.getMap().getReactorById(2001002 + nextHashed).getPosition();
        rm.spawnMonster(9300040, 1, nextPos);
    }
}