/* Author: PurpleMadness
 * In search for the lost memory - bowman
*/

var status = -1;

function start(mode, type, selection) {
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
            qm.startQuest();
            qm.setQuestProgress(3507, 7081, 1);
            qm.completeQuest();
            qm.sendOk("You have regained your memories, talk to #b#p2140001##k to get the pass.");
        } else if (status == 1) {
            qm.dispose();
        }
    }
}