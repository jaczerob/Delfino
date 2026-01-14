
/* ===========================================================
        @author Resonance
	NPC Name: 		Pusla
	Map(s): 		Snow Island: Rien (140000000)
	Description: 	Open Storage
=============================================================
Version 1.0 - Script Done.(11/6/2010)
=============================================================
*/
function start() {
    cm.getPlayer().getStorage().sendStorage(cm.getClient(), 1200000);
    cm.dispose();
}
