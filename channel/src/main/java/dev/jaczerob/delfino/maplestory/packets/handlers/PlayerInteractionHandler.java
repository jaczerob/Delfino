package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.Inventory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.KarmaManipulator;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.server.Trade;
import dev.jaczerob.delfino.maplestory.server.maps.*;
import dev.jaczerob.delfino.maplestory.server.maps.MiniGame.MiniGameType;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author Matze
 * @author Ronan - concurrency safety and reviewed minigames
 */
@Component
public final class PlayerInteractionHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.PLAYER_INTERACTION;
    }

    private static final Logger log = LoggerFactory.getLogger(PlayerInteractionHandler.class);

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

    private static int establishMiniroomStatus(Character chr, boolean isMinigame) {
        if (isMinigame && FieldLimit.CANNOTMINIGAME.check(chr.getMap().getFieldLimit())) {
            return 11;
        }

        if (chr.getChalkboard() != null) {
            return 13;
        }

        return 0;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (!client.tryacquireClient()) {    // thanks GabrielSin for pointing dupes within player interactions
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
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
                } else if (createType == 1) { // omok mini game
                    int status = establishMiniroomStatus(chr, true);
                    if (status > 0) {
                        chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(status));
                        return;
                    }

                    String desc = packet.readString();
                    String pw;

                    if (packet.readByte() != 0) {
                        pw = packet.readString();
                    } else {
                        pw = "";
                    }

                    int type = packet.readByte();
                    if (type > 11) {
                        type = 11;
                    } else if (type < 0) {
                        type = 0;
                    }
                    if (!chr.haveItem(ItemId.MINI_GAME_BASE + type)) {
                        chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(6));
                        return;
                    }

                    MiniGame game = new MiniGame(chr, desc, pw);
                    chr.setMiniGame(game);
                    game.setPieceType(type);
                    game.setGameType(MiniGameType.OMOK);
                    chr.getMap().addMapObject(game);
                    chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().addOmokBox(chr, 1, 0));
                    game.sendOmok(client, type);
                } else if (createType == 2) { // matchcard
                    int status = establishMiniroomStatus(chr, true);
                    if (status > 0) {
                        chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(status));
                        return;
                    }

                    String desc = packet.readString();
                    String pw;

                    if (packet.readByte() != 0) {
                        pw = packet.readString();
                    } else {
                        pw = "";
                    }

                    int type = packet.readByte();
                    if (type > 2) {
                        type = 2;
                    } else if (type < 0) {
                        type = 0;
                    }
                    if (!chr.haveItem(ItemId.MATCH_CARDS)) {
                        chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(6));
                        return;
                    }

                    MiniGame game = new MiniGame(chr, desc, pw);
                    game.setPieceType(type);
                    if (type == 0) {
                        game.setMatchesToWin(6);
                    } else if (type == 1) {
                        game.setMatchesToWin(10);
                    } else if (type == 2) {
                        game.setMatchesToWin(15);
                    }
                    game.setGameType(MiniGameType.MATCH_CARD);
                    chr.setMiniGame(game);
                    chr.getMap().addMapObject(game);
                    chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().addMatchCardBox(chr, 1, 0));
                    game.sendMatchCard(client, type);
                } else if (createType == 4 || createType == 5) { // shop
                    if (!GameConstants.isFreeMarketRoom(chr.getMapId())) {
                        chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(15));
                        return;
                    }

                    int status = establishMiniroomStatus(chr, false);
                    if (status > 0) {
                        chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(status));
                        return;
                    }

                    if (!canPlaceStore(chr)) {
                        return;
                    }

                    String desc = packet.readString();
                    packet.skip(3);
                    int itemId = packet.readInt();
                    if (chr.getInventory(InventoryType.CASH).countById(itemId) < 1) {
                        chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(6));
                        return;
                    }

                    if (ItemConstants.isPlayerShop(itemId)) {
                        PlayerShop shop = new PlayerShop(chr, desc, itemId);
                        chr.setPlayerShop(shop);
                        chr.getMap().addMapObject(shop);
                        shop.sendShop(client);
                        client.getWorldServer().registerPlayerShop(shop);
                        //client.sendPacket(PacketCreator.getPlayerShopRemoveVisitor(1));
                    } else if (ItemConstants.isHiredMerchant(itemId)) {
                        HiredMerchant merchant = new HiredMerchant(chr, desc, itemId);
                        chr.setHiredMerchant(merchant);
                        client.getWorldServer().registerHiredMerchant(merchant);
                        chr.getClient().getChannelServer().addHiredMerchant(chr.getId(), merchant);
                        chr.sendPacket(ChannelPacketCreator.getInstance().getHiredMerchant(chr, merchant, true));
                    }
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
                        return;
                    }
                } else {
                    if (isTradeOpen(chr)) {
                        return;
                    }

                    int oid = packet.readInt();
                    MapObject ob = chr.getMap().getMapObject(oid);
                    if (ob instanceof PlayerShop shop) {
                        shop.visitShop(chr);
                    } else if (ob instanceof MiniGame game) {
                        packet.skip(1);
                        String pw = packet.available() > 1 ? packet.readString() : "";

                        if (game.checkPassword(pw)) {
                            if (game.hasFreeSlot() && !game.isVisitor(chr)) {
                                game.addVisitor(chr);
                                chr.setMiniGame(game);
                                switch (game.getGameType()) {
                                    case OMOK:
                                        game.sendOmok(client, game.getPieceType());
                                        break;
                                    case MATCH_CARD:
                                        game.sendMatchCard(client, game.getPieceType());
                                        break;
                                }
                            } else {
                                chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(2));
                            }
                        } else {
                            chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(22));
                        }
                    } else if (ob instanceof HiredMerchant merchant && chr.getHiredMerchant() == null) {
                        merchant.visitShop(chr);
                    }
                }
            } else if (mode == Action.CHAT.getCode()) { // chat lol
                HiredMerchant merchant = chr.getHiredMerchant();
                if (chr.getTrade() != null) {
                    chr.getTrade().chat(packet.readString());
                } else if (chr.getPlayerShop() != null) { //mini game
                    PlayerShop shop = chr.getPlayerShop();
                    if (shop != null) {
                        shop.chat(client, packet.readString());
                    }
                } else if (chr.getMiniGame() != null) {
                    MiniGame game = chr.getMiniGame();
                    if (game != null) {
                        game.chat(client, packet.readString());
                    }
                } else if (merchant != null) {
                    merchant.sendMessage(chr, packet.readString());
                }
            } else if (mode == Action.EXIT.getCode()) {
                if (chr.getTrade() != null) {
                    Trade.cancelTrade(chr, Trade.TradeResult.PARTNER_CANCEL);
                } else {
                    chr.closePlayerShop();
                    chr.closeMiniGame(false);
                    chr.closeHiredMerchant(true);
                }
            } else if (mode == Action.OPEN_STORE.getCode() || mode == Action.OPEN_CASH.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                if (mode == Action.OPEN_STORE.getCode()) {
                    packet.readByte();    //01
                } else {
                    packet.readShort();
                    int birthday = packet.readInt();
                    if (!CashOperationHandler.checkBirthday(client, birthday)) { // birthday check here found thanks to lucasziron
                        client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "Please check again the birthday date."));
                        return;
                    }

                    client.sendPacket(ChannelPacketCreator.getInstance().hiredMerchantOwnerMaintenanceLeave());
                }

                if (!canPlaceStore(chr)) {    // thanks Ari for noticing player shops overlapping on opening time
                    return;
                }

                PlayerShop shop = chr.getPlayerShop();
                HiredMerchant merchant = chr.getHiredMerchant();
                if (shop != null && shop.isOwner(chr)) {
                    if (YamlConfig.config.server.USE_ERASE_PERMIT_ON_OPENSHOP) {
                        try {
                            InventoryManipulator.removeById(client, InventoryType.CASH, shop.getItemId(), 1, true, false);
                        } catch (RuntimeException re) {
                        } // fella does not have a player shop permit...
                    }

                    chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().updatePlayerShopBox(shop));
                    shop.setOpen(true);
                } else if (merchant != null && merchant.isOwner(chr)) {
                    chr.setHasMerchant(true);
                    merchant.setOpen(true);
                    chr.getMap().addMapObject(merchant);
                    chr.setHiredMerchant(null);
                    chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().spawnHiredMerchantBox(merchant));
                }
            } else if (mode == Action.READY.getCode()) {
                MiniGame game = chr.getMiniGame();
                game.broadcast(ChannelPacketCreator.getInstance().getMiniGameReady(game));
            } else if (mode == Action.UN_READY.getCode()) {
                MiniGame game = chr.getMiniGame();
                game.broadcast(ChannelPacketCreator.getInstance().getMiniGameUnReady(game));
            } else if (mode == Action.START.getCode()) {
                MiniGame game = chr.getMiniGame();
                if (game.getGameType().equals(MiniGameType.OMOK)) {
                    game.minigameMatchStarted();
                    game.broadcast(ChannelPacketCreator.getInstance().getMiniGameStart(game, game.getLoser()));
                    chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().addOmokBox(game.getOwner(), 2, 1));
                } else if (game.getGameType().equals(MiniGameType.MATCH_CARD)) {
                    game.minigameMatchStarted();
                    game.shuffleList();
                    game.broadcast(ChannelPacketCreator.getInstance().getMatchCardStart(game, game.getLoser()));
                    chr.getMap().broadcastMessage(ChannelPacketCreator.getInstance().addMatchCardBox(game.getOwner(), 2, 1));
                }
            } else if (mode == Action.GIVE_UP.getCode()) {
                MiniGame game = chr.getMiniGame();
                if (game.getGameType().equals(MiniGameType.OMOK)) {
                    if (game.isOwner(chr)) {
                        game.minigameMatchVisitorWins(true);
                    } else {
                        game.minigameMatchOwnerWins(true);
                    }
                } else if (game.getGameType().equals(MiniGameType.MATCH_CARD)) {
                    if (game.isOwner(chr)) {
                        game.minigameMatchVisitorWins(true);
                    } else {
                        game.minigameMatchOwnerWins(true);
                    }
                }
            } else if (mode == Action.REQUEST_TIE.getCode()) {
                MiniGame game = chr.getMiniGame();
                if (!game.isTieDenied(chr)) {
                    if (game.isOwner(chr)) {
                        game.broadcastToVisitor(ChannelPacketCreator.getInstance().getMiniGameRequestTie(game));
                    } else {
                        game.broadcastToOwner(ChannelPacketCreator.getInstance().getMiniGameRequestTie(game));
                    }
                }
            } else if (mode == Action.ANSWER_TIE.getCode()) {
                MiniGame game = chr.getMiniGame();
                if (packet.readByte() != 0) {
                    game.minigameMatchDraw();
                } else {
                    game.denyTie(chr);

                    if (game.isOwner(chr)) {
                        game.broadcastToVisitor(ChannelPacketCreator.getInstance().getMiniGameDenyTie(game));
                    } else {
                        game.broadcastToOwner(ChannelPacketCreator.getInstance().getMiniGameDenyTie(game));
                    }
                }
            } else if (mode == Action.SKIP.getCode()) {
                MiniGame game = chr.getMiniGame();
                if (game.isOwner(chr)) {
                    game.broadcast(ChannelPacketCreator.getInstance().getMiniGameSkipOwner(game));
                } else {
                    game.broadcast(ChannelPacketCreator.getInstance().getMiniGameSkipVisitor(game));
                }
            } else if (mode == Action.MOVE_OMOK.getCode()) {
                int x = packet.readInt(); // x point
                int y = packet.readInt(); // y point
                int type = packet.readByte(); // piece ( 1 or 2; Owner has one piece, visitor has another, it switches every game.)
                chr.getMiniGame().setPiece(x, y, type, chr);
            } else if (mode == Action.SELECT_CARD.getCode()) {
                int turn = packet.readByte(); // 1st turn = 1; 2nd turn = 0
                int slot = packet.readByte(); // slot
                MiniGame game = chr.getMiniGame();
                int firstslot = game.getFirstSlot();
                if (turn == 1) {
                    game.setFirstSlot(slot);
                    if (game.isOwner(chr)) {
                        game.broadcastToVisitor(ChannelPacketCreator.getInstance().getMatchCardSelect(game, turn, slot, firstslot, turn));
                    } else {
                        game.getOwner().sendPacket(ChannelPacketCreator.getInstance().getMatchCardSelect(game, turn, slot, firstslot, turn));
                    }
                } else if ((game.getCardId(firstslot)) == (game.getCardId(slot))) {
                    if (game.isOwner(chr)) {
                        game.broadcast(ChannelPacketCreator.getInstance().getMatchCardSelect(game, turn, slot, firstslot, 2));
                        game.setOwnerPoints();
                    } else {
                        game.broadcast(ChannelPacketCreator.getInstance().getMatchCardSelect(game, turn, slot, firstslot, 3));
                        game.setVisitorPoints();
                    }
                } else if (game.isOwner(chr)) {
                    game.broadcast(ChannelPacketCreator.getInstance().getMatchCardSelect(game, turn, slot, firstslot, 0));
                } else {
                    game.broadcast(ChannelPacketCreator.getInstance().getMatchCardSelect(game, turn, slot, firstslot, 1));
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
                    client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                if (item == null) {
                    client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "Invalid item description."));
                    client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                if (ii.isUnmerchable(item.getItemId())) {
                    if (ItemConstants.isPet(item.getItemId())) {
                        client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "Pets are not allowed to be traded."));
                    } else {
                        client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "Cash items are not allowed to be traded."));
                    }

                    client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                if (quantity < 1 || quantity > item.getQuantity()) {
                    client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "You don't have enough quantity of the item."));
                    client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                Trade trade = chr.getTrade();
                if (trade != null) {
                    if ((quantity <= item.getQuantity() && quantity >= 0) || ItemConstants.isRechargeable(item.getItemId())) {
                        if (ii.isDropRestricted(item.getItemId())) { // ensure that undroppable items do not make it to the trade window
                            if (!KarmaManipulator.hasKarmaFlag(item)) {
                                client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "That item is untradeable."));
                                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                                return;
                            }
                        }

                        Inventory inv = chr.getInventory(ivType);
                        inv.lockInventory();
                        try {
                            Item checkItem = chr.getInventory(ivType).getItem(pos);
                            if (checkItem != item || checkItem.getPosition() != item.getPosition()) {
                                client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "Invalid item description."));
                                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
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
                    client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "Could not perform shop operation with that item."));
                    client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return;
                } else if (ItemInformationProvider.getInstance().isUnmerchable(ivItem.getItemId())) {
                    if (ItemConstants.isPet(ivItem.getItemId())) {
                        client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "Pets are not allowed to be sold on the Player Store."));
                    } else {
                        client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "Cash items are not allowed to be sold on the Player Store."));
                    }

                    client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                short perBundle = packet.readShort();

                if (ItemConstants.isRechargeable(ivItem.getItemId())) {
                    perBundle = 1;
                    bundles = 1;
                } else if (ivItem.getQuantity() < (bundles * perBundle)) {     // thanks GabrielSin for finding a dupe here
                    client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "Could not perform shop operation with that item."));
                    client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
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

                PlayerShopItem shopItem = new PlayerShopItem(sellItem, bundles, price);
                PlayerShop shop = chr.getPlayerShop();
                HiredMerchant merchant = chr.getHiredMerchant();
                if (shop != null && shop.isOwner(chr)) {
                    if (shop.isOpen() || !shop.addItem(shopItem)) { // thanks Vcoc for pointing an exploit with unlimited shop slots
                        client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "You can't sell it anymore."));
                        return;
                    }

                    if (ItemConstants.isRechargeable(ivItem.getItemId())) {
                        InventoryManipulator.removeFromSlot(client, ivType, slot, ivItem.getQuantity(), true);
                    } else {
                        InventoryManipulator.removeFromSlot(client, ivType, slot, (short) (bundles * perBundle), true);
                    }

                    client.sendPacket(ChannelPacketCreator.getInstance().getPlayerShopItemUpdate(shop));
                } else if (merchant != null && merchant.isOwner(chr)) {
                    if (ivType.equals(InventoryType.CASH) && merchant.isPublished()) {
                        client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "Cash items are only allowed to be sold when first opening the store."));
                        return;
                    }

                    if (merchant.isOpen() || !merchant.addItem(shopItem)) { // thanks Vcoc for pointing an exploit with unlimited shop slots
                        client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "You can't sell it anymore."));
                        return;
                    }

                    if (ItemConstants.isRechargeable(ivItem.getItemId())) {
                        InventoryManipulator.removeFromSlot(client, ivType, slot, ivItem.getQuantity(), true);
                    } else {
                        InventoryManipulator.removeFromSlot(client, ivType, slot, (short) (bundles * perBundle), true);
                    }

                    client.sendPacket(ChannelPacketCreator.getInstance().updateHiredMerchant(merchant, chr));

                    if (YamlConfig.config.server.USE_ENFORCE_MERCHANT_SAVE) {
                        chr.saveCharToDB(false);
                    }

                    try {
                        merchant.saveItems(false);   // thanks Masterrulax for realizing yet another dupe with merchants/Fredrick
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "You can't sell without owning a shop."));
                }
            } else if (mode == Action.REMOVE_ITEM.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                PlayerShop shop = chr.getPlayerShop();
                if (shop != null && shop.isOwner(chr)) {
                    if (shop.isOpen()) {
                        client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "You can't take it with the store open."));
                        return;
                    }

                    int slot = packet.readShort();
                    if (slot >= shop.getItems().size() || slot < 0) {
                        AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + " tried to packet edit with a player shop.");
                        log.warn("Chr {} tried to remove item at slot {}", chr.getName(), slot);
                        client.disconnect(true, false);
                        return;
                    }

                    shop.takeItemBack(slot, chr);
                }
            } else if (mode == Action.MERCHANT_MESO.getCode()) {
                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null) {
                    return;
                }

                merchant.withdrawMesos(chr);

            } else if (mode == Action.VIEW_VISITORS.getCode()) {
                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null || !merchant.isOwner(chr)) {
                    return;
                }
                client.sendPacket(ChannelPacketCreator.getInstance().viewMerchantVisitorHistory(merchant.getVisitorHistory()));
            } else if (mode == Action.VIEW_BLACKLIST.getCode()) {
                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null || !merchant.isOwner(chr)) {
                    return;
                }

                client.sendPacket(ChannelPacketCreator.getInstance().viewMerchantBlacklist(merchant.getBlacklist()));
            } else if (mode == Action.ADD_TO_BLACKLIST.getCode()) {
                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null || !merchant.isOwner(chr)) {
                    return;
                }
                String chrName = packet.readString();
                merchant.addToBlacklist(chrName);
            } else if (mode == Action.REMOVE_FROM_BLACKLIST.getCode()) {
                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null || !merchant.isOwner(chr)) {
                    return;
                }
                String chrName = packet.readString();
                merchant.removeFromBlacklist(chrName);
            } else if (mode == Action.MERCHANT_ORGANIZE.getCode()) {
                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant == null || !merchant.isOwner(chr)) {
                    return;
                }

                merchant.withdrawMesos(chr);
                merchant.clearInexistentItems();

                if (merchant.getItems().isEmpty()) {
                    merchant.closeOwnerMerchant(chr);
                    return;
                }
                client.sendPacket(ChannelPacketCreator.getInstance().updateHiredMerchant(merchant, chr));

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
                    return;
                }
                PlayerShop shop = chr.getPlayerShop();
                HiredMerchant merchant = chr.getHiredMerchant();
                if (shop != null && shop.isVisitor(chr)) {
                    if (shop.buy(client, itemid, quantity)) {
                        shop.broadcast(ChannelPacketCreator.getInstance().getPlayerShopItemUpdate(shop));
                    }
                } else if (merchant != null && !merchant.isOwner(chr)) {
                    merchant.buy(client, itemid, quantity);
                    merchant.broadcastToVisitorsThreadsafe(ChannelPacketCreator.getInstance().updateHiredMerchant(merchant, chr));
                }
            } else if (mode == Action.TAKE_ITEM_BACK.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant != null && merchant.isOwner(chr)) {
                    if (merchant.isOpen()) {
                        client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "You can't take it with the store open."));
                        return;
                    }

                    int slot = packet.readShort();
                    if (slot >= merchant.getItems().size() || slot < 0) {
                        AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + " tried to packet edit with a hired merchant.");
                        log.warn("Chr {} tried to remove item at slot {}", chr.getName(), slot);
                        client.disconnect(true, false);
                        return;
                    }

                    merchant.takeItemBack(slot, chr);
                }
            } else if (mode == Action.CLOSE_MERCHANT.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant != null) {
                    merchant.closeOwnerMerchant(chr);
                }
            } else if (mode == Action.MAINTENANCE_OFF.getCode()) {
                if (isTradeOpen(chr)) {
                    return;
                }

                HiredMerchant merchant = chr.getHiredMerchant();
                if (merchant != null) {
                    if (merchant.isOwner(chr)) {
                        if (merchant.getItems().isEmpty()) {
                            merchant.closeOwnerMerchant(chr);
                        } else {
                            merchant.clearMessages();
                            merchant.setOpen(true);
                            merchant.getMap().broadcastMessage(ChannelPacketCreator.getInstance().updateHiredMerchantBox(merchant));
                        }
                    }
                }

                chr.setHiredMerchant(null);
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            } else if (mode == Action.BAN_PLAYER.getCode()) {
                packet.skip(1);

                PlayerShop shop = chr.getPlayerShop();
                if (shop != null && shop.isOwner(chr)) {
                    shop.banPlayer(packet.readString());
                }
            } else if (mode == Action.EXPEL.getCode()) {
                MiniGame miniGame = chr.getMiniGame();
                if (miniGame != null && miniGame.isOwner(chr)) {
                    Character visitor = miniGame.getVisitor();

                    if (visitor != null) {
                        visitor.closeMiniGame(false);
                        visitor.sendPacket(ChannelPacketCreator.getInstance().getMiniGameClose(true, 5));
                    }
                }
            } else if (mode == Action.EXIT_AFTER_GAME.getCode()) {
                MiniGame miniGame = chr.getMiniGame();
                if (miniGame != null) {
                    miniGame.setQuitAfterGame(chr, true);
                }
            } else if (mode == Action.CANCEL_EXIT_AFTER_GAME.getCode()) {
                MiniGame miniGame = chr.getMiniGame();
                if (miniGame != null) {
                    miniGame.setQuitAfterGame(chr, false);
                }
            }
        } finally {
            client.releaseClient();
        }
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
            for (MapObject mmo : chr.getMap().getMapObjectsInRange(chr.getPosition(), 23000, Arrays.asList(MapObjectType.HIRED_MERCHANT, MapObjectType.PLAYER))) {
                if (mmo instanceof Character mc) {
                    if (mc.getId() == chr.getId()) {
                        continue;
                    }

                    PlayerShop shop = mc.getPlayerShop();
                    if (shop != null && shop.isOwner(mc)) {
                        chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(13));
                        return false;
                    }
                } else {
                    chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(13));
                    return false;
                }
            }

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
}
