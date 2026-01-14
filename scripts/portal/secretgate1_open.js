
/*
    Stage 4: Mark of Evil Door - Guild Quest
    @Author Lerk
*/

function enter(pi) {
    if (pi.getPlayer().getMap().getReactorByName("secretgate1").getState() == 1) {
        pi.playPortalSound();
        pi.warp(990000611, 1);
        return true;
    } else {
        pi.playerMessage(5, "This door is closed.");
        return false;
    }
}
