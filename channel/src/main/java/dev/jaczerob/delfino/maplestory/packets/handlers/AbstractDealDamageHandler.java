package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.BuffStat;
import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Skill;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.client.status.MonsterStatus;
import dev.jaczerob.delfino.maplestory.client.status.MonsterStatusEffect;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.constants.id.MapId;
import dev.jaczerob.delfino.maplestory.constants.id.MobId;
import dev.jaczerob.delfino.maplestory.constants.skills.Beginner;
import dev.jaczerob.delfino.maplestory.constants.skills.SuperGM;
import dev.jaczerob.delfino.maplestory.net.server.PlayerBuffValueHolder;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.scripting.AbstractPlayerInteraction;
import dev.jaczerob.delfino.maplestory.server.StatEffect;
import dev.jaczerob.delfino.maplestory.server.life.Element;
import dev.jaczerob.delfino.maplestory.server.life.ElementalEffectiveness;
import dev.jaczerob.delfino.maplestory.server.life.MobSkill;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillFactory;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillId;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillType;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.packets.InPacket;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractDealDamageHandler extends AbstractPacketHandler {

    protected void applyAttack(AttackInfo attack, final Character player, int attackCount) {
        final MapleMap map = player.getMap();
        Skill theSkill = null;
        StatEffect attackEffect = null;
        try {
            if (player.isBanned()) {
                return;
            }
            if (attack.skill != 0) {
                theSkill = SkillFactory.getSkill(attack.skill);
                attackEffect = attack.getAttackEffect(player, theSkill);
                if (attackEffect == null) {
                    player.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                if (player.getMp() < attackEffect.getMpCon()) {
                    AutobanFactory.MPCON.addPoint(player.getAutobanManager(), "Skill: " + attack.skill + "; Player MP: " + player.getMp() + "; MP Needed: " + attackEffect.getMpCon());
                }

                int mobCount = attackEffect.getMobCount();
                if (player.isAlive()) {
                    attackEffect.applyTo(player);
                } else {
                    player.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                }

                if (attack.numAttacked > mobCount) {
                    AutobanFactory.MOB_COUNT.autoban(player, "Skill: " + attack.skill + "; Count: " + attack.numAttacked + " Max: " + attackEffect.getMobCount());
                    return;
                }
            }
            if (!player.isAlive()) {
                return;
            }

            for (Map.Entry<Integer, AttackTarget> target : attack.targets.entrySet()) {
                final Monster monster = map.getMonsterByOid(target.getKey());
                if (monster != null) {
                    double distance = player.getPosition().distanceSq(monster.getPosition());
                    double distanceToDetect = 200000.0;

                    if (attack.ranged) {
                        distanceToDetect += 400000;
                    }

                    if (attack.magic) {
                        distanceToDetect += 200000;
                    }

                    if (attack.skill == SuperGM.SUPER_DRAGON_ROAR) {
                        distanceToDetect += 250000;
                    }

                    if (distance > distanceToDetect) {
                        AutobanFactory.DISTANCE_HACK.alert(player, "Distance Sq to monster: " + distance + " SID: " + attack.skill + " MID: " + monster.getId());
                        monster.refreshMobPosition();
                    }

                    int totDamageToOneMonster = 0;
                    List<Integer> onedList = target.getValue().damageLines();

                    if (attack.magic) { // thanks BHB, Alex (CanIGetaPR) for noticing no immunity status check here
                        if (monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
                            Collections.fill(onedList, 1);
                        }
                    } else {
                        if (monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY)) {
                            Collections.fill(onedList, 1);
                        }
                    }

                    if (MobId.isDojoBoss(monster.getId())) {
                        if (attack.skill == 1009 || attack.skill == 10001009 || attack.skill == 20001009) {
                            int dmgLimit = (int) Math.ceil(0.3 * monster.getMaxHp());
                            List<Integer> _onedList = new LinkedList<>();
                            for (Integer i : onedList) {
                                _onedList.add(i < dmgLimit ? i : dmgLimit);
                            }

                            onedList = _onedList;
                        }
                    }

                    for (Integer eachd : onedList) {
                        if (eachd < 0) {
                            eachd += Integer.MAX_VALUE;
                        }
                        totDamageToOneMonster += eachd;
                    }
                    monster.aggroMonsterDamage(player, totDamageToOneMonster);
                    if (attack.skill != 0) {
                        if (attackEffect.getFixDamage() != -1) {
                            if (totDamageToOneMonster != attackEffect.getFixDamage() && totDamageToOneMonster != 0) {
                                AutobanFactory.FIX_DAMAGE.autoban(player, totDamageToOneMonster + " damage");
                            }

                            int threeSnailsId = player.getJobType() * 10000000 + 1000;
                            if (attack.skill == threeSnailsId) {
                                if (YamlConfig.config.server.USE_ULTRA_THREE_SNAILS) {
                                    int skillLv = player.getSkillLevel(threeSnailsId);

                                    if (skillLv > 0) {
                                        AbstractPlayerInteraction api = player.getAbstractPlayerInteraction();

                                        int shellId = switch (skillLv) {
                                            case 1 -> ItemId.SNAIL_SHELL;
                                            case 2 -> ItemId.BLUE_SNAIL_SHELL;
                                            default -> ItemId.RED_SNAIL_SHELL;
                                        };

                                        if (api.haveItem(shellId, 1)) {
                                            api.gainItem(shellId, (short) -1, false);
                                            totDamageToOneMonster *= player.getLevel();
                                        } else {
                                            player.dropMessage(5, "You have ran out of shells to activate the hidden power of Three Snails.");
                                        }
                                    } else {
                                        totDamageToOneMonster = 0;
                                    }
                                }
                            }
                        }
                    }
                    if (totDamageToOneMonster > 0 && attackEffect != null) {
                        Map<MonsterStatus, Integer> attackEffectStati = attackEffect.getMonsterStati();
                        if (!attackEffectStati.isEmpty()) {
                            if (attackEffect.makeChanceResult()) {
                                monster.applyStatus(player, new MonsterStatusEffect(attackEffectStati, theSkill, null, false), false, attackEffect.getDuration());
                            }
                        }
                    }
                    map.damageMonster(player, monster, totDamageToOneMonster, target.getValue().delay());
                    if (monster.isBuffed(MonsterStatus.WEAPON_REFLECT) && !attack.magic) {
                        for (MobSkillId msId : monster.getSkills()) {
                            if (msId.type() == MobSkillType.PHYSICAL_AND_MAGIC_COUNTER) {
                                MobSkill toUse = MobSkillFactory.getMobSkillOrThrow(MobSkillType.PHYSICAL_AND_MAGIC_COUNTER, msId.level());
                                player.addHP(-toUse.getX());
                                map.broadcastMessage(player, ChannelPacketCreator.getInstance().damagePlayer(0, monster.getId(), player.getId(), toUse.getX(), 0, 0, false, 0, true, monster.getObjectId(), 0, 0), true);
                            }
                        }
                    }
                    if (monster.isBuffed(MonsterStatus.MAGIC_REFLECT) && attack.magic) {
                        for (MobSkillId msId : monster.getSkills()) {
                            if (msId.type() == MobSkillType.PHYSICAL_AND_MAGIC_COUNTER) {
                                MobSkill toUse = MobSkillFactory.getMobSkillOrThrow(MobSkillType.PHYSICAL_AND_MAGIC_COUNTER, msId.level());
                                player.addHP(-toUse.getY());
                                map.broadcastMessage(player, ChannelPacketCreator.getInstance().damagePlayer(0, monster.getId(), player.getId(), toUse.getY(), 0, 0, false, 0, true, monster.getObjectId(), 0, 0), true);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    protected AttackInfo parseDamage(InPacket packet, Character chr, boolean ranged, boolean magic) {
        //2C 00 00 01 91 A1 12 00 A5 57 62 FC E2 75 99 10 00 47 80 01 04 01 C6 CC 02 DD FF 5F 00
        AttackInfo ret = new AttackInfo();
        packet.readByte();
        ret.numAttackedAndDamage = packet.readByte();
        ret.numAttacked = (ret.numAttackedAndDamage >>> 4) & 0xF;
        ret.numDamage = ret.numAttackedAndDamage & 0xF;
        ret.targets = new HashMap<>();
        ret.skill = packet.readInt();
        ret.ranged = ranged;
        ret.magic = magic;

        if (ret.skill > 0) {
            ret.skilllevel = chr.getSkillLevel(ret.skill);
            if (ret.skilllevel == 0 && GameConstants.isPqSkillMap(chr.getMapId()) && GameConstants.isPqSkill(ret.skill)) {
                ret.skilllevel = 1;
            }
        }

        ret.charge = 0;

        packet.skip(8);
        ret.display = packet.readByte();
        ret.direction = packet.readByte();
        ret.stance = packet.readByte();

        if (ranged) {
            packet.readByte();
            ret.speed = packet.readByte();
            packet.readByte();
            ret.rangedirection = packet.readByte();
            packet.skip(7);
        } else {
            packet.readByte();
            ret.speed = packet.readByte();
            packet.skip(4);
        }

        long calcDmgMax;

        if (magic && ret.skill != 0) {
            calcDmgMax = (long) (Math.ceil((chr.getTotalMagic() * Math.ceil(chr.getTotalMagic() / 1000.0) + chr.getTotalMagic()) / 30.0) + Math.ceil(chr.getTotalInt() / 200.0));
        } else if (ret.skill == 4001344) {
            calcDmgMax = (long) ((chr.getTotalLuk() * 5L) * Math.ceil(chr.getTotalWatk() / 100.0));
        } else {
            calcDmgMax = chr.calculateMaxBaseDamage();
        }

        StatEffect effect = null;
        if (ret.skill != 0) {
            Skill skill = SkillFactory.getSkill(ret.skill);
            effect = skill.getEffect(ret.skilllevel);

            if (magic) {
                calcDmgMax *= effect.getMatk();
            } else {
                // Normal damage formula for skills
                calcDmgMax = calcDmgMax * effect.getDamage() / 100;
            }
        }

        int bonusDmgBuff = 100;
        for (PlayerBuffValueHolder pbvh : chr.getAllBuffs()) {
            int bonusDmg = pbvh.effect.getDamage() - 100;
            bonusDmgBuff += bonusDmg;
        }

        if (bonusDmgBuff != 100) {
            float dmgBuff = bonusDmgBuff / 100.0f;
            calcDmgMax = (long) Math.ceil(calcDmgMax * dmgBuff);
        }

        if (chr.getMapId() >= MapId.ARAN_TUTORIAL_START && chr.getMapId() <= MapId.ARAN_TUTORIAL_MAX) {
            calcDmgMax += 80000; // Aran Tutorial.
        }

        boolean canCrit = false;

        if (chr.getBuffEffect(BuffStat.SHARP_EYES) != null) {
            canCrit = true;
            calcDmgMax *= 1.4;
        }

        boolean shadowPartner = chr.getBuffEffect(BuffStat.SHADOWPARTNER) != null;

        if (ret.skill != 0) {
            int fixed = ret.getAttackEffect(chr, SkillFactory.getSkill(ret.skill)).getFixDamage();
            if (fixed > 0) {
                calcDmgMax = fixed;
            }
        }
        for (int i = 0; i < ret.numAttacked; i++) {
            int oid = packet.readInt();
            packet.skip(4);
            short delay = packet.readShort();
            List<Integer> damageLines = new ArrayList<>();
            final Monster monster = chr.getMap().getMonsterByOid(oid);

            if (ret.skill != 0) {
                Skill skill = SkillFactory.getSkill(ret.skill);
                if (skill.getElement() != Element.NEUTRAL && chr.getBuffedValue(BuffStat.ELEMENTAL_RESET) == null) {
                    if (monster != null) {
                        ElementalEffectiveness eff = monster.getElementalEffectiveness(skill.getElement());
                        if (eff == ElementalEffectiveness.WEAK) {
                            calcDmgMax *= 1.5;
                        }
                    } else {
                        calcDmgMax *= 1.5;
                    }
                }
            }

            for (int j = 0; j < ret.numDamage; j++) {
                int damage = packet.readInt();
                long hitDmgMax = calcDmgMax;
                if (ret.skill == Beginner.BAMBOO_RAIN) {
                    hitDmgMax = 82569000; // 30% of Max HP of strongest Dojo boss
                }

                long maxWithCrit = hitDmgMax;
                if (canCrit) // They can crit, so up the max.
                {
                    maxWithCrit *= 2;
                }

                // Warn if the damage is over 1.5x what we calculated above.
                if (damage > maxWithCrit * 1.5) {
                    AutobanFactory.DAMAGE_HACK.alert(chr, "DMG: " + damage + " MaxDMG: " + maxWithCrit + " SID: " + ret.skill + " MobID: " + (monster != null ? monster.getId() : "null") + " Map: " + chr.getMap().getMapName() + " (" + chr.getMapId() + ")");
                }

                // Add a ab point if its over 5x what we calculated.
                if (damage > maxWithCrit * 5) {
                    AutobanFactory.DAMAGE_HACK.addPoint(chr.getAutobanManager(), "DMG: " + damage + " MaxDMG: " + maxWithCrit + " SID: " + ret.skill + " MobID: " + (monster != null ? monster.getId() : "null") + " Map: " + chr.getMap().getMapName() + " (" + chr.getMapId() + ")");
                }

                if (canCrit && damage > hitDmgMax) {
                    // If the skill is a crit, inverse the damage to make it show up on clients.
                    damage = -Integer.MAX_VALUE + damage - 1;
                }

                if (effect != null) {
                    int maxattack = Math.max(effect.getBulletCount(), effect.getAttackCount());
                    if (shadowPartner) {
                        maxattack = maxattack * 2;
                    }
                    if (ret.numDamage > maxattack) {
                        AutobanFactory.DAMAGE_HACK.addPoint(chr.getAutobanManager(), "Too many lines: " + ret.numDamage + " Max lines: " + maxattack + " SID: " + ret.skill + " MobID: " + (monster != null ? monster.getId() : "null") + " Map: " + chr.getMap().getMapName() + " (" + chr.getMapId() + ")");
                    }
                }

                damageLines.add(damage);
            }
            packet.skip(4);
            ret.targets.put(oid, new AttackTarget(delay, damageLines));
        }
        return ret;
    }

    public static class AttackInfo {

        public int numAttacked, numDamage, numAttackedAndDamage, skill, skilllevel, stance, direction, rangedirection, charge, display;
        public Map<Integer, AttackTarget> targets;
        public boolean ranged, magic;
        public int speed = 4;
        public Point position = new Point();
        public List<Integer> explodedMesos;
        public Short attackDelay;

        public StatEffect getAttackEffect(Character chr, Skill theSkill) {
            Skill mySkill = theSkill;
            if (mySkill == null) {
                mySkill = SkillFactory.getSkill(skill);
            }

            int skillLevel = chr.getSkillLevel(mySkill);
            if (skillLevel == 0 && GameConstants.isPqSkillMap(chr.getMapId()) && GameConstants.isPqSkill(mySkill.getId())) {
                skillLevel = 1;
            }

            if (skillLevel == 0) {
                return null;
            }
            if (display > 80) { //Hmm
                if (!mySkill.getAction()) {
                    AutobanFactory.FAST_ATTACK.autoban(chr, "WZ Edit; adding action to a skill: " + display);
                    return null;
                }
            }
            return mySkill.getEffect(skillLevel);
        }
    }

    // TODO: add position
    public record AttackTarget(short delay, List<Integer> damageLines) {
    }
}
