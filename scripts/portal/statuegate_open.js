
/*
    Stage 1: Gatekeeper door - Guild Quest
    @Author Lerk
*/
function enter(pi) {
    if (pi.getPlayer().getMap().getReactorByName("statuegate").getState() == 1) {
        pi.playPortalSound();
        pi.warp(990000301, 0);
        return true;
    } else {
        pi.getPlayer().dropMessage(5, "The gate is closed.");
        return false;
    }
}