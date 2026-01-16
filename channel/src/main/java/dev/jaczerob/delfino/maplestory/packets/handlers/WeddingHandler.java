/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dev.jaczerob.delfino.maplestory.packets.handlers;


import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.inventory.Inventory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.KarmaManipulator;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.Marriage;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.packets.WeddingPackets;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author Drago (Dragohe4rt)
 */
@Component
public final class WeddingHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(WeddingHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.WEDDING_ACTION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {

        if (client.tryacquireClient()) {
            try {
                Character chr = client.getPlayer();
                final byte mode = packet.readByte();

                if (mode == 6) { //additem
                    short slot = packet.readShort();
                    int itemid = packet.readInt();
                    short quantity = packet.readShort();

                    Marriage marriage = client.getPlayer().getMarriageInstance();
                    if (marriage != null) {
                        try {
                            boolean groomWishlist = marriage.giftItemToSpouse(chr.getId());
                            String groomWishlistProp = "giftedItem" + (groomWishlist ? "G" : "B") + chr.getId();

                            int giftCount = marriage.getIntProperty(groomWishlistProp);
                            if (giftCount < YamlConfig.config.server.WEDDING_GIFT_LIMIT) {
                                int cid = marriage.getIntProperty(groomWishlist ? "groomId" : "brideId");
                                if (chr.getId() != cid) {   // cannot gift yourself
                                    Character spouse = marriage.getPlayerById(cid);
                                    if (spouse != null) {
                                        InventoryType type = ItemConstants.getInventoryType(itemid);
                                        Inventory chrInv = chr.getInventory(type);

                                        Item newItem = null;
                                        chrInv.lockInventory();
                                        try {
                                            Item item = chrInv.getItem((byte) slot);
                                            if (item != null) {
                                                if (!item.isUntradeable()) {
                                                    if (itemid == item.getItemId() && quantity <= item.getQuantity()) {
                                                        newItem = item.copy();
                                                        newItem.setQuantity(quantity);
                                                        marriage.addGiftItem(groomWishlist, newItem);
                                                        InventoryManipulator.removeFromSlot(client, type, slot, quantity, false, false);

                                                        KarmaManipulator.toggleKarmaFlagToUntradeable(newItem);
                                                        marriage.setIntProperty(groomWishlistProp, giftCount + 1);

                                                        client.sendPacket(WeddingPackets.getInstance().onWeddingGiftResult((byte) 0xB, marriage.getWishlistItems(groomWishlist), Collections.singletonList(newItem)));
                                                    }
                                                } else {
                                                    client.sendPacket(WeddingPackets.getInstance().onWeddingGiftResult((byte) 0xE, marriage.getWishlistItems(groomWishlist), null));
                                                }
                                            }
                                        } finally {
                                            chrInv.unlockInventory();
                                        }

                                        if (newItem != null) {
                                            if (YamlConfig.config.server.USE_ENFORCE_MERCHANT_SAVE) {
                                                chr.saveCharToDB(false);
                                            }
                                            marriage.saveGiftItemsToDb(client, groomWishlist, cid);
                                        }
                                    } else {
                                        client.sendPacket(WeddingPackets.getInstance().onWeddingGiftResult((byte) 0xE, marriage.getWishlistItems(groomWishlist), null));
                                    }
                                } else {
                                    client.sendPacket(WeddingPackets.getInstance().onWeddingGiftResult((byte) 0xE, marriage.getWishlistItems(groomWishlist), null));
                                }
                            } else {
                                client.sendPacket(WeddingPackets.getInstance().onWeddingGiftResult((byte) 0xC, marriage.getWishlistItems(groomWishlist), null));
                            }
                        } catch (NumberFormatException nfe) {
                        }
                    } else {
                        client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    }
                } else if (mode == 7) { // take items
                    packet.readByte();    // invType
                    int itemPos = packet.readByte();

                    Marriage marriage = chr.getMarriageInstance();
                    if (marriage != null) {
                        Boolean groomWishlist = marriage.isMarriageGroom(chr);
                        if (groomWishlist != null) {
                            Item item = marriage.getGiftItem(client, groomWishlist, itemPos);
                            if (item != null) {
                                if (Inventory.checkSpot(chr, item)) {
                                    marriage.removeGiftItem(groomWishlist, item);
                                    marriage.saveGiftItemsToDb(client, groomWishlist, chr.getId());

                                    InventoryManipulator.addFromDrop(client, item, true);

                                    client.sendPacket(WeddingPackets.getInstance().onWeddingGiftResult((byte) 0xF, marriage.getWishlistItems(groomWishlist), marriage.getGiftItems(client, groomWishlist)));
                                } else {
                                    client.getPlayer().dropMessage(1, "Free a slot on your inventory before collecting this item.");
                                    client.sendPacket(WeddingPackets.getInstance().onWeddingGiftResult((byte) 0xE, marriage.getWishlistItems(groomWishlist), marriage.getGiftItems(client, groomWishlist)));
                                }
                            } else {
                                client.getPlayer().dropMessage(1, "You have already collected this item.");
                                client.sendPacket(WeddingPackets.getInstance().onWeddingGiftResult((byte) 0xE, marriage.getWishlistItems(groomWishlist), marriage.getGiftItems(client, groomWishlist)));
                            }
                        }
                    } else {
                        List<Item> items = client.getAbstractPlayerInteraction().getUnclaimedMarriageGifts();
                        try {
                            Item item = items.get(itemPos);
                            if (Inventory.checkSpot(chr, item)) {
                                items.remove(itemPos);
                                Marriage.saveGiftItemsToDb(client, items, chr.getId());

                                InventoryManipulator.addFromDrop(client, item, true);
                                client.sendPacket(WeddingPackets.getInstance().onWeddingGiftResult((byte) 0xF, Collections.singletonList(""), items));
                            } else {
                                client.getPlayer().dropMessage(1, "Free a slot on your inventory before collecting this item.");
                                client.sendPacket(WeddingPackets.getInstance().onWeddingGiftResult((byte) 0xE, Collections.singletonList(""), items));
                            }
                        } catch (Exception e) {
                            client.getPlayer().dropMessage(1, "You have already collected this item.");
                            client.sendPacket(WeddingPackets.getInstance().onWeddingGiftResult((byte) 0xE, Collections.singletonList(""), items));
                        }
                    }
                } else if (mode == 8) { // out of Wedding Registry
                    client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                } else {
                    log.warn("Unhandled wedding mode: {}", mode);
                }
            } finally {
                client.releaseClient();
            }
        }
    }
}
