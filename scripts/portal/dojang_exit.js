
/*
 * @author:  kevintjuh93, moogra
 * @portal:  dojang_exit
 * @purpose: warps user out
 */

function enter(pi) {
    var map = pi.getPlayer().getSavedLocation("MIRROR");
    if (map == -1) {
        map = 100000000;
    }

    pi.playPortalSound();
    pi.warp(map);
    return true;
}