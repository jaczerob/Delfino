function enter(pi) {
    if (pi.isQuestCompleted(7770)) {
        pi.playPortalSound();
        pi.warp(926130000, "out00");
        return true;
    } else {
        pi.playerMessage(5, "This pipe seems too dark to venture inside.");
        return false;
    }
}