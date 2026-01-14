/**
 Assistant Travis
 -- By ---------------------------------------------------------------------------------------------
 Angel (get31720)
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Angel
 2.0 - Second Version by happydud3 & XotiCraze
 3.0 - Third Version by RonanLana (HeavenMS)
 ---------------------------------------------------------------------------------------------------
 **/

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 || mode == 0) {
        cm.dispose();
        return;
    } else if (mode == 1) {
        status++;
    } else {
        status--;
    }

    var eim = cm.getEventInstance();
    if (eim == null) {
        cm.warp(680000000, 0);
        cm.dispose();
        return;
    }

    var isMarrying = (cm.getPlayer().getId() == eim.getIntProperty("groomId") || cm.getPlayer().getId() == eim.getIntProperty("brideId"));

    switch (status) {
        case 0:
            if (cm.getMapId() == 680000300) {
                cm.sendYesNo("Are you sure you want to #rquit the stage#k and head back to #bAmoria#k? You will be #rskipping the bonus stages#k that way.");
            } else {
                var hasEngagement = false;
                for (var x = 4031357; x <= 4031364; x++) {
                    if (cm.haveItem(x, 1)) {
                        hasEngagement = true;
                        break;
                    }
                }

                if (cm.haveItem(4000313) && isMarrying) {
                    if (eim.getIntProperty("weddingStage") == 3) {
                        cm.sendOk("You guys totally rocked the stage!!! Go go, talk to #b#p9201007##k to start the afterparty.");
                        cm.dispose();
                    } else if (hasEngagement) {
                        if (!cm.createMarriageWishlist()) {
                            cm.sendOk("You have already sent your wishlist...");
                        }
                        cm.dispose();
                    } else {
                        cm.sendOk("Oh, hey, where are the credentials for the this so-lauded party? Oh man, we can't continue at this rate now... Sorry, the party is over.");
                    }
                } else {
                    if (eim.getIntProperty("weddingStage") == 3) {
                        if (!isMarrying) {
                            cm.sendYesNo("You guys didn't miss them right? Our superstars #rworked so good together#k, and soon #bthey will start the afterparty#k. Are you really going to #rdrop out of the show#k and return to #bAmoria#k?");
                        } else {
                            cm.sendOk("You guys totally rocked the stage!!! Go go, talk to #b#p9201007##k to start the afterparty.");
                            cm.dispose();
                        }
                    } else {
                        cm.sendYesNo("Are you sure you want to #rquit the stage#k and head to #bAmoria#k? You will be #rskipping the bonus stages#k, fam.");
                    }
                }
            }


            break;

        case 1:
            cm.warp(680000000, 0);
            cm.dispose();
            break;
    }
}
