
/* 2001002 - Metal Bucket Snowman
    @author Ronan
 */

var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            cm.sendYesNo("We have a beautiful christmas tree.\r\nDo you want to see/decorate it?");
        } else if (status == 1) {
            cm.warp(209000002);
            cm.dispose();
        }
    }
} 