/**
 * @author Moogra (BubblesDev)
 * @purpose Warps to the Junior Balrog map for the Rush Skill.
 */
function enter(pi) {
    if (pi.isQuestStarted(6110)) {
        pi.playPortalSound();
        pi.warp(910500100, 0);
        return true;
    } else {
        pi.getPlayer().message("A mysterious force won't let you in.");
        return false;
    }
}