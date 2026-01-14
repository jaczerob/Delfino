/**
 * @Author Ronan
 */
function enter(pi) {
    var name = pi.getPortal().getName().substring(2, 4);
    var gate = pi.getPlayer().getMap().getReactorByName("gate" + name);
    if (gate != null && gate.getState() == 4) {
        pi.playPortalSound();
        pi.warp(670010600, "gt" + name + "PIB");
        return true;
    } else {
        pi.message("The gate is not opened yet.");
        return false;
    }
}