package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.inventory.Inventory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.CashShop;
import dev.jaczerob.delfino.maplestory.server.CashShop.CashItem;
import dev.jaczerob.delfino.maplestory.server.CashShop.CashItemFactory;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.service.NoteService;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.DAYS;

@Component
public class CashOperationHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(CashOperationHandler.class);

    private final NoteService noteService;

    public CashOperationHandler(NoteService noteService) {
        this.noteService = noteService;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CASHSHOP_OPERATION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        CashShop cs = chr.getCashShop();

        if (!cs.isOpened()) {
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        if (client.tryacquireClient()) {     // thanks Thora for finding out an exploit within cash operations
            try {
                final int action = packet.readByte();
                if (action == 0x03 || action == 0x1E) {
                    packet.readByte();
                    final int useNX = packet.readInt();
                    final int snCS = packet.readInt();
                    CashItem cItem = CashItemFactory.getItem(snCS);
                    if (!canBuy(chr, cItem, cs.getCash(useNX))) {
                        log.error("Denied to sell cash item with SN {}", snCS); // preventing NPE here thanks to MedicOP
                        client.enableCSActions();
                        return;
                    }

                    if (action == 0x03) { // Item
                        if (ItemConstants.isCashStore(cItem.getItemId()) && chr.getLevel() < 16) {
                            client.enableCSActions();
                            return;
                        } else if (ItemConstants.isRateCoupon(cItem.getItemId()) && !YamlConfig.config.server.USE_SUPPLY_RATE_COUPONS) {
                            chr.dropMessage(1, "Rate coupons are currently unavailable to purchase.");
                            client.enableCSActions();
                            return;
                        } else if (ItemConstants.isMapleLife(cItem.getItemId()) && chr.getLevel() < 30) {
                            client.enableCSActions();
                            return;
                        }

                        Item item = cItem.toItem();
                        cs.gainCash(useNX, cItem, chr.getWorld());  // thanks Rohenn for noticing cash operations after item acquisition
                        cs.addToInventory(item);
                        client.sendPacket(ChannelPacketCreator.getInstance().showBoughtCashItem(item, client.getAccID()));
                    } else { // Package
                        cs.gainCash(useNX, cItem, chr.getWorld());

                        List<Item> cashPackage = CashItemFactory.getPackage(cItem.getItemId());
                        for (Item item : cashPackage) {
                            cs.addToInventory(item);
                        }
                        client.sendPacket(ChannelPacketCreator.getInstance().showBoughtCashPackage(cashPackage, client.getAccID()));
                    }
                    client.sendPacket(ChannelPacketCreator.getInstance().showCash(chr));
                } else if (action == 0x04) {//TODO check for gender
                    int birthday = packet.readInt();
                    CashItem cItem = CashItemFactory.getItem(packet.readInt());
                    Map<String, String> recipient = Character.getCharacterFromDatabase(packet.readString());
                    String message = packet.readString();
                    if (!canBuy(chr, cItem, cs.getCash(CashShop.NX_PREPAID)) || message.isEmpty() || message.length() > 73) {
                        client.enableCSActions();
                        return;
                    }
                    if (!checkBirthday(client, birthday)) {
                        client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0xC4));
                        return;
                    } else if (recipient == null) {
                        client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0xA9));
                        return;
                    } else if (recipient.get("accountid").equals(String.valueOf(client.getAccID()))) {
                        client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0xA8));
                        return;
                    }
                    cs.gainCash(4, cItem, chr.getWorld());
                    cs.gift(Integer.parseInt(recipient.get("id")), chr.getName(), message, cItem.getSN());
                    client.sendPacket(ChannelPacketCreator.getInstance().showGiftSucceed(recipient.get("name"), cItem));
                    client.sendPacket(ChannelPacketCreator.getInstance().showCash(chr));

                    String noteMessage = chr.getName() + " has sent you a gift! Go check out the Cash Shop.";
                    noteService.sendNormal(noteMessage, chr.getName(), recipient.get("name"));

                    Character receiver = client.getChannelServer().getPlayerStorage().getCharacterByName(recipient.get("name"));
                    if (receiver != null) {
                        noteService.show(receiver);
                    }
                } else if (action == 0x05) { // Modify wish list
                    cs.clearWishList();
                    for (byte i = 0; i < 10; i++) {
                        int sn = packet.readInt();
                        CashItem cItem = CashItemFactory.getItem(sn);
                        if (cItem != null && cItem.isOnSale() && sn != 0) {
                            cs.addToWishList(sn);
                        }
                    }
                    client.sendPacket(ChannelPacketCreator.getInstance().showWishList(chr, true));
                } else if (action == 0x06) { // Increase Inventory Slots
                    packet.skip(1);
                    int cash = packet.readInt();
                    byte mode = packet.readByte();
                    if (mode == 0) {
                        byte type = packet.readByte();
                        if (cs.getCash(cash) < 4000) {
                            client.enableCSActions();
                            return;
                        }
                        int qty = 4;
                        if (!chr.canGainSlots(type, qty)) {
                            client.enableCSActions();
                            return;
                        }
                        cs.gainCash(cash, -4000);
                        if (chr.gainSlots(type, qty, false)) {
                            client.sendPacket(ChannelPacketCreator.getInstance().showBoughtInventorySlots(type, chr.getSlots(type)));
                            client.sendPacket(ChannelPacketCreator.getInstance().showCash(chr));
                        } else {
                            log.warn("Could not add {} slots of type {} for chr {}", qty, type, Character.makeMapleReadable(chr.getName()));
                        }
                    } else {
                        CashItem cItem = CashItemFactory.getItem(packet.readInt());
                        int type = (cItem.getItemId() - 9110000) / 1000;
                        if (!canBuy(chr, cItem, cs.getCash(cash))) {
                            client.enableCSActions();
                            return;
                        }
                        int qty = 8;
                        if (!chr.canGainSlots(type, qty)) {
                            client.enableCSActions();
                            return;
                        }
                        cs.gainCash(cash, cItem, chr.getWorld());
                        if (chr.gainSlots(type, qty, false)) {
                            client.sendPacket(ChannelPacketCreator.getInstance().showBoughtInventorySlots(type, chr.getSlots(type)));
                            client.sendPacket(ChannelPacketCreator.getInstance().showCash(chr));
                        } else {
                            log.warn("Could not add {} slots of type {} for chr {}", qty, type, Character.makeMapleReadable(chr.getName()));
                        }
                    }
                } else if (action == 0x07) { // Increase Storage Slots
                    packet.skip(1);
                    int cash = packet.readInt();
                    byte mode = packet.readByte();
                    if (mode == 0) {
                        if (cs.getCash(cash) < 4000) {
                            client.enableCSActions();
                            return;
                        }
                        int qty = 4;
                        if (!chr.getStorage().canGainSlots(qty)) {
                            client.enableCSActions();
                            return;
                        }
                        cs.gainCash(cash, -4000);
                        if (chr.getStorage().gainSlots(qty)) {
                            log.debug("Chr {} bought {} slots to their account storage.", client.getPlayer().getName(), qty);
                            chr.setUsedStorage();

                            client.sendPacket(ChannelPacketCreator.getInstance().showBoughtStorageSlots(chr.getStorage().getSlots()));
                            client.sendPacket(ChannelPacketCreator.getInstance().showCash(chr));
                        } else {
                            log.warn("Could not add {} slots to {}'s account.", qty, Character.makeMapleReadable(chr.getName()));
                        }
                    } else {
                        CashItem cItem = CashItemFactory.getItem(packet.readInt());

                        if (!canBuy(chr, cItem, cs.getCash(cash))) {
                            client.enableCSActions();
                            return;
                        }
                        int qty = 8;
                        if (!chr.getStorage().canGainSlots(qty)) {
                            client.enableCSActions();
                            return;
                        }
                        cs.gainCash(cash, cItem, chr.getWorld());
                        if (chr.getStorage().gainSlots(qty)) {    // thanks ABaldParrot & Thora for detecting storage issues here
                            log.debug("Chr {} bought {} slots to their account storage", client.getPlayer().getName(), qty);
                            chr.setUsedStorage();

                            client.sendPacket(ChannelPacketCreator.getInstance().showBoughtStorageSlots(chr.getStorage().getSlots()));
                            client.sendPacket(ChannelPacketCreator.getInstance().showCash(chr));
                        } else {
                            log.warn("Could not add {} slots to {}'s account", qty, Character.makeMapleReadable(chr.getName()));
                        }
                    }
                } else if (action == 0x08) { // Increase Character Slots
                    packet.skip(1);
                    int cash = packet.readInt();
                    CashItem cItem = CashItemFactory.getItem(packet.readInt());

                    if (!canBuy(chr, cItem, cs.getCash(cash))) {
                        client.enableCSActions();
                        return;
                    }
                    if (!client.canGainCharacterSlot()) {
                        chr.dropMessage(1, "You have already used up all 12 extra character slots.");
                        client.enableCSActions();
                        return;
                    }
                    cs.gainCash(cash, cItem, chr.getWorld());
                    if (client.gainCharacterSlot()) {
                        client.sendPacket(ChannelPacketCreator.getInstance().showBoughtCharacterSlot(client.getCharacterSlots()));
                        client.sendPacket(ChannelPacketCreator.getInstance().showCash(chr));
                    } else {
                        log.warn("Could not add a chr slot to {}'s account", Character.makeMapleReadable(chr.getName()));
                        client.enableCSActions();
                        return;
                    }
                } else if (action == 0x0D) { // Take from Cash Inventory
                    Item item = cs.findByCashId(packet.readInt());
                    if (item == null) {
                        client.enableCSActions();
                        return;
                    }
                    if (chr.getInventory(item.getInventoryType()).addItem(item) != -1) {
                        cs.removeFromInventory(item);
                        client.sendPacket(ChannelPacketCreator.getInstance().takeFromCashInventory(item));
                    }
                } else if (action == 0x0E) { // Put into Cash Inventory
                    int cashId = packet.readInt();
                    packet.skip(4);

                    byte invType = packet.readByte();
                    if (invType < 1 || invType > 5) {
                        client.disconnect(false, false);
                        return;
                    }

                    Inventory mi = chr.getInventory(InventoryType.getByType(invType));
                    Item item = mi.findByCashId(cashId);
                    if (item == null) {
                        client.enableCSActions();
                        return;
                    } else if (client.getPlayer().getPetIndex(item.getPetId()) > -1) {
                        chr.getClient().sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "You cannot put the pet you currently equip into the Cash Shop inventory."));
                        client.enableCSActions();
                        return;
                    } else if (ItemId.isWeddingRing(item.getItemId()) || ItemId.isWeddingToken(item.getItemId())) {
                        chr.getClient().sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "You cannot put relationship items into the Cash Shop inventory."));
                        client.enableCSActions();
                        return;
                    }
                    cs.addToInventory(item);
                    mi.removeSlot(item.getPosition());
                    client.sendPacket(ChannelPacketCreator.getInstance().putIntoCashInventory(item, client.getAccID()));
                } else if (action == 0x20) {
                    int serialNumber = packet.readInt();  // thanks GabrielSin for detecting a potential exploit with 1 meso cash items.
                    if (serialNumber / 10000000 != 8) {
                        client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0xC0));
                        return;
                    }

                    CashItem item = CashItemFactory.getItem(serialNumber);
                    if (item == null || !item.isOnSale()) {
                        client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0xC0));
                        return;
                    }

                    int itemId = item.getItemId();
                    int itemPrice = item.getPrice();
                    if (itemPrice <= 0) {
                        client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0xC0));
                        return;
                    }

                    if (chr.getMeso() >= itemPrice) {
                        if (chr.canHold(itemId)) {
                            chr.gainMeso(-itemPrice, false);
                            InventoryManipulator.addById(client, itemId, (short) 1, "", -1);
                            client.sendPacket(ChannelPacketCreator.getInstance().showBoughtQuestItem(itemId));
                        }
                    }
                    client.sendPacket(ChannelPacketCreator.getInstance().showCash(client.getPlayer()));
                } else if (action == 0x2E) { //name change
                    CashItem cItem = CashItemFactory.getItem(packet.readInt());
                    if (cItem == null || !canBuy(chr, cItem, cs.getCash(CashShop.NX_PREPAID))) {
                        client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0));
                        client.enableCSActions();
                        return;
                    }
                    if (cItem.getSN() == 50600000 && YamlConfig.config.server.ALLOW_CASHSHOP_NAME_CHANGE) {
                        packet.readString(); //old name
                        String newName = packet.readString();
                        if (!Character.canCreateChar(newName) || chr.getLevel() < 10) { //(longest ban duration isn't tracked currently)
                            client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0));
                            client.enableCSActions();
                            return;
                        } else if (client.getTempBanCalendar() != null && (client.getTempBanCalendar().getTimeInMillis() + DAYS.toMillis(30)) > Calendar.getInstance().getTimeInMillis()) {
                            client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0));
                            client.enableCSActions();
                            return;
                        }
                        if (chr.registerNameChange(newName)) { //success
                            Item item = cItem.toItem();
                            client.sendPacket(ChannelPacketCreator.getInstance().showNameChangeSuccess(item, client.getAccID()));
                            cs.gainCash(4, cItem, chr.getWorld());
                            cs.addToInventory(item);
                        } else {
                            client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0));
                        }
                    }
                    client.enableCSActions();
                } else if (action == 0x31) { //world transfer
                    CashItem cItem = CashItemFactory.getItem(packet.readInt());
                    if (cItem == null || !canBuy(chr, cItem, cs.getCash(CashShop.NX_PREPAID))) {
                        client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0));
                        client.enableCSActions();
                        return;
                    }
                    if (cItem.getSN() == 50600001 && YamlConfig.config.server.ALLOW_CASHSHOP_WORLD_TRANSFER) {
                        int newWorldSelection = packet.readInt();

                        int worldTransferError = chr.checkWorldTransferEligibility();
                        if (worldTransferError != 0 || newWorldSelection >= Server.getInstance().getWorldsSize() || Server.getInstance().getWorldsSize() <= 1) {
                            client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0));
                            return;
                        } else if (newWorldSelection == client.getWorld()) {
                            client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0xDC));
                            return;
                        } else if (client.getAvailableCharacterWorldSlots(newWorldSelection) < 1 || Server.getInstance().getAccountWorldCharacterCount(client.getAccID(), newWorldSelection) >= 3) {
                            client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0xDF));
                            return;
                        } else if (chr.registerWorldTransfer(newWorldSelection)) {
                            Item item = cItem.toItem();
                            client.sendPacket(ChannelPacketCreator.getInstance().showWorldTransferSuccess(item, client.getAccID()));
                            cs.gainCash(4, cItem, chr.getWorld());
                            cs.addToInventory(item);
                        } else {
                            client.sendPacket(ChannelPacketCreator.getInstance().showCashShopMessage((byte) 0));
                        }
                    }
                    client.enableCSActions();
                } else {
                    log.warn("Unhandled action: {}, packet: {}", action, packet);
                }
            } finally {
                client.releaseClient();
            }
        } else {
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
        }
    }

    public static boolean checkBirthday(Client client, int idate) {
        int year = idate / 10000;
        int month = (idate - year * 10000) / 100;
        int day = idate - year * 10000 - month * 100;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, month - 1, day);
        return client.checkBirthDate(cal);
    }

    private static boolean canBuy(Character chr, CashItem item, int cash) {
        if (item != null && item.isOnSale() && item.getPrice() <= cash) {
            log.debug("Chr {} bought cash item {} (SN {}) for {}", chr, ItemInformationProvider.getInstance().getName(item.getItemId()), item.getSN(), item.getPrice());
            return true;
        } else {
            return false;
        }
    }
}
