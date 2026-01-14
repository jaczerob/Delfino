
/*
 * @author:  Moogra
 * @portal:  aMatchMove2
 * @purpose: warps user out from Ariant PQ
 */

function enter(pi) {
    pi.playPortalSound();
    pi.warp(pi.getPlayer().getSavedLocation("MIRROR"));
    return true;
}