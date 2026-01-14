/**
 * @author Twdtwd (DiscoveryMS), Ronan
 * @purpose Warps to the Cold Cave for Resurection.
 */
function enter(pi) {
    if (pi.haveItem(4001108)) {
        if (pi.getWarpMap(923000100).countPlayers() == 0) {
            pi.resetMapObjects(923000100);
            pi.playPortalSound();
            pi.warp(923000100, 0);

            return true;
        } else {
            pi.getPlayer().message("Some other player is currently inside.");
            return false;
        }
    }

    pi.getPlayer().message("A mysterious force won't let you in.");
    return false;
}