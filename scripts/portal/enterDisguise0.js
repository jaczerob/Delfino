
/*
	Map(s): 		Empress' Road : Crossroads of Ereve
	Description: 		Takes you to Training Forest I
*/

function enter(pi) {
    if (pi.isQuestStarted(20301) || pi.isQuestStarted(20302) || pi.isQuestStarted(20303) || pi.isQuestStarted(20304) || pi.isQuestStarted(20305)) {
        if (pi.hasItem(4032179)) {
            pi.playPortalSound();
            pi.warp(130010000, "east00");
        } else {
            pi.getPlayer().dropMessage(5, "Due to the lock down you can not enter without a permit.");
            return false;
        }
    } else {
        pi.playPortalSound();
        pi.warp(130010000, "east00");
    }
    return true;
}