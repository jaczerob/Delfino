
/*      Author: Xterminator, Moogra
	NPC Name: 		Fourth Eos Rock
	Map(s): 		Ludibrium : Eos Tower 1st Floor (221020000)
	Description: 		Brings you to 41st Floor
*/

function start() {
    if (cm.haveItem(4001020)) {
        cm.sendYesNo("You can use #bEos Rock Scroll#k to activate #bFourth Eos Rock#k. Will you head over to #bThird Eos Rock#k at the 41st floor?");
    } else {
        cm.sendOk("There's a rock that will enable you to teleport to #bThird Eos Rock#k, but it cannot be activated without the scroll.");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode < 1) {
    } else {
        cm.gainItem(4001020, -1);
        cm.warp(221021700, 3);
    }
    cm.dispose();
}