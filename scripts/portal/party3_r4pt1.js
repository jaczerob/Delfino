/**
 *@author Jvlaple
 *party3_r4pt
 */

function enter(pi) {
    pi.playPortalSound();
    pi.warp(920010600, Math.random() * 3 > 1 ? 1 : 2);
    return true;
}