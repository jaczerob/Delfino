function enter(pi) {
    try {
        var toMap = pi.getPlayer().getSavedLocation("FREE_MARKET");
        pi.playPortalSound();
        pi.warp(toMap, pi.getMarketPortalId(toMap));
    } catch (err) {
        pi.playPortalSound();
        pi.warp(100000000, 0);
    }
    return true;
}