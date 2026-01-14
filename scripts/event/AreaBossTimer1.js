/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Timer1 Spawner
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
    var whirlpoolOfTime = em.getChannelServer().getMapFactory().getMap(220050100);
    const LifeFactory = Java.type('dev.jaczerob.delfino.maplestory.server.life.LifeFactory');
    var timer1 = LifeFactory.getMonster(5220003);

    if (whirlpoolOfTime.getMonsterById(5220003) != null) {
        em.schedule("start", 3 * 60 * 60 * 1000);
        return;
    }

    var posX;
    var posY = 1030;
    posX = Math.floor((Math.random() * 770) - 770);
    const Point = Java.type('java.awt.Point');
    const spawnpoint = new Point(posX, posY);
    whirlpoolOfTime.spawnMonsterOnGroundBelow(timer1, spawnpoint);

    const ChannelPacketCreator = Java.type('dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator');
    whirlpoolOfTime.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(6, "Tick-Tock Tick-Tock! Timer makes it's presence known."));
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

