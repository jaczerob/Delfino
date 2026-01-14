/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Chimera/Kimera Spawner
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
    const LifeFactory = Java.type('dev.jaczerob.delfino.maplestory.server.life.LifeFactory');
    var labSecretBasementPath = em.getChannelServer().getMapFactory().getMap(261030000);
    var chimera = LifeFactory.getMonster(8220002);

    if (labSecretBasementPath.getMonsterById(8220002) != null) {
        em.schedule("start", 3 * 60 * 60 * 1000);
        return;
    }

    var posX;
    var posY = 180;
    posX = (Math.floor(Math.random() * 900) - 900);
    const Point = Java.type('java.awt.Point');
    const spawnpoint = new Point(posX, posY);
    labSecretBasementPath.spawnMonsterOnGroundBelow(chimera, spawnpoint);

    const ChannelPacketCreator = Java.type('dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator');
    labSecretBasementPath.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(6, "Kimera has appeared out of the darkness of the underground with a glitter in her eyes."));
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

