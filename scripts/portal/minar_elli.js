function enter(pi) {
    if (!pi.haveItem(4031346)) {
        pi.getPlayer().dropMessage(6, "You need a magic seed to use this portal.");
        return false;
    }
    if (pi.getPlayer().getMapId() == 240010100) {
        pi.gainItem(4031346, -1);
        pi.playPortalSound();
        pi.warp(101010000, "minar00");
        return true;
    } else if (pi.getPlayer().getMapId() == 101010000) {
        pi.gainItem(4031346, -1);
        pi.playPortalSound();
        pi.warp(240010100, "elli00");
        return true;
    }
    return true;
}