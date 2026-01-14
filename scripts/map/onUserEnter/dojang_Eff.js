
/*
 *@Author:     Moogra, Traitor, Ronan
 *@Map(s):     All Dojo fighting maps
 *@Function:   Displays info for the player when entering a dojo map
*/


function start(ms) {
    ms.getPlayer().resetEnteredScript();
    var stage = Math.floor(ms.getPlayer().getMap().getId() / 100) % 100;

    ms.getPlayer().showDojoClock();
    if (stage % 6 > 0) {
        var realstage = stage - ((stage / 6) | 0);
        ms.dojoEnergy();

        ms.playSound("Dojang/start");
        ms.showEffect("dojang/start/stage");
        ms.showEffect("dojang/start/number/" + realstage);
    }
}