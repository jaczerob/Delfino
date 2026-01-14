/* 9000021 - Gaga
    BossRushPQ recruiter
    @author Ronan
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            cm.sendNext("Hey, traveler! I am #p9000021#, and my job is to recruit travelers like you, who eagers for new challenges daily. Right now, my team is holding contests that thoroughly tests the mental and physical capabilities of adventurers like you.");
        } else if (status == 1) {
            cm.sendNext("These contests involve #bsequential boss fights#k, with some resting spots between some sections. These will require some strategy time and enough supplies at hand, as they are not common fights.");
        } else if (status == 2) {
            cm.sendAcceptDecline("If you feel you are powerful enough, you can join others like you at where we are hosting the contests of power. ... So, what is your decision? Will you come to where the contests are being held right now?");
        } else if (status == 3) {
            cm.sendOk("Very well. Remember, there you can assemble a team or take on the fightings on your own, it's up to you. Good luck!");
        } else if (status == 4) {
            cm.getPlayer().saveLocation("BOSSPQ");
            cm.warp(970030000, "out00");
            cm.dispose();
        }
    }
}