
function start() {
    if (cm.getQuestProgressInt(2180, 1) == 2) {
        cm.sendNext("You have taken milk from this cow recently, check another cow.");
        cm.dispose();
        return;
    }

    if (cm.canHold(4031848) && cm.haveItem(4031847)) {
        cm.sendNext("Now filling up the bottle with milk. The bottle is now 1/3 full of milk.");
        cm.gainItem(4031847, -1);
        cm.gainItem(4031848, 1);

        cm.setQuestProgress(2180, 1, 2);
    } else if (cm.canHold(4031849) && cm.haveItem(4031848)) {
        cm.sendNext("Now filling up the bottle with milk. The bottle is now 2/3 full of milk.");
        cm.gainItem(4031848, -1);
        cm.gainItem(4031849, 1);

        cm.setQuestProgress(2180, 1, 2);
    } else if (cm.canHold(4031850) && cm.haveItem(4031849)) {
        cm.sendNext("Now filling up the bottle with milk. The bottle is now completely full of milk.");
        cm.gainItem(4031849, -1);
        cm.gainItem(4031850, 1);

        cm.setQuestProgress(2180, 1, 2);
    } else {
        cm.sendNext("Your inventory is full, and there's no room for a milk bottle.");
    }
    cm.dispose();
}