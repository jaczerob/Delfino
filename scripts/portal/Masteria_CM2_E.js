
function enter(pi) {
    if (pi.hasItem(3992039)) {
        pi.playPortalSound();
        pi.warp(610020001, "CM2_F");
        return false;
    }
    return true;
}