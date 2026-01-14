function enter(pi) {
    if (pi.isQuestCompleted(3706)) {
        pi.playPortalSound();
        pi.warp(240040612, "out00");
        return true;
    } else if (pi.isQuestStarted(100203) || pi.getPlayer().haveItem(4001094)) {
        var em = pi.getEventManager("NineSpirit");
        if (!em.startInstance(pi.getPlayer())) {
            pi.message("There is currently someone in this map, come back later.");
            return false;
        } else {
            pi.playPortalSound();
            return true;
        }
    } else {
        pi.message("A strange force is blocking you from entering.");
        return false;
    }
}