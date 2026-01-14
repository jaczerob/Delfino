/* Olson the Toy Soldier
	2040002

map: 922000010
quest: 3230
escape: 2040028
*/

var status = 0;
var em;

function start() {
    if (cm.isQuestStarted(3230)) {
        em = cm.getEventManager("DollHouse");

        if (em.getProperty("noEntry") == "false") {
            cm.sendNext("The pendulum is hidden inside a dollhouse that looks different than the others.");
        } else {
            cm.sendOk("Someone else is already searching the area. Please wait until the area is cleared.");
            cm.dispose();
        }
    } else {
        cm.sendOk("We are not allowed to let the general public wander past this point.");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        status++;
        if (status == 1) {
            cm.sendYesNo("Are you ready to enter the dollhouse map?");
        } else if (status == 2) {
            var em = cm.getEventManager("DollHouse");
            if (!em.startInstance(cm.getPlayer())) {
                cm.sendOk("Hmm... The DollHouse is being challenged already, it seems. Try again later.");
            }

            cm.dispose();
        }
    }
}