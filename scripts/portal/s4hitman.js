/**
 * @author Moogra (BubblesDev)
 * @purpose Warps to the Relic map for the Assassinate Skill.
 */
function enter(pi) {
    if (pi.isQuestStarted(6201)) {
        if (pi.getWarpMap(910200000).countPlayers() == 0) {
            pi.resetMapObjects(910200000);
            pi.playPortalSound();
            pi.warp(910200000, 0);

            return true;
        } else {
            pi.getPlayer().message("Some other player is currently inside.");
            return false;
        }
    }

    pi.getPlayer().message("A mysterious force won't let you in.");
    return false;
}