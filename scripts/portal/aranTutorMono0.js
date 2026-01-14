
/*
@author kevintjuh93
*/
function enter(pi) {
    pi.blockPortal();
    if (pi.containsAreaInfo(21002, "mo1=o")) {
        return false;
    }
    pi.updateAreaInfo(21002, "mo1=o");
    pi.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/legendBalloon1");
    return true;
}  