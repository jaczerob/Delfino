/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Nine Tailed Fox (Old Fox) Spawner
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
    var moonRidge = em.getChannelServer().getMapFactory().getMap(222010310);
    const LifeFactory = Java.type('dev.jaczerob.delfino.maplestory.server.life.LifeFactory');
    var nineTailedFox = LifeFactory.getMonster(7220001);
    if (moonRidge.getMonsterById(7220001) != null) {
        em.schedule("start", 3 * 60 * 60 * 1000);
        return;
    }
    var posX;
    var posY = 33;
    posX = Math.floor((Math.random() * 1300) - 800);
    const Point = Java.type('java.awt.Point');
    const spawnpoint = new Point(posX, posY);
    moonRidge.spawnMonsterOnGroundBelow(nineTailedFox, spawnpoint);

    const PacketCreator = Java.type('dev.jaczerob.delfino.maplestory.tools.PacketCreator');
    moonRidge.broadcastMessage(PacketCreator.serverNotice(6, "As the moon light dims, a long fox cry can be heard and the presence of the old fox can be felt"));
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

