/* Yulete
	Yulete's Office (926110203)
	Magatia NPC
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function playersTooClose() {
    var npcpos = cm.getMap().getMapObject(cm.getNpcObjectId()).getPosition();
    var listchr = cm.getMap().getPlayers();

    for (var iterator = listchr.iterator(); iterator.hasNext();) {
        var chr = iterator.next();

        var chrpos = chr.getPosition();
        if (Math.sqrt(Math.pow((npcpos.getX() - chrpos.getX()), 2) + Math.pow((npcpos.getY() - chrpos.getY()), 2)) < 310) {
            return true;
        }
    }

    return false;
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        var eim = cm.getEventInstance();

        if (cm.getMapId() == 926110203) {
            if (status == 0) {
                var state = eim.getIntProperty("yuleteTalked");

                if (state == -1) {
                    cm.sendOk("Heh, it seems you guys have company. Have fun with them, as I politely request my leave.");

                } else if (playersTooClose()) {
                    cm.sendOk("Oh, hello there. I have been #bmonitoring your moves#k since you guys entered this perimeter. Quite the feat reaching here, I commend all of you. Now, now, look at the time, I've got an appointment right now, I'm afraid I will need to request my leave. But worry not, my #raccessors#k will deal with all of you. Now, if you permit me, I'm leaving now.");

                    eim.setIntProperty("yuleteTalked", -1);
                } else if (eim.getIntProperty("npcShocked") == 0) {
                    cm.sendOk("Ho~ Aren't you quite the sneaky one? Well, it matters not. I have been #bmonitoring your moves#k since you guys entered this perimeter. Quite the feat reaching here, I commend all of you. Now, now, look at the time, I've got an appointment right now, I'm afraid I will need to request my leave. But worry not, my #raccessors#k will deal with all of you. Now, if you permit me, I'm leaving now.");

                    eim.setIntProperty("yuleteTalked", -1);
                } else {
                    cm.sendOk("... Hah! What, wh-- How did you get here?! I though I had sealed all paths here! No matter, this situation will be resolved soon. Guys: DEPLOY the #rmaster weapon#k!! You! Yes, you. Don't you think this ends here, look back at your companions, they need some help! I'll be retreating for now.");

                    eim.setIntProperty("yuleteTalked", 1);
                }
            }

            cm.dispose();
        } else {
            if (status == 0) {
                if (eim.isEventCleared()) {
                    cm.sendOk("Nooooo... I have been beated? But how? Everything I did was for the sake of the development of a greater alchemy! You can't jail me, I did what everybody standing in a place like mine would do! But no, they simply decided to damp up the progress of the science JUST BECAUSE it was deemed dangerous??? Oh, come on!");
                } else {
                    var state = eim.getIntProperty("yuletePassed");

                    const LifeFactory = Java.type('dev.jaczerob.delfino.maplestory.server.life.LifeFactory');
                    const Point = Java.type('java.awt.Point');
                    if (state == -1) {
                        cm.sendOk("Behold! The pinnacle of Magatia's alchemy studies! Hahahahahahaha...");
                    } else if (state == 0) {
                        cm.sendOk("You guys are such a pain, geez. Very well, I present you my newest weapon, brought by the finest alchemy, #rFrankenroid#k.");
                        eim.dropMessage(5, "Yulete: I present you my newest weapon, brought by the finest alchemy, Frankenroid!");

                        var mapobj = eim.getMapInstance(926110401);
                        var bossobj = LifeFactory.getMonster(9300151);
                        mapobj.spawnMonsterOnGroundBelow(bossobj, new Point(250, 100));

                        eim.setIntProperty("statusStg7", 1);
                        eim.setIntProperty("yuletePassed", -1);
                    } else {
                        cm.sendOk("You guys are such a pain, geez. Very well, I present you my newest weapon, brought by the finest combined alchemy of Alcadno's and Zenumist's, those that the boring people of Magatia societies have banned to bring along, the #rmighty Frankenroid#k!");
                        eim.dropMessage(5, "Yulete: I present you my newest weapon, brought by the finest combined alchemy of Alcadno's and Zenumist's, those that the boring people of Magatia societies have banned to bring along, the mighty Frankenroid!!");

                        var mapobj = eim.getMapInstance(926110401);
                        var bossobj = LifeFactory.getMonster(9300152);
                        mapobj.spawnMonsterOnGroundBelow(bossobj, new Point(250, 100));

                        eim.setIntProperty("statusStg7", 2);
                        eim.setIntProperty("yuletePassed", -1);
                    }
                }
            }

            cm.dispose();
        }
    }
}