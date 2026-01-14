
/*
@Author Jvlaple
*/

function enter(pi) {
    if (pi.getMap().countPlayers() == 1) {
        pi.getPlayer().dropMessage(6, "As the last player on this map, you are compelled to wait for the incoming keys.");
        return false;
    } else {
        if (pi.haveItem(4001087)) {
            pi.getPlayer().dropMessage(6, "You cannot pass to the next map holding the 1st Crystal Key in your inventory.");
            return false;
        }
        pi.playPortalSound();
        pi.warp(240050101, 0);
        return true;
    }
}