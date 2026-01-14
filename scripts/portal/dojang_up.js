
/*
 * @author:   Moogra
 * @function: Warp character up and award player with dojo points
 * @maps:     All Dojo fighting maps
*/

function enter(pi) {
    try {
        if (pi.getPlayer().getMap().getMonsterById(9300216) != null) {
            pi.goDojoUp();
            pi.getPlayer().getMap().setReactorState();
            var stage = Math.floor(pi.getPlayer().getMapId() / 100) % 100;
            const MapId = Java.type('dev.jaczerob.delfino.maplestory.constants.id.MapId');
            if ((stage - (stage / 6) | 0) == pi.getPlayer().getVanquisherStage() && !MapId.isPartyDojo(pi.getPlayer().getMapId())) // we can also try 5 * stage / 6 | 0 + 1
            {
                pi.getPlayer().setVanquisherKills(pi.getPlayer().getVanquisherKills() + 1);
            }
        } else {
            pi.getPlayer().message("There are still some monsters remaining.");
        }
        pi.enableActions();
        return true;
    } catch (err) {
        pi.getPlayer().dropMessage(err);
    }
}
