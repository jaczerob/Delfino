/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Eliza1 Spawner
 -- Edited by --------------------------------------------------------------------------------------
 ThreeStep - based on xQuasar's King Clang spawner

 **/

var setupTask;

function init() {
    scheduleNew();
}

function scheduleNew() {
    setupTask = em.schedule("start", 0);    //spawns upon server start. Each 3 hours an server event checks if boss exists, if not spawns it instantly.
}

function cancelSchedule() {
    if (setupTask != null) {
        setupTask.cancel(true);
    }
}

function start() {
    const LifeFactory = Java.type('dev.jaczerob.delfino.maplestory.server.life.LifeFactory');
    var eliza = LifeFactory.getMonster(8220000);
    var stairwayToTheSky2 = em.getChannelServer().getMapFactory().getMap(200010300);

    if (stairwayToTheSky2.getMonsterById(8220000) != null) {
        em.schedule("start", 3 * 60 * 60 * 1000);
        return;
    }

    const PacketCreator = Java.type('dev.jaczerob.delfino.maplestory.tools.PacketCreator');
    const Point = Java.type('java.awt.Point');
    const spawnpoint = new Point(208, 83);
    stairwayToTheSky2.spawnMonsterOnGroundBelow(eliza, spawnpoint);
    stairwayToTheSky2.broadcastMessage(PacketCreator.serverNotice(6, "Eliza has appeared with a black whirlwind."));
    em.schedule("start", 3 * 60 * 60 * 1000);
}

// ---------- FILLER FUNCTIONS ----------

function dispose() {}

function setup(eim, leaderid) {}

function monsterValue(eim, mobid) {return 0;}

function disbandParty(eim, player) {}

function playerDisconnected(eim, player) {}

function playerEntry(eim, player) {}

function monsterKilled(mob, eim) {}

function scheduledTimeout(eim) {}

function afterSetup(eim) {}

function changedLeader(eim, leader) {}

function playerExit(eim, player) {}

function leftParty(eim, player) {}

function clearPQ(eim) {}

function allMonstersDead(eim) {}

function playerUnregistered(eim, player) {}

