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
import dev.jaczerob.delfino.maplestory.client.Disease;
import dev.jaczerob.delfino.maplestory.net.server.world.Party;
import dev.jaczerob.delfino.maplestory.net.server.world.PartyCharacter;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.life.LifeFactory;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillType;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.server.partyquest.CarnivalFactory;
import dev.jaczerob.delfino.maplestory.server.partyquest.CarnivalFactory.MCSkill;
import dev.jaczerob.delfino.maplestory.server.partyquest.MonsterCarnival;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.Pair;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;


/**
 * @author Drago (Dragohe4rt)
 */

@Component
public final class MonsterCarnivalHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.MONSTER_CARNIVAL;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (client.tryacquireClient()) {
            try {
                try {
                    int tab = packet.readByte();
                    int num = packet.readByte();
                    int neededCP = 0;
                    if (tab == 0) {
                        final List<Pair<Integer, Integer>> mobs = client.getPlayer().getMap().getMobsToSpawn();
                        if (num >= mobs.size() || client.getPlayer().getCP() < mobs.get(num).right) {
                            context.writeAndFlush(ChannelPacketCreator.getInstance().CPQMessage((byte) 1));
                            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                            return;
                        }

                        final Monster mob = LifeFactory.getMonster(mobs.get(num).left);
                        MonsterCarnival mcpq = client.getPlayer().getMonsterCarnival();
                        if (mcpq != null) {
                            if (!mcpq.canSummonR() && client.getPlayer().getTeam() == 0 || !mcpq.canSummonB() && client.getPlayer().getTeam() == 1) {
                                context.writeAndFlush(ChannelPacketCreator.getInstance().CPQMessage((byte) 2));
                                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                                return;
                            }

                            if (client.getPlayer().getTeam() == 0) {
                                mcpq.summonR();
                            } else {
                                mcpq.summonB();
                            }

                            Point spawnPos = client.getPlayer().getMap().getRandomSP(client.getPlayer().getTeam());
                            mob.setPosition(spawnPos);

                            client.getPlayer().getMap().addMonsterSpawn(mob, 1, client.getPlayer().getTeam());
                            client.getPlayer().getMap().addAllMonsterSpawn(mob, 1, client.getPlayer().getTeam());
                            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                        }

                        neededCP = mobs.get(num).right;
                    } else if (tab == 1) { //debuffs
                        final List<Integer> skillid = client.getPlayer().getMap().getSkillIds();
                        if (num >= skillid.size()) {
                            client.getPlayer().dropMessage(5, "An unexpected error has occurred.");
                            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                            return;
                        }
                        final MCSkill skill = CarnivalFactory.getInstance().getSkill(skillid.get(num)); //ugh wtf
                        if (skill == null || client.getPlayer().getCP() < skill.cpLoss()) {
                            context.writeAndFlush(ChannelPacketCreator.getInstance().CPQMessage((byte) 1));
                            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                            return;
                        }
                        final Disease dis = skill.getDisease();
                        Party enemies = client.getPlayer().getParty().getEnemy();
                        if (skill.targetsAll()) {
                            int hitChance = rollHitChance(dis.getMobSkillType());
                            if (hitChance <= 80) {
                                for (PartyCharacter mpc : enemies.getPartyMembers()) {
                                    Character mc = mpc.getPlayer();
                                    if (mc != null) {
                                        if (dis == null) {
                                            mc.dispel();
                                        } else {
                                            mc.giveDebuff(dis, skill.getSkill());
                                        }
                                    }
                                }
                            }
                        } else {
                            int amount = enemies.getMembers().size() - 1;
                            int randd = (int) Math.floor(Math.random() * amount);
                            Character chrApp = client.getPlayer().getMap().getCharacterById(enemies.getMemberByPos(randd).getId());
                            if (chrApp != null && chrApp.getMap().isCPQMap()) {
                                if (dis == null) {
                                    chrApp.dispel();
                                } else {
                                    chrApp.giveDebuff(dis, skill.getSkill());
                                }
                            }
                        }
                        neededCP = skill.cpLoss();
                        context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                    } else if (tab == 2) { //protectors
                        final MCSkill skill = CarnivalFactory.getInstance().getGuardian(num);
                        if (skill == null || client.getPlayer().getCP() < skill.cpLoss()) {
                            context.writeAndFlush(ChannelPacketCreator.getInstance().CPQMessage((byte) 1));
                            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                            return;
                        }

                        MonsterCarnival mcpq = client.getPlayer().getMonsterCarnival();
                        if (mcpq != null) {
                            if (!mcpq.canGuardianR() && client.getPlayer().getTeam() == 0 || !mcpq.canGuardianB() && client.getPlayer().getTeam() == 1) {
                                context.writeAndFlush(ChannelPacketCreator.getInstance().CPQMessage((byte) 2));
                                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                                return;
                            }

                            int success = client.getPlayer().getMap().spawnGuardian(client.getPlayer().getTeam(), num);
                            if (success != 1) {
                                switch (success) {
                                    case -1:
                                        context.writeAndFlush(ChannelPacketCreator.getInstance().CPQMessage((byte) 3));
                                        break;

                                    case 0:
                                        context.writeAndFlush(ChannelPacketCreator.getInstance().CPQMessage((byte) 4));
                                        break;

                                    default:
                                        context.writeAndFlush(ChannelPacketCreator.getInstance().CPQMessage((byte) 3));
                                }
                                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                                return;
                            } else {
                                neededCP = skill.cpLoss();
                            }
                        }
                    }
                    client.getPlayer().gainCP(-neededCP);
                    client.getPlayer().getMap().broadcastMessage(ChannelPacketCreator.getInstance().playerSummoned(client.getPlayer().getName(), tab, num));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } finally {
                client.releaseClient();
            }
        }
    }

    private int rollHitChance(MobSkillType type) {
        return switch (type) {
            case DARKNESS, WEAKNESS, POISON, SLOW -> (int) (Math.random() * 100);
            default -> 0;
        };
    }
}
