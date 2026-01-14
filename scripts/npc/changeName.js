
/* Changes the players name.
	Can only be accessed with the item 2430026.
 */

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0 && mode == 1) {
            if (cm.haveItem(2430026)) {
                cm.sendYesNo("I can change your name for you if you would like?", 1);
            } else {
                cm.dispose();
            }
        } else if (status == 1) {
            cm.sendGetText("Please input your desired name below.");
        } else if (status == 2) {
            var text = cm.getText();

            const Character = Java.type('dev.jaczerob.delfino.maplestory.client.Character');
            var canCreate = Character.canCreateChar(text);
            if (canCreate) {
                cm.getPlayer().setName(text);
                cm.sendOk("Your name has been changed to #b" + text + "#k. You will have to login again for this to take effect.", 1);
                cm.gainItem(2430026, -1);
            } else {
                cm.sendNext("I'm afraid you can't use the name #b" + text + "#k or it is already taken.", 1);
            }
        } else if (status == 3) {
            cm.dispose();
            cm.getClient().disconnect(false, false);
        }
    }
}