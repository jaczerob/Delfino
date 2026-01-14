
/*
@author kevintjuh93
*/
function enter(pi) {
    pi.blockPortal();
    if (pi.containsAreaInfo(21002, "arr0=o")) {
        return false;
    }
    pi.updateAreaInfo(21002, "arr0=o;mo1=o;mo2=o;mo3=o");
    pi.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
    return true;
}  