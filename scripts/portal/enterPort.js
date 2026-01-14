
function enter(pi) {
    if (pi.isQuestStarted(21301) && pi.getQuestProgressInt(21301, 9001013) == 0) {
        if (pi.getPlayerCount(108010700) != 0) {
            pi.message("The portal is blocked from the other side. I wonder if someone is already fighting the Thief Crow?");
            return false;
        } else {
            var map = pi.getClient().getChannelServer().getMapFactory().getMap(108010700);
            spawnMob(2732, 3, 9001013, map);

            pi.playPortalSound();
            pi.warp(108010700, "west00");
        }
    } else {
        pi.playPortalSound();
        pi.warp(140020300, 1);
    }
    return true;
}

function spawnMob(x, y, id, map) {
    if (map.getMonsterById(id) != null) {
        return;
    }

    const LifeFactory = Java.type('dev.jaczerob.delfino.maplestory.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');
    var mob = LifeFactory.getMonster(id);
    map.spawnMonsterOnGroundBelow(mob, new Point(x, y));
}