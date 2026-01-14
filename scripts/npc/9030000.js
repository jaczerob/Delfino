/* Fredrick NPC (9030000)
 * @author kevintjuh93
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        cm.dispose();
        return;
    }
    if (status == 0) {
        if (!cm.hasMerchant() && cm.hasMerchantItems()) {
            cm.showFredrick();
            cm.dispose();
        } else {
            if (cm.hasMerchant()) {
                cm.sendOk("You have a Merchant open.");
                cm.dispose();
            } else {
                cm.sendOk("You don't have any items or mesos to be retrieved.");
                cm.dispose();
            }
        }
    }
}