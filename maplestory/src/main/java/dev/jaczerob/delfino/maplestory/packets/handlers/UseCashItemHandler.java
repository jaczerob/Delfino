/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.*;
import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.creator.veteran.*;
import dev.jaczerob.delfino.maplestory.client.inventory.*;
import dev.jaczerob.delfino.maplestory.client.inventory.Equip.ScrollResult;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.KarmaManipulator;
import dev.jaczerob.delfino.maplestory.client.processor.npc.DueyProcessor;
import dev.jaczerob.delfino.maplestory.client.processor.stat.AssignAPProcessor;
import dev.jaczerob.delfino.maplestory.client.processor.stat.AssignSPProcessor;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.constants.id.MapId;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.server.Shop;
import dev.jaczerob.delfino.maplestory.server.ShopFactory;
import dev.jaczerob.delfino.maplestory.server.TimerManager;
import dev.jaczerob.delfino.maplestory.server.maps.*;
import dev.jaczerob.delfino.maplestory.service.NoteService;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.Pair;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import dev.jaczerob.delfino.network.packets.out.SendNoteSuccessPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public final class UseCashItemHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(UseCashItemHandler.class);

    private final NoteService noteService;

    public UseCashItemHandler(NoteService noteService) {
        this.noteService = noteService;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.USE_CASH_ITEM;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        final Character player = client.getPlayer();

        long timeNow = currentServerTime();
        if (timeNow - player.getLastUsedCashItem() < 3000) {
            player.dropMessage(1, "You have used a cash item recently. Wait a moment, then try again.");
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }
        player.setLastUsedCashItem(timeNow);

        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        short position = packet.readShort();
        int itemId = packet.readInt();
        int itemType = itemId / 10000;

        Inventory cashInv = player.getInventory(InventoryType.CASH);
        Item toUse = cashInv.getItem(position);
        if (toUse == null || toUse.getItemId() != itemId) {
            toUse = cashInv.findById(itemId);

            if (toUse == null) {
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                return;
            }

            position = toUse.getPosition();
        }

        if (toUse.getQuantity() < 1) {
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        String medal = "";
        Item medalItem = player.getInventory(InventoryType.EQUIPPED).getItem((short) -49);
        if (medalItem != null) {
            medal = "<" + ii.getName(medalItem.getItemId()) + "> ";
        }

        if (itemType == 504) { // vip teleport rock
            String error1 = "Either the player could not be found or you were trying to teleport to an illegal location.";
            boolean vip = packet.readByte() == 1 && itemId / 1000 >= 5041;
            remove(client, position, itemId);
            boolean success = false;
            if (!vip) {
                int mapId = packet.readInt();
                if (itemId / 1000 >= 5041 || mapId / 100000000 == player.getMapId() / 100000000) { //check vip or same continent
                    MapleMap targetMap = client.getChannelServer().getMapFactory().getMap(mapId);
                    if (!FieldLimit.CANNOTVIPROCK.check(targetMap.getFieldLimit()) && (targetMap.getForcedReturnId() == MapId.NONE || MapId.isMapleIsland(mapId))) {
                        player.forceChangeMap(targetMap, targetMap.getRandomPlayerSpawnpoint());
                        success = true;
                    } else {
                        player.dropMessage(1, error1);
                    }
                } else {
                    player.dropMessage(1, "You cannot teleport between continents with this teleport rock.");
                }
            } else {
                String name = packet.readString();
                Character victim = client.getChannelServer().getPlayerStorage().getCharacterByName(name);

                if (victim != null) {
                    MapleMap targetMap = victim.getMap();
                    if (!FieldLimit.CANNOTVIPROCK.check(targetMap.getFieldLimit()) && (targetMap.getForcedReturnId() == MapId.NONE || MapId.isMapleIsland(targetMap.getId()))) {
                        if (!victim.isGM() || victim.gmLevel() <= player.gmLevel()) {   // thanks Yoboes for noticing non-GM's being unreachable through rocks
                            player.forceChangeMap(targetMap, targetMap.findClosestPlayerSpawnpoint(victim.getPosition()));
                            success = true;
                        } else {
                            player.dropMessage(1, error1);
                        }
                    } else {
                        player.dropMessage(1, "You cannot teleport to this map.");
                    }
                } else {
                    player.dropMessage(1, "Player could not be found in this channel.");
                }
            }

            if (!success) {
                InventoryManipulator.addById(client, itemId, (short) 1);
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            }
        } else if (itemType == 505) { // AP/SP reset
            if (!player.isAlive()) {
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                return;
            }

            if (itemId > ItemId.AP_RESET) {
                int SPTo = packet.readInt();
                if (!AssignSPProcessor.canSPAssign(client, SPTo)) {  // exploit found thanks to Arnah
                    return;
                }

                int SPFrom = packet.readInt();
                Skill skillSPTo = SkillFactory.getSkill(SPTo);
                Skill skillSPFrom = SkillFactory.getSkill(SPFrom);
                byte curLevel = player.getSkillLevel(skillSPTo);
                byte curLevelSPFrom = player.getSkillLevel(skillSPFrom);
                if ((curLevel < skillSPTo.getMaxLevel()) && curLevelSPFrom > 0) {
                    player.changeSkillLevel(skillSPFrom, (byte) (curLevelSPFrom - 1), player.getMasterLevel(skillSPFrom), -1);
                    player.changeSkillLevel(skillSPTo, (byte) (curLevel + 1), player.getMasterLevel(skillSPTo), -1);

                    // update macros, thanks to Arnah
                    if ((curLevelSPFrom - 1) == 0) {
                        boolean updated = false;
                        for (SkillMacro macro : player.getMacros()) {
                            if (macro == null) {
                                continue;
                            }

                            boolean update = false;// cleaner?
                            if (macro.getSkill1() == SPFrom) {
                                update = true;
                                macro.setSkill1(0);
                            }
                            if (macro.getSkill2() == SPFrom) {
                                update = true;
                                macro.setSkill2(0);
                            }
                            if (macro.getSkill3() == SPFrom) {
                                update = true;
                                macro.setSkill3(0);
                            }
                            if (update) {
                                updated = true;
                                player.updateMacros(macro.getPosition(), macro);
                            }
                        }
                        if (updated) {
                            player.sendMacros();
                        }
                    }
                }
            } else {
                int APTo = packet.readInt();
                int APFrom = packet.readInt();

                if (!AssignAPProcessor.APResetAction(client, APFrom, APTo)) {
                    return;
                }
            }
            remove(client, position, itemId);
        } else if (itemType == 506) {
            Item eq = null;
            if (itemId == 5060000) { // Item tag.
                int equipSlot = packet.readShort();
                if (equipSlot == 0) {
                    return;
                }
                eq = player.getInventory(InventoryType.EQUIPPED).getItem((short) equipSlot);
                eq.setOwner(player.getName());
            } else if (itemId == 5060001 || itemId == 5061000 || itemId == 5061001 || itemId == 5061002 || itemId == 5061003) { // Sealing lock
                InventoryType type = InventoryType.getByType((byte) packet.readInt());
                eq = player.getInventory(type).getItem((short) packet.readInt());
                if (eq == null) { //Check if the type is EQUIPMENT?
                    return;
                }
                short flag = eq.getFlag();
                if (eq.getExpiration() > -1 && (eq.getFlag() & ItemConstants.LOCK) != ItemConstants.LOCK) {
                    return; //No perma items pls
                }
                flag |= ItemConstants.LOCK;
                eq.setFlag(flag);

                long period = 0;
                if (itemId == 5061000) {
                    period = 7;
                } else if (itemId == 5061001) {
                    period = 30;
                } else if (itemId == 5061002) {
                    period = 90;
                } else if (itemId == 5061003) {
                    period = 365;
                }

                if (period > 0) {
                    long expiration = eq.getExpiration() > -1 ? eq.getExpiration() : currentServerTime();
                    eq.setExpiration(expiration + DAYS.toMillis(period));
                }

                // double-remove found thanks to BHB
            } else if (itemId == 5060002) { // Incubator
                byte inventory2 = (byte) packet.readInt();
                short slot2 = (short) packet.readInt();
                Item item2 = player.getInventory(InventoryType.getByType(inventory2)).getItem(slot2);
                if (item2 == null) // hacking
                {
                    return;
                }
                if (getIncubatedItem(client, itemId)) {
                    InventoryManipulator.removeFromSlot(client, InventoryType.getByType(inventory2), slot2, (short) 1, false);
                    remove(client, position, itemId);
                }
                return;
            }
            packet.readInt(); // time stamp
            if (eq != null) {
                player.forceUpdateItem(eq);
                remove(client, position, itemId);
            }
        } else if (itemType == 507) {
            boolean whisper;
            switch ((itemId / 1000) % 10) {
                case 1: // Megaphone
                    if (player.getLevel() > 9) {
                        player.getClient().getChannelServer().broadcastPacket(ChannelPacketCreator.getInstance().serverNotice(2, medal + player.getName() + " : " + packet.readString()));
                    } else {
                        player.dropMessage(1, "You may not use this until you're level 10.");
                        return;
                    }
                    break;
                case 2: // Super megaphone
                    Server.getInstance().broadcastMessage(client.getWorld(), ChannelPacketCreator.getInstance().serverNotice(3, client.getChannel(), medal + player.getName() + " : " + packet.readString(), (packet.readByte() != 0)));
                    break;
                case 5: // Maple TV
                    int tvType = itemId % 10;
                    boolean megassenger = false;
                    boolean ear = false;
                    Character victim = null;
                    if (tvType != 1) {
                        if (tvType >= 3) {
                            megassenger = true;
                            if (tvType == 3) {
                                packet.readByte();
                            }
                            ear = 1 == packet.readByte();
                        } else if (tvType != 2) {
                            packet.readByte();
                        }
                        if (tvType != 4) {
                            victim = client.getChannelServer().getPlayerStorage().getCharacterByName(packet.readString());
                        }
                    }
                    List<String> messages = new LinkedList<>();
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < 5; i++) {
                        String message = packet.readString();
                        if (megassenger) {
                            builder.append(" ").append(message);
                        }
                        messages.add(message);
                    }
                    packet.readInt();

                    if (!MapleTVEffect.broadcastMapleTVIfNotActive(player, victim, messages, tvType)) {
                        player.dropMessage(1, "MapleTV is already in use.");
                        return;
                    }

                    if (megassenger) {
                        Server.getInstance().broadcastMessage(client.getWorld(), ChannelPacketCreator.getInstance().serverNotice(3, client.getChannel(), medal + player.getName() + " : " + builder, ear));
                    }

                    break;
                case 6: //item megaphone
                    String msg = medal + player.getName() + " : " + packet.readString();
                    whisper = packet.readByte() == 1;
                    Item item = null;
                    if (packet.readByte() == 1) { //item
                        item = player.getInventory(InventoryType.getByType((byte) packet.readInt())).getItem((short) packet.readInt());
                        if (item == null) //hack
                        {
                            return;
                        }

                        // thanks Conrad for noticing that untradeable items should be allowed in megas
                    }
                    Server.getInstance().broadcastMessage(client.getWorld(), ChannelPacketCreator.getInstance().itemMegaphone(msg, whisper, client.getChannel(), item));
                    break;
                case 7: //triple megaphone
                    int lines = packet.readByte();
                    if (lines < 1 || lines > 3) //hack
                    {
                        return;
                    }
                    String[] msg2 = new String[lines];
                    for (int i = 0; i < lines; i++) {
                        msg2[i] = medal + player.getName() + " : " + packet.readString();
                    }
                    whisper = packet.readByte() == 1;
                    Server.getInstance().broadcastMessage(client.getWorld(), ChannelPacketCreator.getInstance().getMultiMegaphone(msg2, client.getChannel(), whisper));
                    break;
            }
            remove(client, position, itemId);
        } else if (itemType == 508) {   // thanks tmskdl12 for graduation banner; thanks ratency for first pointing lack of Kite handling
            Kite kite = new Kite(player, packet.readString(), itemId);

            if (!GameConstants.isFreeMarketRoom(player.getMapId())) {
                player.getMap().spawnKite(kite);
                remove(client, position, itemId);
            } else {
                client.sendPacket(ChannelPacketCreator.getInstance().sendCannotSpawnKite());
            }
        } else if (itemType == 509) {
            String sendTo = packet.readString();
            String msg = packet.readString();
            boolean sendSuccess = noteService.sendNormal(msg, player.getName(), sendTo);
            if (sendSuccess) {
                remove(client, position, itemId);
                client.sendPacket(new SendNoteSuccessPacket());
            }
        } else if (itemType == 510) {
            player.getMap().broadcastMessage(ChannelPacketCreator.getInstance().musicChange("Jukebox/Congratulation"));
            remove(client, position, itemId);
        } else if (itemType == 512) {
            if (ii.getStateChangeItem(itemId) != 0) {
                for (Character mChar : player.getMap().getCharacters()) {
                    ii.getItemEffect(ii.getStateChangeItem(itemId)).applyTo(mChar);
                }
            }
            player.getMap().startMapEffect(ii.getMsg(itemId).replaceFirst("%s", player.getName()).replaceFirst("%s", packet.readString()), itemId);
            remove(client, position, itemId);
        } else if (itemType == 517) {
            Pet pet = player.getPet(0);
            if (pet == null) {
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                return;
            }
            String newName = packet.readString();
            pet.setName(newName);
            pet.saveToDb();

            Item item = player.getInventory(InventoryType.CASH).getItem(pet.getPosition());
            if (item != null) {
                player.forceUpdateItem(item);
            }

            player.getMap().broadcastMessage(player, ChannelPacketCreator.getInstance().changePetName(player, newName, 1), true);
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            remove(client, position, itemId);
        } else if (itemType == 520) {
            player.gainMeso(ii.getMeso(itemId), true, false, true);
            remove(client, position, itemId);
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
        } else if (itemType == 523) {
            int itemid = packet.readInt();

            if (!YamlConfig.config.server.USE_ENFORCE_ITEM_SUGGESTION) {
                client.getWorldServer().addOwlItemSearch(itemid);
            }
            player.setOwlSearch(itemid);
            List<Pair<PlayerShopItem, AbstractMapObject>> hmsAvailable = client.getWorldServer().getAvailableItemBundles(itemid);
            if (!hmsAvailable.isEmpty()) {
                remove(client, position, itemId);
            }

            client.sendPacket(ChannelPacketCreator.getInstance().owlOfMinerva(client, itemid, hmsAvailable));
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());

        } else if (itemType == 524) {
            for (byte i = 0; i < 3; i++) {
                Pet pet = player.getPet(i);
                if (pet != null) {
                    Pair<Integer, Boolean> pair = pet.canConsume(itemId);

                    if (pair.getRight()) {
                        pet.gainTamenessFullness(player, pair.getLeft(), 100, 1, true);
                        remove(client, position, itemId);
                        break;
                    }
                } else {
                    break;
                }
            }
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
        } else if (itemType == 530) {
            ii.getItemEffect(itemId).applyTo(player);
            remove(client, position, itemId);
        } else if (itemType == 533) {
            DueyProcessor.dueySendTalk(client, true);
        } else if (itemType == 537) {
            if (GameConstants.isFreeMarketRoom(player.getMapId())) {
                player.dropMessage(5, "You cannot use the chalkboard here.");
                player.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                return;
            }

            player.setChalkboard(packet.readString());
            player.getMap().broadcastMessage(ChannelPacketCreator.getInstance().useChalkboard(player, false));
            player.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            //remove(client, position, itemId);  thanks Conrad for noticing chalkboards shouldn't be depleted upon use
        } else if (itemType == 539) {
            List<String> strLines = new LinkedList<>();
            for (int i = 0; i < 4; i++) {
                strLines.add(packet.readString());
            }

            final int world = client.getWorld();
            Server.getInstance().broadcastMessage(world, ChannelPacketCreator.getInstance().getAvatarMega(player, medal, client.getChannel(), itemId, strLines, (packet.readByte() != 0)));
            TimerManager.getInstance().schedule(() -> Server.getInstance().broadcastMessage(world, ChannelPacketCreator.getInstance().byeAvatarMega()), SECONDS.toMillis(10));
            remove(client, position, itemId);
        } else if (itemType == 540) {
            packet.readByte();
            packet.readInt();
            if (itemId == ItemId.NAME_CHANGE) {
                client.sendPacket(ChannelPacketCreator.getInstance().showNameChangeCancel(player.cancelPendingNameChange()));
            } else if (itemId == ItemId.WORLD_TRANSFER) {
                client.sendPacket(ChannelPacketCreator.getInstance().showWorldTransferCancel(player.cancelPendingWorldTranfer()));
            }
            remove(client, position, itemId);
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
        } else if (itemType == 543) {
            if (itemId == ItemId.MAPLE_LIFE_B && !client.gainCharacterSlot()) {
                player.dropMessage(1, "You have already used up all 12 extra character slots.");
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                return;
            }

            String name = packet.readString();
            int face = packet.readInt();
            int hair = packet.readInt();
            int haircolor = packet.readInt();
            int skin = packet.readInt();
            int gender = packet.readInt();
            int jobid = packet.readInt();
            int improveSp = packet.readInt();

            int createStatus = switch (jobid) {
                case 0 -> WarriorCreator.createCharacter(client, name, face, hair + haircolor, skin, gender, improveSp);
                case 1 ->
                        MagicianCreator.createCharacter(client, name, face, hair + haircolor, skin, gender, improveSp);
                case 2 -> BowmanCreator.createCharacter(client, name, face, hair + haircolor, skin, gender, improveSp);
                case 3 -> ThiefCreator.createCharacter(client, name, face, hair + haircolor, skin, gender, improveSp);
                default -> PirateCreator.createCharacter(client, name, face, hair + haircolor, skin, gender, improveSp);
            };

            if (createStatus == 0) {
                client.sendPacket(ChannelPacketCreator.getInstance().sendMapleLifeError(0));   // success!

                player.showHint("#bSuccess#k on creation of the new character through the Maple Life card.");
                remove(client, position, itemId);
            } else {
                if (createStatus == -1) {    // check name
                    client.sendPacket(ChannelPacketCreator.getInstance().sendMapleLifeNameError());
                } else {
                    client.sendPacket(ChannelPacketCreator.getInstance().sendMapleLifeError(-1 * createStatus));
                }
            }
        } else if (itemType == 545) { // MiuMiu's travel store
            if (player.getShop() == null) {
                Shop shop = ShopFactory.getInstance().getShop(1338);
                if (shop != null) {
                    shop.sendShop(client);
                    remove(client, position, itemId);
                }
            } else {
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            }
        } else if (itemType == 550) { //Extend item expiration
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
        } else if (itemType == 552) {
            InventoryType type = InventoryType.getByType((byte) packet.readInt());
            short slot = (short) packet.readInt();
            Item item = player.getInventory(type).getItem(slot);
            if (item == null || item.getQuantity() <= 0 || KarmaManipulator.hasKarmaFlag(item) || !ii.isKarmaAble(item.getItemId())) {
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                return;
            }

            KarmaManipulator.setKarmaFlag(item);
            player.forceUpdateItem(item);
            remove(client, position, itemId);
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
        } else if (itemType == 552) { //DS EGG THING
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
        } else if (itemType == 557) {
            packet.readInt();
            int itemSlot = packet.readInt();
            packet.readInt();
            final Equip equip = (Equip) player.getInventory(InventoryType.EQUIP).getItem((short) itemSlot);
            if (equip.getVicious() >= 2 || player.getInventory(InventoryType.CASH).findById(ItemId.VICIOUS_HAMMER) == null) {
                return;
            }
            equip.setVicious(equip.getVicious() + 1);
            equip.setUpgradeSlots(equip.getUpgradeSlots() + 1);
            remove(client, position, itemId);
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            client.sendPacket(ChannelPacketCreator.getInstance().sendHammerData(equip.getVicious()));
            player.forceUpdateItem(equip);
        } else if (itemType == 561) { //VEGA'S SPELL
            if (packet.readInt() != 1) {
                return;
            }

            final byte eSlot = (byte) packet.readInt();
            final Item eitem = player.getInventory(InventoryType.EQUIP).getItem(eSlot);

            if (packet.readInt() != 2) {
                return;
            }

            final byte uSlot = (byte) packet.readInt();
            final Item uitem = player.getInventory(InventoryType.USE).getItem(uSlot);
            if (eitem == null || uitem == null) {
                return;
            }

            Equip toScroll = (Equip) eitem;
            if (toScroll.getUpgradeSlots() < 1) {
                client.sendPacket(ChannelPacketCreator.getInstance().getInventoryFull());
                return;
            }

            //should have a check here against PE hacks
            if (itemId / 1000000 != 5) {
                itemId = 0;
            }

            player.toggleBlockCashShop();

            final int curlevel = toScroll.getLevel();
            client.sendPacket(ChannelPacketCreator.getInstance().sendVegaScroll(0x40));

            final Equip scrolled = (Equip) ii.scrollEquipWithId(toScroll, uitem.getItemId(), false, itemId, player.isGM());
            client.sendPacket(ChannelPacketCreator.getInstance().sendVegaScroll(scrolled.getLevel() > curlevel ? 0x41 : 0x43));
            //opcodes 0x42, 0x44: "this item cannot be used"; 0x39, 0x45: crashes

            InventoryManipulator.removeFromSlot(client, InventoryType.USE, uSlot, (short) 1, false);
            remove(client, position, itemId);

            TimerManager.getInstance().schedule(() -> {
                if (!player.isLoggedin()) {
                    return;
                }

                player.toggleBlockCashShop();

                final List<ModifyInventory> mods = new ArrayList<>();
                mods.add(new ModifyInventory(3, scrolled));
                mods.add(new ModifyInventory(0, scrolled));
                client.sendPacket(ChannelPacketCreator.getInstance().modifyInventory(true, mods));

                ScrollResult scrollResult = scrolled.getLevel() > curlevel ? ScrollResult.SUCCESS : ScrollResult.FAIL;
                player.getMap().broadcastMessage(ChannelPacketCreator.getInstance().getScrollEffect(player.getId(), scrollResult, false, false));
                if (eSlot < 0 && (scrollResult == ScrollResult.SUCCESS)) {
                    player.equipChanged();
                }

                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            }, SECONDS.toMillis(3));
        } else {
            log.warn("NEW CASH ITEM TYPE: {}, packet: {}", itemType, packet);
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
        }
    }

    private static void remove(Client client, short position, int itemid) {
        Inventory cashInv = client.getPlayer().getInventory(InventoryType.CASH);
        cashInv.lockInventory();
        try {
            Item it = cashInv.getItem(position);
            if (it == null || it.getItemId() != itemid) {
                it = cashInv.findById(itemid);
                if (it != null) {
                    position = it.getPosition();
                }
            }

            InventoryManipulator.removeFromSlot(client, InventoryType.CASH, position, (short) 1, true, false);
        } finally {
            cashInv.unlockInventory();
        }
    }

    private static boolean getIncubatedItem(Client client, int id) {
        final int[] ids = {1012070, 1302049, 1302063, 1322027, 2000004, 2000005, 2020013, 2020015, 2040307, 2040509, 2040519, 2040521, 2040533, 2040715, 2040717, 2040810, 2040811, 2070005, 2070006, 4020009,};
        final int[] quantitys = {1, 1, 1, 1, 240, 200, 200, 200, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3};
        int amount = 0;
        for (int i = 0; i < ids.length; i++) {
            if (i == id) {
                amount = quantitys[i];
            }
        }
        if (client.getPlayer().getInventory(InventoryType.getByType((byte) (id / 1000000))).isFull()) {
            return false;
        }
        InventoryManipulator.addById(client, id, (short) amount);
        return true;
    }
}
