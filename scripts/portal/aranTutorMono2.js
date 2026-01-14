
/*
@author kevintjuh93
*/
function enter(pi) {
    pi.blockPortal();
    if (pi.containsAreaInfo(21002, "mo3=o")) {
        return false;
    }
    pi.updateAreaInfo(21002, "mo1=o;mo2=o;mo3=o");
    pi.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/legendBalloon3");
    return true;
}