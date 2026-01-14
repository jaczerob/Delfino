
function enter(pi) {
    if (pi.hasItem(3992039)) {
        pi.playPortalSound();
        pi.warp(610020000, "CM1_E");
        return false;
    }
    return true;
}