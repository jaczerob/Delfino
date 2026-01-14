
/* Portal for the LightBulb Map...

**hontale_c.js
@author Jvlaple
@author Ronan
*/
function enter(pi) {
    if (pi.isEventLeader() == true) {
        var eim = pi.getPlayer().getEventInstance();
        var target;
        var theWay = pi.getMap().getReactorByName("light").getState();
        if (theWay == 1) {
            target = 240050300; //light
        } else if (theWay == 3) {
            target = 240050310; //dark
        } else {
            pi.playerMessage(5, "Hit the Lightbulb to determine your fate!");
            return false;
        }

        pi.playPortalSound();
        eim.warpEventTeam(target);
        return true;
    } else {
        pi.playerMessage(6, "You are not the party leader. Only the party leader may proceed through this portal.");
        return false;
    }
}