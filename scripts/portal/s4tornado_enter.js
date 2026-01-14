/**
 * @author Moogra (BubblesDev)
 * @purpose Warps to the Junior Balrog map for the Rush Skill.
 */
function enter(pi) {
    if (pi.isQuestStarted(6230) || pi.isQuestStarted(6231) || pi.haveItem(4001110)) {
        if (pi.getWarpMap(922020200).countPlayers() == 0) {
            pi.resetMapObjects(922020200);
            pi.playPortalSound();
            pi.warp(922020200, 0);

            return true;
        } else {
            pi.getPlayer().message("Some other player is currently inside.");
            return false;
        }
    }

    return false;
}