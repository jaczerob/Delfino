
/* Door that warps you inside the CWKPQ start map.
 * 
 * @Author Ronan
 */

function start() {
    if (cm.haveItem(3992041, 1)) {
        cm.warp(610030020, "out00");
    } else {
        cm.playerMessage(5, "The giant gate of iron will not budge no matter what, however there is a visible key-shaped socket.");
    }

    cm.dispose();
}