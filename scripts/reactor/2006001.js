/**
 *2006001.js - Spawns Minerva
 *@author Ronan
 */

function act() {
    rm.spawnNpc(2013002);
    rm.getEventInstance().clearPQ();

    rm.getEventInstance().setProperty("statusStg8", "1");
    eim.giveEventPlayersExp(3500);
    eim.showClearEffect(true);

    rm.getEventInstance().startEventTimer(5 * 60000); //bonus time
}