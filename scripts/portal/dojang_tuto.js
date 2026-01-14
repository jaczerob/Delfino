
/*
	Author: Traitor
	Map(s):	So Gong's maps
	Desc:   doesn't do anything man. ANYTHING.
*/

function enter(pi) {
    if (pi.getPlayer().getMap().getMonsterById(9300216) != null) {
        pi.getPlayer().enteredScript("dojang_Msg", pi.getPlayer().getMap().getId());
        pi.getPlayer().setFinishedDojoTutorial();
        pi.getClient().getChannelServer().resetDojo(pi.getPlayer().getMap().getId());
        pi.getClient().getChannelServer().dismissDojoSchedule(pi.getPlayer().getMap().getId(), pi.getParty());
        pi.playPortalSound();
        pi.warp(925020001, 0);
        return true;
    } else {
        pi.getPlayer().message("So Gong: Haha! You're going to run away like a coward? I won't let you get away that easily!");
        return false;
    }
}