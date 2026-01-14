function enter(pi) {
    pi.playPortalSound();
    pi.warp(300000100, "out00");
    pi.playerMessage(5, "Now passing the Time Gate.");
    return true;
}
