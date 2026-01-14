/* 	Kelvin
	SingaPore VIP Face changer
	@Author AAron (aaroncsn), Cody

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/
var status = 0;
var beauty = 0;
var mface_v = Array(20005, 20012, 20013, 20020, 20021, 20026);
var fface_v = Array(21006, 21009, 21011, 21012, 21021, 21025);
var facenew = Array();

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1)  // disposing issue with stylishs found thanks to Vcoc
    {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendSimple("Let's see...I can totally transform your face into something new. Don't you want to try it? For #b#t5152038##k, you can get the face of your liking. Take your time in choosing the face of your preference...\r\n\#L2#Let me get my dream face! (Uses #i5152038# #t5152038#)#l");
        } else if (status == 1) {
            if (!cm.haveItem(5152038)) {
                cm.sendOk("Hmm ... it looks like you don't have the coupon specifically for this place. Sorry to say this, but without the coupon, there's no plastic surgery for you...");
                cm.dispose();
                return;
            }

            facenew = Array();
            if (cm.getPlayer().getGender() == 0) {
                for (var i = 0; i < mface_v.length; i++) {
                    pushIfItemExists(facenew, mface_v[i] + cm.getPlayer().getFace() % 1000 - (cm.getPlayer().getFace() % 100));
                }
            }
            if (cm.getPlayer().getGender() == 1) {
                for (var i = 0; i < fface_v.length; i++) {
                    pushIfItemExists(facenew, fface_v[i] + cm.getPlayer().getFace() % 1000 - (cm.getPlayer().getFace() % 100));
                }
            }
            cm.sendStyle("Let's see... I can totally transform your face into something new. Don't you want to try it? For #b#t5152038##k, you can get the face of your liking. Take your time in choosing the face of your preference...", facenew);
        } else if (status == 2) {
            cm.gainItem(5152038, -1);
            cm.setFace(facenew[selection]);
            cm.sendOk("Enjoy your new and improved face!");

            cm.dispose();
        }
    }
}
