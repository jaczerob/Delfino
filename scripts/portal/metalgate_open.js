
/*
Stage 2: Door guard by Dark Muscle Golems - Guild Quest

@Author Lerk
*/

function enter(pi) {
    if (pi.getPlayer().getMap().getReactorByName("metalgate").getState() == 1) {
        pi.playPortalSound();
        pi.warp(990000431, 0);
        return true;
    }
    pi.playerMessage(5, "This way forward is not open yet.");
    return false;
}
