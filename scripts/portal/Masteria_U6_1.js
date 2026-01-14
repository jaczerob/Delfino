
function enter(pi) {
    if (pi.hasItem(3992040)) {
        pi.playPortalSound();
        pi.warp(610010002, "sU3_1");
        return false;
    }
    return true;
}