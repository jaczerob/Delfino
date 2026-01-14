/* Ms. Tan
	Henesys Skin Change.
*/
var status;
var skin = Array(0, 1, 2, 3, 4);
var price = 1000000;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {  // disposing issue with stylishs found thanks to Vcoc
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }


        if (status == 0) {
            cm.sendSimple("Well, hello! Welcome to the Henesys Skin-Care! Would you like to have a firm, tight, healthy looking skin like mine?  With a #b#t5153000##k, you can let us take care of the rest and have the kind of skin you've always wanted~!\r\n#L1#Skin Care: #i5153000##t5153000##l");
        } else if (status == 1) {
            if (cm.haveItem(5153000)) {
                cm.sendStyle("With our specialized machine, you can see yourself after the treatment in advance. What kind of skin-treatment would you like to do? Choose the style of your liking.", skin);
            } else {
                cm.sendOk("Um... you don't have the skin-care coupon you need to receive the treatment. Sorry, but I am afraid we can't do it for you...");
                cm.dispose();

            }
        } else {
            cm.gainItem(5153000, -1);
            cm.setSkin(selection);
            cm.sendOk("Enjoy your new and improved skin!");
            cm.dispose();
        }
    }
}
