/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Centipede Spawner
 -- Edited by --------------------------------------------------------------------------------------
 Ronan - based on xQuasar's King Clang spawner

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
    var herbGarden = em.getChannelServer().getMapFactory().getMap(251010102);

    if (herbGarden.getMonsterById(5220004) != null) {
        em.schedule("start", 3 * 60 * 60 * 1000);
        return;
    }

    const LifeFactory = Java.type('dev.jaczerob.delfino.maplestory.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');
    const PacketCreator = Java.type('dev.jaczerob.delfino.maplestory.tools.PacketCreator');

    var gcent = LifeFactory.getMonster(5220004);
    herbGarden.spawnMonsterOnGroundBelow(gcent, new Point(560, 50));
    herbGarden.broadcastMessage(PacketCreator.serverNotice(6, "From the mists surrounding the herb garden, the gargantuous Giant Centipede appears."));
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

