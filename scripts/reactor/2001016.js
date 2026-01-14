
/*@author Ronan
 *Nependeath Pot - Spawns Papa Pixie
 */

function act() {
    rm.getMap().killAllMonsters();
    rm.getMap().allowSummonState(false);
    rm.spawnMonster(9300039, 260, 490);
    rm.mapMessage(5, "As the air on the tower outskirts starts to become more dense, Papa Pixie appears.");
}