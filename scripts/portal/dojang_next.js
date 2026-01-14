/**
 * @Author Moogra, Ronan
 */
function enter(pi) {
    var currwarp = Date.now();

    if (currwarp - pi.getPlayer().getNpcCooldown() < 3000) {
        return false;
    } // this script can be ran twice when passing the dojo portal... strange.
    pi.getPlayer().setNpcCooldown(currwarp);

    var gate = pi.getPlayer().getMap().getReactorByName("door");
    if (gate != null) {
        if (gate.getState() == 1 || pi.getMap().countMonsters() == 0) {
            if (Math.floor(pi.getPlayer().getMapId() / 100) % 100 < 38) {
                if (((Math.floor((pi.getPlayer().getMap().getId() + 100) / 100)) % 100) % 6 == 0) {
                    if (Math.floor(pi.getPlayer().getMapId() / 10000) == 92503) {
                        var restMapId = pi.getPlayer().getMap().getId() + 100;
                        var mapId = pi.getPlayer().getMap().getId();

                        for (var i = 0; i < 5; i++) {
                            var chrlist = pi.getMap(mapId - 100 * i).getAllPlayers();

                            var pIter = chrlist.iterator();
                            while (pIter.hasNext()) {
                                var chr = pIter.next();

                                for (var j = i; j >= 0; j--) {
                                    chr.message("You received " + chr.addDojoPointsByMap(mapId - 100 * j) + " training points. Your total training points score is now " + chr.getDojoPoints() + ".");
                                }

                                chr.changeMap(restMapId, 0);
                            }
                        }
                    } else {
                        pi.getPlayer().message("You received " + pi.getPlayer().addDojoPointsByMap(pi.getMapId()) + " training points. Your total training points score is now " + pi.getPlayer().getDojoPoints() + ".");
                        pi.playPortalSound();
                        pi.warp(pi.getPlayer().getMap().getId() + 100, 0);
                    }
                } else {
                    pi.getPlayer().message("You received " + pi.getPlayer().addDojoPointsByMap(pi.getMapId()) + " training points. Your total training points score is now " + pi.getPlayer().getDojoPoints() + ".");
                    pi.playPortalSound();
                    pi.warp(pi.getPlayer().getMap().getId() + 100, 0);
                }
            } else {
                pi.playPortalSound();
                pi.warp(925020003, 0);
                pi.getPlayer().gainExp(2000 * pi.getPlayer().getDojoPoints(), true, true, true);
            }
            return true;
        } else {
            pi.getPlayer().message("The door is not open yet.");
            return false;
        }
    } else {
        return false;
    }
}
