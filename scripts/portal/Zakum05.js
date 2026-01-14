
/*
    Zakum Entrance
*/

function enter(pi) {
    if (!(pi.isQuestStarted(100200) || pi.isQuestCompleted(100200))) {
        pi.getPlayer().dropMessage(5, "You need approval from the masters to battle. You may not attempt the boss right now.");
        return false;
    }

    if (!pi.isQuestCompleted(100201)) {
        pi.getPlayer().dropMessage(5, "You haven't completed all the trials yet. You may not attempt the boss right now.");
        return false;
    }

    if (!pi.haveItem(4001017)) {    // thanks Conrad for pointing out missing checks for token item and unused reactor
        pi.getPlayer().dropMessage(5, "You do not have the Eye of Fire. You may not face the boss.");
        return false;
    }

    var react = pi.getMap().getReactorById(2118002);
    if (react != null && react.getState() > 0) {
        pi.getPlayer().dropMessage(5, "The entrance is currently blocked.");
        return false;
    }

    pi.playPortalSound();
    pi.warp(211042400, "west00");
    return true;
}