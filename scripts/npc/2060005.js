
/**
 @Author Ronan

 2060005 - Kenta
 Enter 3rd job mount event
 **/

function start() {
    if (cm.isQuestCompleted(6002)) {
        cm.sendOk("Thanks for saving the pork.");
    } else if (cm.isQuestStarted(6002)) {
        if (cm.haveItem(4031507, 5) && cm.haveItem(4031508, 5)) {
            cm.sendOk("Thanks for saving the pork.");
        } else {
            var em = cm.getEventManager("3rdJob_mount");
            if (em == null) {
                cm.sendOk("Sorry, but 3rd job advancement (mount) is closed.");
            } else {
                if (em.startInstance(cm.getPlayer())) {
                    cm.removeAll(4031507);
                    cm.removeAll(4031508);
                } else {
                    cm.sendOk("There is currently someone in this map, come back later.");
                }
            }
        }
    } else {
        cm.sendOk("Only few adventurers, from a selected public, are eligible to protect the Watch Hog.");
    }

    cm.dispose();
}