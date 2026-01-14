
/* @Author Lerk
 * 
 * 9208001.js: Guild Quest - Gatekeeper Puzzle Reactor
 * 
*/


function padWithZeroes(n, width) {
    while (n.length < width) {
        n = '0' + n;
    }
    return n;
}

function act() {
    var eim = rm.getPlayer().getEventInstance();
    if (eim != null) {
        var status = eim.getProperty("stage1status");
        if (status != null && status !== "waiting") {
            var stage = parseInt(eim.getProperty("stage1phase"));
            //rm.mapMessage(6,"Stage " + stage);
            if (status === "display") {
                if (!rm.getReactor().isRecentHitFromAttack()) {
                    var prevCombo = eim.getProperty("stage1combo");

                    var n = "" + (rm.getReactor().getObjectId() % 1000);
                    prevCombo += padWithZeroes(n, 3);

                    eim.setProperty("stage1combo", prevCombo);
                    if (prevCombo.length == (3 * (stage + 3))) { //end of displaying
                        eim.setProperty("stage1status", "active");
                        rm.mapMessage(5, "The combo has been displayed; Proceed with caution.");
                        eim.setProperty("stage1guess", "");
                    }
                }
            } else { //active
                var prevGuess = "" + eim.getProperty("stage1guess");
                if (prevGuess.length != (3 * (stage + 3))) {
                    var n = "" + (rm.getReactor().getObjectId() % 1000);
                    prevGuess += padWithZeroes(n, 3);

                    eim.setProperty("stage1guess", prevGuess);
                }
                //rm.mapMessage(6,"Current Guess: " + prevGuess);
            }
        }
    }
}