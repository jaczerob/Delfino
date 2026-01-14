/*

/*
Kerning PQ: 4th stage to last stage portal
*/

function enter(pi) {
    var eim = pi.getPlayer().getEventInstance();
    var target = eim.getMapInstance(103000804);
    if (eim.getProperty("4stageclear") != null) {
        pi.playPortalSound();
        pi.getPlayer().changeMap(target, target.getPortal("st00"));
        return true;
    } else {
        pi.getPlayer().dropMessage(5, "The portal is not opened yet.");
        return false;
    }
}