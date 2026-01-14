
/*
-- Odin JavaScript --------------------------------------------------------------------------------
	Carson - Magatia (GMS Like)
-- Version Info -----------------------------------------------------------------------------------
    1.2 - Improved by Ronan
    1.1 - Shortened by Moogra
	1.0 - First Version by Maple4U
---------------------------------------------------------------------------------------------------
*/
function start() {
    if (cm.isQuestStarted(3310) && !cm.haveItem(4031709, 1)) {
        cm.warp(926120100, "out00");
    } else {
        cm.sendNext("Alchemy....and Alchemist.....both of them are important. But more importantly, it is the Magatia that tolerate everything. The honor of Magatia should be protected by me.");
    }

    cm.dispose();
}
