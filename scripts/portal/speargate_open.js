
/*
    Stage 2: Exit Door - Guild Quest
    @Author Lerk
*/

function enter(pi) {
    if (pi.getPlayer().getMap().getReactorByName("speargate").getState() == 4) {
        pi.playPortalSound();
        pi.warp(990000401, 0);
        return true;
    } else {
        pi.getPlayer().dropMessage(5, "This way forward is not open yet.");
        return false;
    }
}