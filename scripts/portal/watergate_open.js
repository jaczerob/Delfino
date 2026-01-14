
/*
Stage 3: Exit Door - Guild Quest
@Author Lerk
*/

function enter(pi) {
    if (pi.getPlayer().getMap().getReactorByName("watergate").getState() == 1) {
        pi.playPortalSound();
        pi.warp(990000600, 1);
        return true;
    } else {
        pi.getPlayer().dropMessage(5, "This way forward is not open yet.");
    }
    return false;
}
