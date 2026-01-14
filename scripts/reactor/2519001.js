
/*2519001.js - Reactor used at the door on stage 4.
 *@author Ronan
 */

function act() {
    var denyWidth = 320, denyHeight = 150;
    var denyPos = rm.getReactor().getPosition();
    const Rectangle = Java.type('dev.jaczerob.delfino.maplestory.java.awt.Rectangle');
    var denyArea = new Rectangle(denyPos.getX() - denyWidth / 2, denyPos.getY() - denyHeight / 2, denyWidth, denyHeight);

    rm.getReactor().getMap().setAllowSpawnPointInBox(false, denyArea);

    var map = rm.getReactor().getMap();
    if (map.getReactorByName("sMob1").getState() >= 1 && map.getReactorByName("sMob3").getState() >= 1 && map.getReactorByName("sMob4").getState() >= 1 && map.countMonsters() == 0) {
        rm.getEventInstance().showClearEffect(map.getId());
    }
}
