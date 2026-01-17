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

import dev.jaczerob.delfino.maplestory.client.BuffStat;
import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.Skill;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.Inventory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.client.status.MonsterStatus;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.id.MapId;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.StatEffect;
import dev.jaczerob.delfino.maplestory.server.life.LifeFactory.loseItem;
import dev.jaczerob.delfino.maplestory.server.life.MobAttackInfo;
import dev.jaczerob.delfino.maplestory.server.life.MobAttackInfoFactory;
import dev.jaczerob.delfino.maplestory.server.life.MobSkill;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillFactory;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillType;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.server.maps.MapObject;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.Randomizer;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public final class TakeDamageHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(TakeDamageHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.TAKE_DAMAGE;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        List<Character> banishPlayers = new ArrayList<>();

        Character chr = client.getPlayer();
        packet.readInt();
        byte damagefrom = packet.readByte();
        packet.readByte(); //Element
        int damage = packet.readInt();
        int oid = 0, monsteridfrom = 0, pgmr = 0, direction = 0;
        int pos_x = 0, pos_y = 0, fake = 0;
        boolean is_pgmr = false, is_pg = true, is_deadly = false;
        int mpattack = 0;
        Monster attacker = null;
        final MapleMap map = chr.getMap();
        if (damagefrom != -3 && damagefrom != -4) {
            monsteridfrom = packet.readInt();
            oid = packet.readInt();

            try {
                MapObject mmo = map.getMapObject(oid);
                if (mmo instanceof Monster) {
                    attacker = (Monster) mmo;
                    if (attacker.getId() != monsteridfrom) {
                        attacker = null;
                    }
                }

                if (attacker != null) {
                    if (attacker.isBuffed(MonsterStatus.NEUTRALISE)) {
                        return;
                    }

                    List<loseItem> loseItems;
                    if (damage > 0) {
                        loseItems = attacker.getStats().loseItem();
                        if (loseItems != null) {
                            if (chr.getBuffEffect(BuffStat.AURA) == null) {
                                InventoryType type;
                                final int playerpos = chr.getPosition().x;
                                byte d = 1;
                                Point pos = new Point(0, chr.getPosition().y);
                                for (loseItem loseItem : loseItems) {
                                    type = ItemConstants.getInventoryType(loseItem.getId());

                                    int dropCount = 0;
                                    for (byte b = 0; b < loseItem.getX(); b++) {
                                        if (Randomizer.nextInt(100) < loseItem.getChance()) {
                                            dropCount += 1;
                                        }
                                    }

                                    if (dropCount > 0) {
                                        int qty;

                                        Inventory inv = chr.getInventory(type);
                                        inv.lockInventory();
                                        try {
                                            qty = Math.min(chr.countItem(loseItem.getId()), dropCount);
                                            InventoryManipulator.removeById(client, type, loseItem.getId(), qty, false, false);
                                        } finally {
                                            inv.unlockInventory();
                                        }

                                        for (byte b = 0; b < qty; b++) {
                                            pos.x = playerpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2)));
                                            map.spawnItemDrop(chr, chr, new Item(loseItem.getId(), (short) 0, (short) 1), map.calcDropPos(pos, chr.getPosition()), true, true);
                                            d++;
                                        }
                                    }
                                }
                            }
                            map.removeMapObject(attacker);
                        }
                    }
                } else if (damagefrom != 0 || !map.removeSelfDestructive(oid)) {    // thanks inhyuk for noticing self-destruct damage not being handled properly
                    return;
                }
            } catch (ClassCastException e) {
                //this happens due to mob on last map damaging player just before changing maps
                log.warn("Attack is not a mob-type, rather is a {} entity", map.getMapObject(oid).getClass().getSimpleName(), e);
                return;
            }

            direction = packet.readByte();
        }
        if (damagefrom != -1 && damagefrom != -2 && attacker != null) {
            MobAttackInfo attackInfo = MobAttackInfoFactory.getMobAttackInfo(attacker, damagefrom);
            if (attackInfo != null) {
                if (attackInfo.isDeadlyAttack()) {
                    mpattack = chr.getMp() - 1;
                    is_deadly = true;
                }
                mpattack += attackInfo.getMpBurn();

                Optional<MobSkillType> possibleType = MobSkillType.from(attackInfo.getDiseaseSkill());
                Optional<MobSkill> possibleMobSkill = possibleType.map(type -> MobSkillFactory.getMobSkillOrThrow(type, attackInfo.getDiseaseLevel()));
                if (possibleMobSkill.isPresent() && damage > 0) {
                    possibleMobSkill.get().applyEffect(chr, attacker, false, banishPlayers);
                }

                attacker.setMp(attacker.getMp() - attackInfo.getMpCon());
                if (chr.getBuffedValue(BuffStat.MANA_REFLECTION) != null && damage > 0 && !attacker.isBoss()) {
                    int jobid = chr.getJob().getId();
                    if (jobid == 212 || jobid == 222 || jobid == 232) {
                        int id = jobid * 10000 + 1002;
                        Skill manaReflectSkill = SkillFactory.getSkill(id);
                        if (chr.isBuffFrom(BuffStat.MANA_REFLECTION, manaReflectSkill) && chr.getSkillLevel(manaReflectSkill) > 0 && manaReflectSkill.getEffect(chr.getSkillLevel(manaReflectSkill)).makeChanceResult()) {
                            int bouncedamage = (damage * manaReflectSkill.getEffect(chr.getSkillLevel(manaReflectSkill)).getX() / 100);
                            if (bouncedamage > attacker.getMaxHp() / 5) {
                                bouncedamage = attacker.getMaxHp() / 5;
                            }
                            map.damageMonster(chr, attacker, bouncedamage);
                            map.broadcastMessage(chr, ChannelPacketCreator.getInstance().damageMonster(oid, bouncedamage), true);
                            chr.sendPacket(ChannelPacketCreator.getInstance().showOwnBuffEffect(id, 5));
                            map.broadcastMessage(chr, ChannelPacketCreator.getInstance().showBuffEffect(chr.getId(), id, 5), false);
                        }
                    }
                }
            }
        }

        if (damage == -1) {
            fake = 4020002 + (chr.getJob().getId() / 10 - 40) * 100000;
        }

        if (damage > 0) {
            chr.getAutobanManager().resetMisses();
        } else {
            chr.getAutobanManager().addMiss();
        }

        //in dojo player cannot use pot, so deadly attacks should be turned off as well
        if (is_deadly && MapId.isDojo(chr.getMap().getId()) && !YamlConfig.config.server.USE_DEADLY_DOJO) {
            damage = 0;
            mpattack = 0;
        }

        if (damage > 0 && !chr.isHidden()) {
            if (attacker != null) {
                if (damagefrom == -1) {
                    if (chr.getBuffedValue(BuffStat.POWERGUARD) != null) { // PG works on bosses, but only at half of the rate.
                        int bouncedamage = (int) (damage * (chr.getBuffedValue(BuffStat.POWERGUARD).doubleValue() / (attacker.isBoss() ? 200 : 100)));
                        bouncedamage = Math.min(bouncedamage, attacker.getMaxHp() / 10);
                        damage -= bouncedamage;
                        map.damageMonster(chr, attacker, bouncedamage);
                        map.broadcastMessage(chr, ChannelPacketCreator.getInstance().damageMonster(oid, bouncedamage), false, true);
                        attacker.aggroMonsterDamage(chr, bouncedamage);
                    }
                }

                StatEffect cBarrier = chr.getBuffEffect(BuffStat.COMBO_BARRIER);  // thanks BHB for noticing Combo Barrier buff not working
                if (cBarrier != null) {
                    damage *= (cBarrier.getX() / 1000.0);
                }
            }
            if (damagefrom != -3 && damagefrom != -4) {
                int achilles = 0;
                Skill achilles1 = null;
                int jobid = chr.getJob().getId();
                if (jobid < 200 && jobid % 10 == 2) {
                    achilles1 = SkillFactory.getSkill(jobid * 10000 + (jobid == 112 ? 4 : 5));
                    achilles = chr.getSkillLevel(achilles1);
                }
                if (achilles != 0 && achilles1 != null) {
                    damage *= (achilles1.getEffect(achilles).getX() / 1000.0);
                }
            }
            Integer mesoguard = chr.getBuffedValue(BuffStat.MESOGUARD);
            if (chr.getBuffedValue(BuffStat.MAGIC_GUARD) != null && mpattack == 0) {
                int mploss = (int) (damage * (chr.getBuffedValue(BuffStat.MAGIC_GUARD).doubleValue() / 100.0));
                int hploss = damage - mploss;

                int curmp = chr.getMp();
                if (mploss > curmp) {
                    hploss += mploss - curmp;
                    mploss = curmp;
                }

                chr.addMPHP(-hploss, -mploss);
            } else if (mesoguard != null) {
                damage = Math.round(damage / 2);
                int mesoloss = (int) (damage * (mesoguard.doubleValue() / 100.0));
                if (chr.getMeso() < mesoloss) {
                    chr.gainMeso(-chr.getMeso(), false);
                    chr.cancelBuffStats(BuffStat.MESOGUARD);
                } else {
                    chr.gainMeso(-mesoloss, false);
                }
                chr.addMPHP(-damage, -mpattack);
            } else {
                chr.addMPHP(-damage, -mpattack);
            }
        }
        if (!chr.isHidden()) {
            map.broadcastMessage(chr, ChannelPacketCreator.getInstance().damagePlayer(damagefrom, monsteridfrom, chr.getId(), damage, fake, direction, is_pgmr, pgmr, is_pg, oid, pos_x, pos_y), false);
        } else {
            map.broadcastGMMessage(chr, ChannelPacketCreator.getInstance().damagePlayer(damagefrom, monsteridfrom, chr.getId(), damage, fake, direction, is_pgmr, pgmr, is_pg, oid, pos_x, pos_y), false);
        }

        for (Character player : banishPlayers) {  // chill, if this list ever gets non-empty an attacker does exist, trust me :)
            player.changeMapBanish(attacker.getBanish());
        }
    }
}
