function start() {
    var eim = cm.getPlayer().getEventInstance();
    if (eim != null) {
        if (cm.isEventLeader()) {
            if (cm.haveItem(4001024)) {
                cm.removeAll(4001024);
                var prev = eim.setProperty("bossclear", "true");
                if (prev == null) {
                    var start = parseInt(eim.getProperty("entryTimestamp"));
                    var diff = Date.now() - start;

                    var points = 1000 - Math.floor(diff / (100 * 60));
                    if (points < 100) {
                        points = 100;
                    }

                    cm.getGuild().gainGP(points);
                }

                eim.clearPQ();
            } else {
                cm.sendOk("This is your final challenge. Defeat the evil lurking within the Rubian and return it to me. That is all.");
            }
        } else {
            cm.sendOk("This is your final challenge. Defeat the evil lurking within the Rubian and let your instance leader return it to me. That is all.");
        }
    } else {
        cm.warp(990001100);
    }

    cm.dispose();
}