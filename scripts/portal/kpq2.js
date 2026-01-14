
/*
Kerning PQ: 3rd stage to 4th stage portal
*/

function enter(pi) {
    var eim = pi.getPlayer().getEventInstance();
    var target = eim.getMapInstance(103000803);
    if (eim.getProperty("3stageclear") != null) {
        pi.playPortalSound();
        pi.getPlayer().changeMap(target, target.getPortal("st00"));
        return true;
    } else {
        pi.getPlayer().dropMessage(5, "The portal is not opened yet.");
        return false;
    }
}