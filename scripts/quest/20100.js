/*
	Author : Traitor
	NPC Name: 		Neinheart
	Map(s): 		Ereve
	Description: 		Quest - Time to Choose
	Quest ID : 		20100
*/

var status = -1;

function start(mode, type, selection) {
    if (mode > 0) {
        status++;
    } else {
        qm.dispose();
        return;
    }
    if (status == 0) {
        qm.sendAcceptDecline("Ahhh, you're back. I can see that you're at level 10 now. It looks like you're flashing a glimmer of hope towards becoming a Knight. The basic training has now ended, and it's time for you to make the decision.");
    } else if (status == 1) {
        qm.forceStartQuest();
        qm.forceCompleteQuest();

        qm.sendOk("Now look to the left. The leaders of the Knights will be waiting for you. There will be 5 paths for you to choose from. All you need to do is choose one of them. All 5 of them will lead you to the path of a Knight, so... I suggest you pay attention to what each path offers, and select the one you'd most like to take.");
    } else if (status == 2) {
        qm.dispose();
    }
}