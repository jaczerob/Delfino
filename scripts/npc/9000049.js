/* Fairytail Crackers
	Witch Tower Entrance (980040000)
	Used to warp into the Jump Quest. Currently only used for GM events.
	
	First revision by Twdtwd.
 */

var status;
var stage = 1;

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
            if (cm.getPlayer().isGM()) {
                var event = "CLOSED";
                var stage = cm.getClient().getChannelServer().getStoredVar(9000049);
                if (stage == 1) {
                    event = "EASY";
                }
                if (stage == 2) {
                    event = "MEDIUM";
                }
                if (stage == 3) {
                    event = "HARD";
                }
                cm.sendSimple("Hello GM.\r\nThe event is currently: #r" + event + "#k\r\nWhat would you like to do?\r\n#b#L0#Enter the event#l\r\n#L1#Close the event#l\r\n#L2#Set the event to EASY#l\r\n#L3#Set the event to MEDIUM#l\r\n#L4#Set the event to HARD#l");
            } else {
                var stage = cm.getClient().getChannelServer().getStoredVar(9000049);
                if (stage == 0) {
                    cm.sendOk("It looks like the Tower isn't unlocked yet. Please wait for a GM to unlock it!");
                } else {
                    cm.warp(980040000 + stage * 1000, 0);
                }
                cm.dispose();
            }
        } else if (status == 1 && cm.getPlayer().isGM()) {
            if (selection == 0) {
                var stage = cm.getClient().getChannelServer().getStoredVar(9000049);
                if (stage == 0) {
                    cm.sendOk("It looks like the Tower isn't unlocked yet. Please wait for a GM to unlock it!");
                } else {
                    cm.warp(980040000 + stage * 1000, 0);
                }
                cm.dispose();
                return;
            }
            cm.getClient().getChannelServer().setStoredVar(9000049, selection - 1);
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}