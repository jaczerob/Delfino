
/* @Author Lerk
 * @Author Ronan
 * 
 * Guild Quest Waiting Room - Entry Portal (map 990000000)
 */

function enter(pi) {
    var entryTime = pi.getPlayer().getEventInstance().getProperty("entryTimestamp");
    var timeNow = Date.now();

    var timeLeft = Math.ceil((entryTime - timeNow) / 1000);

    if (timeLeft <= 0) {
        pi.playPortalSound();
        pi.warp(990000100, 0);
        return true;
    } else { //cannot proceed while allies can still enter
        pi.playerMessage(5, "The portal will open in about " + timeLeft + " seconds.");
        return false;
    }
}