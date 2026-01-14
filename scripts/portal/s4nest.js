/**
 * @author Moogra (BubblesDev)
 * @purpose Warps to the Junior Balrog map for the Rush Skill.
 */
function enter(pi) {
    if (pi.isQuestStarted(6241) || pi.isQuestStarted(6243)) {
        if (pi.getWarpMap(924000100).countPlayers() == 0) {
            pi.resetMapObjects(924000100);
            pi.playPortalSound();
            pi.warp(924000100, 0);

            return true;
        } else {
            pi.getPlayer().message("Some other player is currently inside.");
            return false;
        }
    }

    pi.getPlayer().message("A mysterious force won't let you in.");
    return false;
}