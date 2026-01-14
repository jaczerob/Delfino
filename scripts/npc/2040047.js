/*
@	Author : Raz
@       Author : Ronan
@
@	NPC = Sgt.Anderson
@	Map =  Abandoned Tower <Stage 1>
@	NPC MapId = 922010100
@	NPC Exit-MapId = 221024500
@
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
    var mapId = cm.getPlayer().getMapId();
    if (mapId == 922010000) {
        if (status == 0) {
            cm.sendNext("To return back to the recruitment map, follow this way.");
        } else {
            cm.warp(221024500);
            cm.dispose();
        }
    } else {
        if (status == 0) {
            var outText = "Once you leave the map, you'll have to restart the whole quest if you want to try it again.  Do you still want to leave this map?";
            cm.sendYesNo(outText);
        } else if (mode == 1) {
            cm.warp(922010000); // Warp player
            cm.dispose();
        }
    }
}