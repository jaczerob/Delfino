
/*
Warp to Sharen III's Grave - Guild Quest
Give guild points if holding appropriate item and not gained already
Save location to return.

@Author Lerk
*/

function enter(pi) {
    pi.getEventInstance().gridInsert(pi.getPlayer(), 3);
    pi.playPortalSound();
    pi.warp(990000700, "st00");
    return true;
}
