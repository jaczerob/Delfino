/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Tae Roon Spawner
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
    var territoryOfWanderingBear = em.getChannelServer().getMapFactory().getMap(250010304);
    const LifeFactory = Java.type('dev.jaczerob.delfino.maplestory.server.life.LifeFactory');
    var taeRoon = LifeFactory.getMonster(7220000);

    if (territoryOfWanderingBear.getMonsterById(7220000) != null) {
        em.schedule("start", 3 * 60 * 60 * 1000);
        return;
    }

    var posX;
    var posY = 390;
    posX = Math.floor((Math.random() * 700) - 800);
    const Point = Java.type('java.awt.Point');
    const spawnpoint = new Point(posX, posY);
    territoryOfWanderingBear.spawnMonsterOnGroundBelow(taeRoon, spawnpoint);

    const PacketCreator = Java.type('dev.jaczerob.delfino.maplestory.tools.PacketCreator');
    territoryOfWanderingBear.broadcastMessage(PacketCreator.serverNotice(6, "Tae Roon has appeared with a soft whistling sound."));
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

