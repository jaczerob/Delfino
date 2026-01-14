/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Dyle Spawner
 -- Edited by --------------------------------------------------------------------------------------
 ThreeStep - based on xQuasar's King Clang spawner

 **/

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
    var dangeroudCroko1 = em.getChannelServer().getMapFactory().getMap(107000300);
    if (dangeroudCroko1.getMonsterById(6220000) != null) {
        setupTask = em.schedule("start", 3 * 60 * 60 * 1000);
        return;
    }

    const LifeFactory = Java.type('dev.jaczerob.delfino.maplestory.server.life.LifeFactory');
    const ChannelPacketCreator = Java.type('dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator');
    const Point = Java.type('java.awt.Point');
    const spawnpoint = new Point(90, 119);
    dangeroudCroko1.spawnMonsterOnGroundBelow(LifeFactory.getMonster(6220000), spawnpoint);
    dangeroudCroko1.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(6, "The huge crocodile Dyle has come out from the swamp."));
    setupTask = em.schedule("start", 3 * 60 * 60 * 1000);
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

