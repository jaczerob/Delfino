
/*
 * Author: kevintjuh93
 *
*/
function start(ms) {
    if (ms.getPlayer().getMapId() == 110000000 || (ms.getPlayer().getMapId() >= 100000000 && ms.getPlayer().getMapId() < 105040300)) {
        ms.explorerQuest(29005, "Beginner Explorer");//Beginner Explorer
    } else if (ms.getPlayer().getMapId() >= 105040300 && ms.getPlayer().getMapId() <= 105090900) {
        ms.explorerQuest(29014, "Sleepywood Explorer");//Sleepywood Explorer
    } else if (ms.getPlayer().getMapId() >= 200000000 && ms.getPlayer().getMapId() <= 211041800) {
        ms.explorerQuest(29006, "El Nath Mts. Explorer");//El Nath Mts. Explorer
    } else if (ms.getPlayer().getMapId() >= 220000000 && ms.getPlayer().getMapId() <= 222020000) {
        ms.explorerQuest(29007, "Ludus Lake Explorer");//Ludus Lake Explorer
    } else if (ms.getPlayer().getMapId() >= 230000000 && ms.getPlayer().getMapId() <= 230040401) {
        ms.explorerQuest(29008, "Undersea Explorer");//Undersea Explorer
    } else if (ms.getPlayer().getMapId() >= 250000000 && ms.getPlayer().getMapId() <= 251010500) {
        ms.explorerQuest(29009, "Mu Lung Explorer");//Mu Lung Explorer
    } else if (ms.getPlayer().getMapId() >= 260000000 && ms.getPlayer().getMapId() <= 261030000) {
        ms.explorerQuest(29010, "Nihal Desert Explorer");//Nihal Desert Explorer
    } else if (ms.getPlayer().getMapId() >= 240000000 && ms.getPlayer().getMapId() <= 240050000) {
        ms.explorerQuest(29011, "Minar Forest Explorer");//Minar Forest Explorer
    }
    if (ms.getPlayer().getMapId() == 104000000) {
        ms.mapEffect("maplemap/enter/104000000");
    }
}