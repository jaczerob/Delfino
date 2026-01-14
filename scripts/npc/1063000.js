/* John JQ Flower pile #1
*/

var repeatablePrizes = [[4010000, 3], [4010001, 3], [4010002, 3], [4010003, 3], [4010004, 3], [4010005, 3]];

function start() {
    if (cm.isQuestStarted(2052) && !cm.haveItem(4031025, 10)) {
        if (!cm.canHold(4031025, 10)) {
            cm.sendNext("Check for a available slot on your ETC inventory.");
            cm.dispose();
            return;
        }

        cm.gainItem(4031025, 10);
    } else {
        const InventoryType = Java.type('dev.jaczerob.delfino.maplestory.client.inventory.InventoryType');
        if (cm.getPlayer().getInventory(InventoryType.ETC).getNumFreeSlot() < 1) {
            cm.sendNext("Check for a available slot on your ETC inventory.");
            cm.dispose();
            return;
        }

        var itemPrize = repeatablePrizes[Math.floor((Math.random() * repeatablePrizes.length))];
        cm.gainItem(itemPrize[0], itemPrize[1]);
    }

    cm.warp(105040300, 0);
    cm.dispose();
}