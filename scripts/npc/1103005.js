/**
 * @NPC: Cygnus
 * @ID: 1103005
 * @Map Id: 913040006
 * @Function: Cygnus Creator
 * @Author Jay <text>
 * @Author David
 */

function start() {
    cm.sendAcceptDecline("Becoming a Knight of Cygnus requires talent, faith, courage, and will power... and it looks like you are more than qualified to become a Knight of Cygnus. What do you think? If you wish to become one right this minute, I'll take you straight to Erev. Would you like to head over to Erev right now?");
}

function action(coded, by, Moogra) {
    if (coded > 0) {
        cm.warp(130000000);
    } else {
        try {
            cm.warp(cm.getPlayer().getSavedLocation("CYGNUSINTRO"));
        } catch (err) {
            cm.warp(100000000);
        }
    }
    cm.dispose();
}
