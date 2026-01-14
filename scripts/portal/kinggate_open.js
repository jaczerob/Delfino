
/*
Stage 5: Door before Ergoth - Guild Quest

@Author Lerk
*/

function enter(pi) {
    if (pi.getPlayer().getMap().getReactorByName("kinggate").getState() == 1) {
        pi.playPortalSound();
        pi.warp(990000900, 1);
        if (pi.getPlayer().getEventInstance().getProperty("boss") != null && pi.getPlayer().getEventInstance().getProperty("boss") === "true") {
            pi.changeMusic("Bgm10/Eregos");
        }
        return true;
    } else {
        pi.playerMessage(5, "This door is closed.");
        return false;
    }
}
