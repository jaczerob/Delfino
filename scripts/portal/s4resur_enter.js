/**
 * @author Moogra (BubblesDev)
 * @purpose Warps to the Junior Balrog map for the Rush Skill.
 */
function enter(pi) {
    if (pi.isQuestStarted(6134)) {
        pi.playPortalSound();
        pi.warp(922020000, 0);
        return true;
    }

    pi.getPlayer().message("A mysterious force won't let you in.");
    return false;
}