
/*
@author kevintjuh93
*/
function enter(pi) {
    if (pi.isQuestStarted(1031)) {
        pi.showInfo("UI/tutorial.img/25");
    }

    pi.blockPortal();
    return true;
}  