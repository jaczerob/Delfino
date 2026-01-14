
/*
    Zakum PQ portal
*/

function enter(pi) {
    if (!pi.getEventInstance().isEventCleared()) {
        pi.getPlayer().dropMessage(5, "Your team has not yet completed the trials. Fetch the Fire Ore and give it to Aura first.");
        return false;
    }

    if (pi.getEventInstance().gridCheck(pi.getPlayer()) == -1) {
        pi.getPlayer().dropMessage(5, "Your have yet to claim your prize. Talk to Aura.");
        return false;
    }

    pi.playPortalSound();
    pi.warp(211042300);
    return true;
}