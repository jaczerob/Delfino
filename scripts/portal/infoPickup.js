
/*
@author kevintjuh93
*/
function enter(pi) {
    if (pi.isQuestStarted(1035)) {
        pi.showInfo("UI/tutorial.img/21");
    }

    pi.blockPortal();
    return true;
}  