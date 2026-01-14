
/*
-- Odin JavaScript --------------------------------------------------------------------------------
	Parwen - Magatia (GMS Like)
-- Version Info -----------------------------------------------------------------------------------
    1.2 - Improved by Ronan
    1.1 - Shortened by Moogra
	1.0 - First Version by Maple4U
---------------------------------------------------------------------------------------------------
*/

function start() {
    if (cm.isQuestStarted(3320) || cm.isQuestCompleted(3320)) {
        cm.warp(926120200, 1);
    } else {
        cm.sendOk("uuuuhuk...Why only Ghost are around here?...");
    }

    cm.dispose();
}
