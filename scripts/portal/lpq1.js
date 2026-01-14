
/*
LudiPQ - 1 - 2 Portal
@author Jvlaple
*/

function enter(pi) {
    var nextMap = 922010300;
    var eim = pi.getPlayer().getEventInstance();
    var target = eim.getMapInstance(nextMap);
    var targetPortal = target.getPortal("st00");
    var avail = eim.getProperty("2stageclear");
    if (avail == null) {
        pi.getPlayer().dropMessage(5, "Some seal is blocking this door.");
        return false;
    } else {
        pi.playPortalSound();
        pi.getPlayer().changeMap(target, targetPortal);
        return true;
    }
}