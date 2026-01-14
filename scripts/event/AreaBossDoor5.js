/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Door boss Spawner (based on xQuasar's King Clang spawner)
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
    var bossMobid = 9400612;
    var bossMapid = 677000001;
    var bossMsg = "Marbas has appeared!";

    var map = em.getChannelServer().getMapFactory().getMap(bossMapid);
    if (map.getMonsterById(bossMobid) != null) {
        em.schedule("start", 3 * 60 * 60 * 1000);
        return;
    }

    const LifeFactory = Java.type('dev.jaczerob.delfino.maplestory.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');
    const ChannelPacketCreator = Java.type('dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator');

    var boss = LifeFactory.getMonster(bossMobid);
    var bossPos = new Point(461, 61);
    map.spawnMonsterOnGroundBelow(boss, bossPos);
    map.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(6, bossMsg));

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

