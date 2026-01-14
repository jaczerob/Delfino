/*
	Author : 		Ronan
	NPC Name: 	        Dr. Kim
	Map(s): 		Omega Sector
	Description: 		Quest - Wave Translator
	Quest ID: 		3454
*/

var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            const InventoryType = Java.type('dev.jaczerob.delfino.maplestory.client.inventory.InventoryType');
            if (qm.getPlayer().getInventory(InventoryType.ETC).getNumFreeSlot() < 1) {
                qm.sendOk("Make room on your ETC inventory first.");
                qm.dispose();
                return;
            }

            qm.gainItem(4000125, -1);
            qm.gainItem(4031926, -10);
            qm.gainItem(4000119, -30);
            qm.gainItem(4000118, -30);

            rnd = Math.random();
            if (rnd < 1.0) {
                qm.gainItem(4031928, 1);
            } else {
                qm.gainItem(4031927, 1);
            }

            qm.sendOk("Now, go meet Alien Gray and use this undercover to read through their plans. If this fails, we will need to gather some materials once again.");
            qm.forceCompleteQuest();
        } else if (status == 1) {
            qm.dispose();
        }
    }
}
