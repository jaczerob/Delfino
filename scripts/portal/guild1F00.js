
/*
Return from Sharen III's Grave - Guild Quest

@Author Ronan
*/

function enter(pi) {
    var backPortals = [6, 8, 9, 11];
    var idx = pi.getEventInstance().gridCheck(pi.getPlayer());

    pi.playPortalSound();
    pi.warp(990000600, backPortals[idx]);
    return true;
}