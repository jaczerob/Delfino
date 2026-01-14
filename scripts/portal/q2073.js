/**
 * @author Twdtwd
 * @purpose Warps to Utah's Pig Farm for the quest Camila's Gem.
 */
function enter(pi) {
    if (pi.isQuestStarted(2073)) {
        pi.playPortalSound();
        pi.warp(900000000, 0);
        return true;
    } else {
        pi.message("Private property. This place can only be entered when running an errand from Camila.");
        return false;
    }
}