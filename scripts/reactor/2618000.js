
/*2618000.js - MagatiaPQ Beaker
 *@author Ronan
 */

function hit() {
    if (rm.getReactor().getState() == 6) {
        var eim = rm.getEventInstance();

        var done = eim.getIntProperty("statusStg3") + 1;
        eim.setIntProperty("statusStg3", done);

        if (done == 3) {
            eim.showClearEffect();
            eim.giveEventPlayersStageReward(3);
            rm.getMap().killAllMonsters();

            var reactname = (eim.getIntProperty("isAlcadno") == 0) ? "rnj2_door" : "jnr2_door";
            rm.getMap().getReactorByName(reactname).hitReactor(rm.getClient());
        }
    }
}

function act() {}