/**
 * @author Moogra (BubblesDev)
 * @purpose Warps to the second drill hall
 */
function enter(pi) {
    var maps = [108000600, 108000601, 108000602];
    if (pi.isQuestStarted(20201) || pi.isQuestStarted(20202) || pi.isQuestStarted(20203) || pi.isQuestStarted(20204) || pi.isQuestStarted(20205)) {
        pi.removeAll(4032096);
        pi.removeAll(4032097);
        pi.removeAll(4032098);
        pi.removeAll(4032099);
        pi.removeAll(4032100);

        var rand = Math.floor(Math.random() * maps.length);
        pi.playPortalSound();
        pi.warp(maps[rand], 0);
        return true;
    } else {
        return false;
    }
}