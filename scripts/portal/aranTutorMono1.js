
/*
@author kevintjuh93
*/
function enter(pi) {
    pi.blockPortal();
    if (pi.containsAreaInfo(21002, "mo2=o")) {
        return false;
    }
    pi.playSound("Aran/balloon");
    pi.updateAreaInfo(21002, "mo1=o;mo2=o");
    pi.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/legendBalloon2");
    return true;
}