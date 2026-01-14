
/* Amon
 * 
 * @Author Stereo
 * Adobis's Mission I : Breath of Lava <Level 1> (280020000)
 * Adobis's Mission I : Breath of Lava <Level 2> (280020001)
 * Last Mission : Zakum's Altar (280030000)
 * Zakum Quest NPC 
 * Helps players leave the map
 */

function start() {
    if (cm.getMapId() == 280030000) {
        if (!cm.getEventInstance().isEventCleared()) {
            cm.sendYesNo("If you leave now, you'll have to start over. Are you sure you want to leave?");
        } else {
            cm.sendYesNo("You guys finally overthrew Zakum, what a superb feat! Congratulations! Are you sure you want to leave now?");
        }
    } else {
        cm.sendYesNo("If you leave now, you'll have to start over. Are you sure you want to leave?");
    }
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        cm.warp(211042300);
        cm.dispose();
    }
}