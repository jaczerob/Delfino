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

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.inventory.Inventory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.server.life.LifeFactory;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.server.maps.MapObject;
import dev.jaczerob.delfino.maplestory.server.maps.MapObjectType;
import dev.jaczerob.delfino.maplestory.server.quest.Quest;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.Randomizer;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public final class AdminCommandHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(AdminCommandHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.ADMIN_COMMAND;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (!client.getPlayer().isGM()) {
            return;
        }
        byte mode = packet.readByte();
        String victim;
        Character target;
        switch (mode) {
            case 0x00: // Level1~Level8 & Package1~Package2
                int[][] toSpawn = ItemInformationProvider.getInstance().getSummonMobs(packet.readInt());
                for (int[] toSpawnChild : toSpawn) {
                    if (Randomizer.nextInt(100) < toSpawnChild[1]) {
                        client.getPlayer().getMap().spawnMonsterOnGroundBelow(LifeFactory.getMonster(toSpawnChild[0]), client.getPlayer().getPosition());
                    }
                }
                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                break;
            case 0x01: { // /d (inv)
                byte type = packet.readByte();
                Inventory in = client.getPlayer().getInventory(InventoryType.getByType(type));
                for (short i = 1; i <= in.getSlotLimit(); i++) { //TODO What is the point of this loop?
                    if (in.getItem(i) != null) {
                        InventoryManipulator.removeFromSlot(client, InventoryType.getByType(type), i, in.getItem(i).getQuantity(), false);
                    }
                    return;
                }
                break;
            }
            case 0x02: // Exp
                client.getPlayer().setExp(packet.readInt());
                break;
            case 0x03: // /ban <name>
                client.getPlayer().yellowMessage("Please use !ban <IGN> <Reason>");
                break;
            case 0x04: // /block <name> <duration (in days)> <HACK/BOT/AD/HARASS/CURSE/SCAM/MISCONDUCT/SELL/ICASH/TEMP/GM/IPROGRAM/MEGAPHONE>
                victim = packet.readString();
                int type = packet.readByte(); //reason
                int duration = packet.readInt();
                String description = packet.readString();
                String reason = client.getPlayer().getName() + " used /ban to ban";
                target = client.getChannelServer().getPlayerStorage().getCharacterByName(victim);
                if (target != null) {
                    String readableTargetName = Character.makeMapleReadable(target.getName());
                    String ip = target.getClient().getRemoteAddress();
                    reason += readableTargetName + " (IP: " + ip + ")";
                    if (duration == -1) {
                        target.ban(description + " " + reason);
                    } else {
                        target.block(type, duration, description);
                        target.sendPolice(duration, reason, 6000);
                    }
                    context.writeAndFlush(ChannelPacketCreator.getInstance().getGMEffect(4, (byte) 0));
                } else if (Character.ban(victim, reason, false)) {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().getGMEffect(4, (byte) 0));
                } else {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().getGMEffect(6, (byte) 1));
                }
                break;
            case 0x10: // /h, information added by vana -- <and tele mode f1> ... hide ofcourse
                client.getPlayer().Hide(packet.readByte() == 1);
                break;
            case 0x11: // Entering a map
                switch (packet.readByte()) {
                    case 0:// /u
                        StringBuilder sb = new StringBuilder("USERS ON THIS MAP: ");
                        for (Character mc : client.getPlayer().getMap().getCharacters()) {
                            sb.append(mc.getName());
                            sb.append(" ");
                        }
                        client.getPlayer().message(sb.toString());
                        break;
                    case 12:// /uclip and entering a map
                        break;
                }
                break;
            case 0x12: // Send
                victim = packet.readString();
                int mapId = packet.readInt();
                client.getChannelServer().getPlayerStorage().getCharacterByName(victim).changeMap(client.getChannelServer().getMapFactory().getMap(mapId));
                break;
            case 0x15: // Kill
                int mobToKill = packet.readInt();
                int amount = packet.readInt();
                List<MapObject> monsterx = client.getPlayer().getMap().getMapObjectsInRange(client.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapObjectType.MONSTER));
                for (int x = 0; x < amount; x++) {
                    Monster monster = (Monster) monsterx.get(x);
                    if (monster.getId() == mobToKill) {
                        client.getPlayer().getMap().killMonster(monster, client.getPlayer(), true, (short) 0);
                    }
                }
                break;
            case 0x16: // Questreset
                Quest.getInstance(packet.readShort()).reset(client.getPlayer());
                break;
            case 0x17: // Summon
                int mobId = packet.readInt();
                int quantity = packet.readInt();
                for (int i = 0; i < quantity; i++) {
                    client.getPlayer().getMap().spawnMonsterOnGroundBelow(LifeFactory.getMonster(mobId), client.getPlayer().getPosition());
                }
                break;
            case 0x18: // Maple & Mobhp
                int mobHp = packet.readInt();
                client.getPlayer().dropMessage("Monsters HP");
                List<MapObject> monsters = client.getPlayer().getMap().getMapObjectsInRange(client.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapObjectType.MONSTER));
                for (MapObject mobs : monsters) {
                    Monster monster = (Monster) mobs;
                    if (monster.getId() == mobHp) {
                        client.getPlayer().dropMessage(monster.getName() + ": " + monster.getHp());
                    }
                }
                break;
            case 0x1E: // Warn
                victim = packet.readString();
                String message = packet.readString();
                target = client.getChannelServer().getPlayerStorage().getCharacterByName(victim);
                if (target != null) {
                    target.getClient().sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, message));
                    context.writeAndFlush(ChannelPacketCreator.getInstance().getGMEffect(0x1E, (byte) 1));
                } else {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().getGMEffect(0x1E, (byte) 0));
                }
                break;
            case 0x24:// /Artifact Ranking
                break;
            case 0x77: //Testing purpose
                if (packet.available() == 4) {
                    log.debug("int: {}", packet.readInt());
                } else if (packet.available() == 2) {
                    log.debug("short: {}", packet.readShort());
                }
                break;
            default:
                log.info("New GM packet encountered (MODE: {}): {}", mode, packet);
                break;
        }
    }
}
