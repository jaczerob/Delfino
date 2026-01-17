package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.Inventory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.KarmaManipulator;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.server.Trade;
import dev.jaczerob.delfino.maplestory.server.maps.FieldLimit;
import dev.jaczerob.delfino.maplestory.server.maps.Portal;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;

/**
 * @author Matze
 * @author Ronan - concurrency safety and reviewed minigames
 */
@Component
public final class PlayerInteractionHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(PlayerInteractionHandler.class);

    private static int establishMiniroomStatus(Character chr, boolean isMinigame) {
        if (isMinigame && FieldLimit.CANNOTMINIGAME.check(chr.getMap().getFieldLimit())) {
            return 11;
        }

        if (chr.getChalkboard() != null) {
            return 13;
        }

        return 0;
    }

    private static boolean isTradeOpen(Character chr) {
        if (chr.getTrade() != null) {   // thanks to Rien dev team
            //Apparently there is a dupe exploit that causes racing conditions when saving/retrieving from the db with stuff like trade open.
            chr.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return true;
        }

        return false;
    }

    private static boolean canPlaceStore(Character chr) {
        try {
            Point cpos = chr.getPosition();
            Portal portal = chr.getMap().findClosestTeleportPortal(cpos);
            if (portal != null && portal.getPosition().distance(cpos) < 120.0) {
                chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(10));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.PLAYER_INTERACTION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (!client.tryacquireClient()) {    // thanks GabrielSin for pointing dupes within player interactions
            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        try {
            byte mode = packet.readByte();
            final Character chr = client.getPlayer();

            if (mode == Action.CREATE.getCode()) {
                if (!chr.isAlive()) {    // thanks GabrielSin for pointing this
                    chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(4));
                    return;
                }

                byte createType = packet.readByte();
                if (createType == 3) {  // trade
                    Trade.startTrade(chr);
                }
            } else if (mode == Action.INVITE.getCode()) {
                int otherCid = packet.readInt();
                Character other = chr.getMap().getCharacterById(otherCid);
                if (other == null || chr.getId() == other.getId()) {
                    return;
                }

                Trade.inviteTrade(chr, other);
            } else if (mode == Action.DECLINE.getCode()) {
                Trade.declineTrade(chr);
            } else if (mode == Action.VISIT.getCode()) {
                if (chr.getTrade() != null && chr.getTrade().getPartner() != null) {
                    if (!chr.getTrade().isFullTrade() && !chr.getTrade().getPartner().isFullTrade()) {
                        Trade.visitTrade(chr, chr.getTrade().getPartner().getChr());
                    } else {
                        chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(2));
                    }
                } else {
                    if (isTradeOpen(chr)) {
                        return;
                    }

                    packet.readInt();
                    chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(22));
                }
            } else if (mode == Action.CHAT.getCode()) {
                if (chr.getTrade() != null) {
                    chr.getTrade().chat(packet.readString());
                }
            } else if (mode == Action.EXIT.getCode()) {
                Trade.cancelTrade(chr, Trade.TradeResult.PARTNER_CANCEL);
            } else if (mode == Action.OPEN_STORE.getCode() || mode == Action.OPEN_CASH.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                if (mode == Action.OPEN_STORE.getCode()) {
                    packet.readByte();    //01
                } else {
                    packet.readShort();
                    int birthday = packet.readInt();
                    if (!CashOperationHandler.checkBirthday(client, birthday)) {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "Please check again the birthday date."));
                    }
                }
            } else if (mode == Action.SET_MESO.getCode()) {
                chr.getTrade().setMeso(packet.readInt());
            } else if (mode == Action.SET_ITEMS.getCode()) {
                ItemInformationProvider ii = ItemInformationProvider.getInstance();
                InventoryType ivType = InventoryType.getByType(packet.readByte());
                short pos = packet.readShort();
                Item item = chr.getInventory(ivType).getItem(pos);
                short quantity = packet.readShort();
                byte targetSlot = packet.readByte();

                if (targetSlot < 1 || targetSlot > 9) {
                    log.warn("[Hack] Chr {} Trying to dupe on trade slot.", chr.getName());
                    context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                if (item == null) {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "Invalid item description."));
                    context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                if (ii.isUnmerchable(item.getItemId())) {
                    if (ItemConstants.isPet(item.getItemId())) {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "Pets are not allowed to be traded."));
                    } else {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "Cash items are not allowed to be traded."));
                    }

                    context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                if (quantity < 1 || quantity > item.getQuantity()) {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "You don't have enough quantity of the item."));
                    context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                Trade trade = chr.getTrade();
                if (trade != null) {
                    if ((quantity <= item.getQuantity() && quantity >= 0) || ItemConstants.isRechargeable(item.getItemId())) {
                        if (ii.isDropRestricted(item.getItemId())) { // ensure that undroppable items do not make it to the trade window
                            if (!KarmaManipulator.hasKarmaFlag(item)) {
                                context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "That item is untradeable."));
                                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                                return;
                            }
                        }

                        Inventory inv = chr.getInventory(ivType);
                        inv.lockInventory();
                        try {
                            Item checkItem = chr.getInventory(ivType).getItem(pos);
                            if (checkItem != item || checkItem.getPosition() != item.getPosition()) {
                                context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "Invalid item description."));
                                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                                return;
                            }

                            Item tradeItem = item.copy();
                            if (ItemConstants.isRechargeable(item.getItemId())) {
                                quantity = item.getQuantity();
                            }

                            tradeItem.setQuantity(quantity);
                            tradeItem.setPosition(targetSlot);

                            if (trade.addItem(tradeItem)) {
                                InventoryManipulator.removeFromSlot(client, ivType, item.getPosition(), quantity, true);

                                trade.getChr().sendPacket(ChannelPacketCreator.getInstance().getTradeItemAdd((byte) 0, tradeItem));
                                if (trade.getPartner() != null) {
                                    trade.getPartner().getChr().sendPacket(ChannelPacketCreator.getInstance().getTradeItemAdd((byte) 1, tradeItem));
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Chr {} tried to add {}x {} in trade (slot {}), then exception occurred", chr, ii.getName(item.getItemId()), item.getQuantity(), targetSlot, e);
                        } finally {
                            inv.unlockInventory();
                        }
                    }
                }
            } else if (mode == Action.CONFIRM.getCode()) {
                Trade.completeTrade(chr);
            } else if (mode == Action.ADD_ITEM.getCode() || mode == Action.PUT_ITEM.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                InventoryType ivType = InventoryType.getByType(packet.readByte());
                short slot = packet.readShort();
                short bundles = packet.readShort();
                Item ivItem = chr.getInventory(ivType).getItem(slot);

                if (ivItem == null || ivItem.isUntradeable()) {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "Could not perform shop operation with that item."));
                    context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                    return;
                } else if (ItemInformationProvider.getInstance().isUnmerchable(ivItem.getItemId())) {
                    if (ItemConstants.isPet(ivItem.getItemId())) {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "Pets are not allowed to be sold on the Player Store."));
                    } else {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "Cash items are not allowed to be sold on the Player Store."));
                    }

                    context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                short perBundle = packet.readShort();

                if (ItemConstants.isRechargeable(ivItem.getItemId())) {
                    perBundle = 1;
                    bundles = 1;
                } else if (ivItem.getQuantity() < (bundles * perBundle)) {     // thanks GabrielSin for finding a dupe here
                    context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "Could not perform shop operation with that item."));
                    context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                int price = packet.readInt();
                if (perBundle <= 0 || perBundle * bundles > 2000 || bundles <= 0 || price <= 0 || price > Integer.MAX_VALUE) {
                    AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + " tried to packet edit with hired merchants.");
                    log.warn("Chr {} might possibly have packet edited Hired Merchants. perBundle: {}, perBundle * bundles (This multiplied cannot be greater than 2000): {}, bundles: {}, price: {}",
                            chr.getName(), perBundle, perBundle * bundles, bundles, price);
                    return;
                }

                Item sellItem = ivItem.copy();
                if (!ItemConstants.isRechargeable(ivItem.getItemId())) {
                    sellItem.setQuantity(perBundle);
                }
            } else if (mode == Action.BUY.getCode() || mode == Action.MERCHANT_BUY.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                int itemid = packet.readByte();
                short quantity = packet.readShort();
                if (quantity < 1) {
                    AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + " tried to packet edit with a hired merchant and or player shop.");
                    log.warn("Chr {} tried to buy item {} with quantity {}", chr.getName(), itemid, quantity);
                    client.disconnect(true, false);
                }
            }
        } finally {
            client.releaseClient();
        }
    }

    public enum Action {
        CREATE(0),
        INVITE(2),
        DECLINE(3),
        VISIT(4),
        ROOM(5),
        CHAT(6),
        CHAT_THING(8),
        EXIT(0xA),
        OPEN_STORE(0xB),
        OPEN_CASH(0xE),
        SET_ITEMS(0xF),
        SET_MESO(0x10),
        CONFIRM(0x11),
        TRANSACTION(0x14),
        ADD_ITEM(0x16),
        BUY(0x17),
        UPDATE_MERCHANT(0x19),
        UPDATE_PLAYERSHOP(0x1A),
        REMOVE_ITEM(0x1B),
        BAN_PLAYER(0x1C),
        MERCHANT_THING(0x1D),
        OPEN_THING(0x1E),
        PUT_ITEM(0x21),
        MERCHANT_BUY(0x22),
        TAKE_ITEM_BACK(0x26),
        MAINTENANCE_OFF(0x27),
        MERCHANT_ORGANIZE(0x28),
        CLOSE_MERCHANT(0x29),
        REAL_CLOSE_MERCHANT(0x2A),
        MERCHANT_MESO(0x2B),
        SOMETHING(0x2D),
        VIEW_VISITORS(0x2E),
        VIEW_BLACKLIST(0x2F),
        ADD_TO_BLACKLIST(0x30),
        REMOVE_FROM_BLACKLIST(0x31),
        REQUEST_TIE(0x32),
        ANSWER_TIE(0x33),
        GIVE_UP(0x34),
        EXIT_AFTER_GAME(0x38),
        CANCEL_EXIT_AFTER_GAME(0x39),
        READY(0x3A),
        UN_READY(0x3B),
        EXPEL(0x3C),
        START(0x3D),
        GET_RESULT(0x3E),
        SKIP(0x3F),
        MOVE_OMOK(0x40),
        SELECT_CARD(0x44);
        final byte code;

        Action(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }
    }
}
