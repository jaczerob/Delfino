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
package dev.jaczerob.delfino.maplestory.server;

import dev.jaczerob.delfino.maplestory.client.BuffStat;
import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Disease;
import dev.jaczerob.delfino.maplestory.client.Mount;
import dev.jaczerob.delfino.maplestory.client.Skill;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.client.status.MonsterStatus;
import dev.jaczerob.delfino.maplestory.client.status.MonsterStatusEffect;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.constants.id.MapId;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.constants.skills.Beginner;
import dev.jaczerob.delfino.maplestory.constants.skills.GM;
import dev.jaczerob.delfino.maplestory.constants.skills.SuperGM;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.provider.Data;
import dev.jaczerob.delfino.maplestory.provider.DataTool;
import dev.jaczerob.delfino.maplestory.server.life.MobSkill;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillFactory;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillType;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.server.maps.MapObject;
import dev.jaczerob.delfino.maplestory.server.maps.MapObjectType;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.server.maps.Portal;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.Pair;
import dev.jaczerob.delfino.network.packets.Packet;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Matze
 * @author Frz
 * @author Ronan
 */
public class StatEffect {
    private short watk, matk, wdef, mdef, acc, avoid, speed, jump;
    private short hp, mp;
    private double hpR, mpR;
    private short mhpRRate, mmpRRate, mobSkill, mobSkillLevel;
    private byte mhpR, mmpR;
    private short mpCon, hpCon;
    private int duration, target, barrier, mob;
    private boolean overTime, repeatEffect;
    private int sourceid;
    private int moveTo;
    private int cp, nuffSkill;
    private List<Disease> cureDebuffs;
    private boolean skill;
    private List<Pair<BuffStat, Integer>> statups;
    private Map<MonsterStatus, Integer> monsterStatus;
    private int x, y, mobCount, moneyCon, cooldown, morphId = 0, ghost, fatigue, berserk, booster;
    private double prop;
    private int itemCon, itemConNo;
    private int damage, attackCount, fixdamage;
    private Point lt, rb;
    private short bulletCount, bulletConsume;
    private byte mapProtection;
    private CardItemupStats cardStats;

    public static StatEffect loadSkillEffectFromData(Data source, int skillid, boolean overtime) {
        return loadFromData(source, skillid, true, overtime);
    }

    public static StatEffect loadItemEffectFromData(Data source, int itemid) {
        return loadFromData(source, itemid, false, false);
    }

    private static void addBuffStatPairToListIfNotZero(List<Pair<BuffStat, Integer>> list, BuffStat buffstat, Integer val) {
        if (val != 0) {
            list.add(new Pair<>(buffstat, val));
        }
    }

    private static byte mapProtection(int sourceid) {
        if (sourceid == ItemId.RED_BEAN_PORRIDGE || sourceid == ItemId.SOFT_WHITE_BUN) {
            return 1;   //elnath cold
        } else if (sourceid == ItemId.AIR_BUBBLE) {
            return 2;   //aqua road underwater
        } else {
            return 0;
        }
    }

    private static StatEffect loadFromData(Data source, int sourceid, boolean skill, boolean overTime) {
        StatEffect ret = new StatEffect();
        ret.duration = DataTool.getIntConvert("time", source, -1);
        ret.hp = (short) DataTool.getInt("hp", source, 0);
        ret.hpR = DataTool.getInt("hpR", source, 0) / 100.0;
        ret.mp = (short) DataTool.getInt("mp", source, 0);
        ret.mpR = DataTool.getInt("mpR", source, 0) / 100.0;
        ret.mpCon = (short) DataTool.getInt("mpCon", source, 0);
        ret.hpCon = (short) DataTool.getInt("hpCon", source, 0);
        int iprop = DataTool.getInt("prop", source, 100);
        ret.prop = iprop / 100.0;

        ret.cp = DataTool.getInt("cp", source, 0);
        List<Disease> cure = new ArrayList<>(5);
        if (DataTool.getInt("poison", source, 0) > 0) {
            cure.add(Disease.POISON);
        }
        if (DataTool.getInt("seal", source, 0) > 0) {
            cure.add(Disease.SEAL);
        }
        if (DataTool.getInt("darkness", source, 0) > 0) {
            cure.add(Disease.DARKNESS);
        }
        if (DataTool.getInt("weakness", source, 0) > 0) {
            cure.add(Disease.WEAKEN);
            cure.add(Disease.SLOW);
        }
        if (DataTool.getInt("curse", source, 0) > 0) {
            cure.add(Disease.CURSE);
        }
        ret.cureDebuffs = cure;
        ret.nuffSkill = DataTool.getInt("nuffSkill", source, 0);

        ret.mobCount = DataTool.getInt("mobCount", source, 1);
        ret.cooldown = DataTool.getInt("cooltime", source, 0);
        ret.morphId = DataTool.getInt("morph", source, 0);
        ret.ghost = DataTool.getInt("ghost", source, 0);
        ret.fatigue = DataTool.getInt("incFatigue", source, 0);
        ret.repeatEffect = DataTool.getInt("repeatEffect", source, 0) > 0;

        Data mdd = source.getChildByPath("0");
        if (mdd != null && mdd.getChildren().size() > 0) {
            ret.mobSkill = (short) DataTool.getInt("mobSkill", mdd, 0);
            ret.mobSkillLevel = (short) DataTool.getInt("level", mdd, 0);
            ret.target = DataTool.getInt("target", mdd, 0);
        } else {
            ret.mobSkill = 0;
            ret.mobSkillLevel = 0;
            ret.target = 0;
        }

        Data mdds = source.getChildByPath("mob");
        if (mdds != null) {
            if (mdds.getChildren() != null && mdds.getChildren().size() > 0) {
                ret.mob = DataTool.getInt("mob", mdds, 0);
            }
        }
        ret.sourceid = sourceid;
        ret.skill = skill;
        if (!ret.skill && ret.duration > -1) {
            ret.overTime = true;
        } else {
            ret.duration *= 1000; // items have their times stored in ms, of course
            ret.overTime = overTime;
        }

        ArrayList<Pair<BuffStat, Integer>> statups = new ArrayList<>();
        ret.watk = (short) DataTool.getInt("pad", source, 0);
        ret.wdef = (short) DataTool.getInt("pdd", source, 0);
        ret.matk = (short) DataTool.getInt("mad", source, 0);
        ret.mdef = (short) DataTool.getInt("mdd", source, 0);
        ret.acc = (short) DataTool.getIntConvert("acc", source, 0);
        ret.avoid = (short) DataTool.getInt("eva", source, 0);

        ret.speed = (short) DataTool.getInt("speed", source, 0);
        ret.jump = (short) DataTool.getInt("jump", source, 0);

        ret.barrier = DataTool.getInt("barrier", source, 0);
        addBuffStatPairToListIfNotZero(statups, BuffStat.AURA, ret.barrier);

        ret.mapProtection = mapProtection(sourceid);
        addBuffStatPairToListIfNotZero(statups, BuffStat.MAP_PROTECTION, (int) ret.mapProtection);

        if (ret.overTime) {
            if (!skill) {
                if (ItemId.isDojoBuff(sourceid) || isHpMpRecovery(sourceid)) {
                    ret.mhpR = (byte) DataTool.getInt("mhpR", source, 0);
                    ret.mhpRRate = (short) (DataTool.getInt("mhpRRate", source, 0) * 100);
                    ret.mmpR = (byte) DataTool.getInt("mmpR", source, 0);
                    ret.mmpRRate = (short) (DataTool.getInt("mmpRRate", source, 0) * 100);

                    addBuffStatPairToListIfNotZero(statups, BuffStat.HPREC, (int) ret.mhpR);
                    addBuffStatPairToListIfNotZero(statups, BuffStat.MPREC, (int) ret.mmpR);

                } else if (ItemId.isRateCoupon(sourceid)) {
                    switch (DataTool.getInt("expR", source, 0)) {
                        case 1:
                            addBuffStatPairToListIfNotZero(statups, BuffStat.COUPON_EXP1, 1);
                            break;

                        case 2:
                            addBuffStatPairToListIfNotZero(statups, BuffStat.COUPON_EXP2, 1);
                            break;

                        case 3:
                            addBuffStatPairToListIfNotZero(statups, BuffStat.COUPON_EXP3, 1);
                            break;

                        case 4:
                            addBuffStatPairToListIfNotZero(statups, BuffStat.COUPON_EXP4, 1);
                            break;
                    }

                    switch (DataTool.getInt("drpR", source, 0)) {
                        case 1:
                            addBuffStatPairToListIfNotZero(statups, BuffStat.COUPON_DRP1, 1);
                            break;

                        case 2:
                            addBuffStatPairToListIfNotZero(statups, BuffStat.COUPON_DRP2, 1);
                            break;

                        case 3:
                            addBuffStatPairToListIfNotZero(statups, BuffStat.COUPON_DRP3, 1);
                            break;
                    }
                } else if (ItemId.isMonsterCard(sourceid)) {
                    int prob = 0, itemupCode = Integer.MAX_VALUE;
                    List<Pair<Integer, Integer>> areas = null;
                    boolean inParty = false;

                    Data con = source.getChildByPath("con");
                    if (con != null) {
                        areas = new ArrayList<>(3);

                        for (Data conData : con.getChildren()) {
                            int type = DataTool.getInt("type", conData, -1);

                            if (type == 0) {
                                int startMap = DataTool.getInt("sMap", conData, 0);
                                int endMap = DataTool.getInt("eMap", conData, 0);

                                areas.add(new Pair<>(startMap, endMap));
                            } else if (type == 2) {
                                inParty = true;
                            }
                        }

                        if (areas.isEmpty()) {
                            areas = null;
                        }
                    }

                    if (DataTool.getInt("mesoupbyitem", source, 0) != 0) {
                        addBuffStatPairToListIfNotZero(statups, BuffStat.MESO_UP_BY_ITEM, 4);
                        prob = DataTool.getInt("prob", source, 1);
                    }

                    int itemupType = DataTool.getInt("itemupbyitem", source, 0);
                    if (itemupType != 0) {
                        addBuffStatPairToListIfNotZero(statups, BuffStat.ITEM_UP_BY_ITEM, 4);
                        prob = DataTool.getInt("prob", source, 1);

                        switch (itemupType) {
                            case 2:
                                itemupCode = DataTool.getInt("itemCode", source, 1);
                                break;

                            case 3:
                                itemupCode = DataTool.getInt("itemRange", source, 1);    // 3 digits
                                break;
                        }
                    }

                    if (DataTool.getInt("respectPimmune", source, 0) != 0) {
                        addBuffStatPairToListIfNotZero(statups, BuffStat.RESPECT_PIMMUNE, 4);
                    }

                    if (DataTool.getInt("respectMimmune", source, 0) != 0) {
                        addBuffStatPairToListIfNotZero(statups, BuffStat.RESPECT_MIMMUNE, 4);
                    }

                    if (DataTool.getString("defenseAtt", source, null) != null) {
                        addBuffStatPairToListIfNotZero(statups, BuffStat.DEFENSE_ATT, 4);
                    }

                    if (DataTool.getString("defenseState", source, null) != null) {
                        addBuffStatPairToListIfNotZero(statups, BuffStat.DEFENSE_STATE, 4);
                    }

                    int thaw = DataTool.getInt("thaw", source, 0);
                    if (thaw != 0) {
                        addBuffStatPairToListIfNotZero(statups, BuffStat.MAP_PROTECTION, thaw > 0 ? 1 : 2);
                    }

                    ret.cardStats = new CardItemupStats(itemupCode, prob, areas, inParty);
                } else if (ItemId.isExpIncrease(sourceid)) {
                    addBuffStatPairToListIfNotZero(statups, BuffStat.EXP_INCREASE, DataTool.getInt("expinc", source, 0));
                }
            } else {
                if (isMapChair(sourceid)) {
                    addBuffStatPairToListIfNotZero(statups, BuffStat.MAP_CHAIR, 1);
                } else if (sourceid == Beginner.NIMBLE_FEET && YamlConfig.config.server.USE_ULTRA_NIMBLE_FEET) {
                    ret.jump = (short) (ret.speed * 4);
                    ret.speed *= 15;
                }
            }

            addBuffStatPairToListIfNotZero(statups, BuffStat.WATK, (int) ret.watk);
            addBuffStatPairToListIfNotZero(statups, BuffStat.WDEF, (int) ret.wdef);
            addBuffStatPairToListIfNotZero(statups, BuffStat.MATK, (int) ret.matk);
            addBuffStatPairToListIfNotZero(statups, BuffStat.MDEF, (int) ret.mdef);
            addBuffStatPairToListIfNotZero(statups, BuffStat.ACC, (int) ret.acc);
            addBuffStatPairToListIfNotZero(statups, BuffStat.AVOID, (int) ret.avoid);
            addBuffStatPairToListIfNotZero(statups, BuffStat.SPEED, (int) ret.speed);
            addBuffStatPairToListIfNotZero(statups, BuffStat.JUMP, (int) ret.jump);
        }

        Data ltd = source.getChildByPath("lt");
        if (ltd != null) {
            ret.lt = (Point) ltd.getData();
            ret.rb = (Point) source.getChildByPath("rb").getData();

            if (YamlConfig.config.server.USE_MAXRANGE_ECHO_OF_HERO && sourceid == Beginner.ECHO_OF_HERO) {
                ret.lt = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
                ret.rb = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
        }

        int x = DataTool.getInt("x", source, 0);

        if (sourceid == Beginner.RECOVERY && YamlConfig.config.server.USE_ULTRA_RECOVERY) {
            x *= 10;
        }
        ret.x = x;
        ret.y = DataTool.getInt("y", source, 0);

        ret.damage = DataTool.getIntConvert("damage", source, 100);
        ret.fixdamage = DataTool.getIntConvert("fixdamage", source, -1);
        ret.attackCount = DataTool.getIntConvert("attackCount", source, 1);
        ret.bulletCount = (short) DataTool.getIntConvert("bulletCount", source, 1);
        ret.bulletConsume = (short) DataTool.getIntConvert("bulletConsume", source, 0);
        ret.moneyCon = DataTool.getIntConvert("moneyCon", source, 0);
        ret.itemCon = DataTool.getInt("itemCon", source, 0);
        ret.itemConNo = DataTool.getInt("itemConNo", source, 0);
        ret.moveTo = DataTool.getInt("moveTo", source, -1);
        Map<MonsterStatus, Integer> monsterStatus = new EnumMap<>(MonsterStatus.class);
        if (skill) {
            switch (sourceid) {
                // BEGINNER
                case Beginner.RECOVERY:
                    statups.add(new Pair<>(BuffStat.RECOVERY, x));
                    break;
                case Beginner.ECHO_OF_HERO:
                    statups.add(new Pair<>(BuffStat.ECHO_OF_HERO, ret.x));
                    break;
                case Beginner.MONSTER_RIDER:
                case Beginner.SPACESHIP:
                case Beginner.YETI_MOUNT1:
                case Beginner.YETI_MOUNT2:
                case Beginner.WITCH_BROOMSTICK:
                case Beginner.BALROG_MOUNT:
                    statups.add(new Pair<>(BuffStat.MONSTER_RIDING, sourceid));
                    break;
                case Beginner.INVINCIBLE_BARRIER:
                    statups.add(new Pair<>(BuffStat.DIVINE_BODY, 1));
                    break;
                case GM.HYPER_BODY:
                case SuperGM.HYPER_BODY:
                    statups.add(new Pair<>(BuffStat.HYPERBODYHP, x));
                    statups.add(new Pair<>(BuffStat.HYPERBODYMP, ret.y));
                    break;
                case SuperGM.HOLY_SYMBOL:
                    statups.add(new Pair<>(BuffStat.HOLY_SYMBOL, x));
                    break;
                case Beginner.SPACE_DASH:
                    statups.add(new Pair<>(BuffStat.DASH2, ret.x));
                    statups.add(new Pair<>(BuffStat.DASH, ret.y));
                    break;
                case Beginner.POWER_EXPLOSION:
                    statups.add(new Pair<>(BuffStat.BOOSTER, x));
                    break;
                default:
                    break;
            }
        }
        if (ret.isMorph()) {
            statups.add(new Pair<>(BuffStat.MORPH, ret.getMorph()));
        }
        if (ret.ghost > 0 && !skill) {
            statups.add(new Pair<>(BuffStat.GHOST_MORPH, ret.ghost));
        }
        ret.monsterStatus = monsterStatus;
        statups.trimToSize();
        ret.statups = statups;
        return ret;
    }

    public static boolean isMapChair(int sourceid) {
        return sourceid == Beginner.MAP_CHAIR;
    }

    public static boolean isHpMpRecovery(int sourceid) {
        return sourceid == ItemId.RUSSELLONS_PILLS || sourceid == ItemId.SORCERERS_POTION;
    }

    private boolean isEffectActive(int mapid, boolean partyHunting) {
        if (cardStats == null) {
            return true;
        }

        if (!cardStats.isInArea(mapid)) {
            return false;
        }

        return !cardStats.party || partyHunting;
    }

    public boolean isActive(Character applyto) {
        return isEffectActive(applyto.getMapId(), applyto.getPartyMembersOnSameMap().size() > 1);
    }

    public int getCardRate(int itemid) {
        if (cardStats != null) {
            if (cardStats.itemCode == Integer.MAX_VALUE) {
                return cardStats.prob;
            } else if (cardStats.itemCode < 1000) {
                if (itemid / 10000 == cardStats.itemCode) {
                    return cardStats.prob;
                }
            } else {
                if (itemid == cardStats.itemCode) {
                    return cardStats.prob;
                }
            }
        }

        return 0;
    }

    public boolean applyEchoOfHero(Character applyfrom) {
        Map<Integer, Character> mapPlayers = applyfrom.getMap().getMapPlayers();
        mapPlayers.remove(applyfrom.getId());

        boolean hwResult = applyTo(applyfrom);
        for (Character chr : mapPlayers.values()) {    // Echo of Hero not buffing players in the map detected thanks to Masterrulax
            applyTo(applyfrom, chr, false, null, false);
        }

        return hwResult;
    }

    public boolean applyTo(Character chr) {
        return applyTo(chr, chr, true, null, false);
    }

    public boolean applyTo(Character chr, boolean useMaxRange) {
        return applyTo(chr, chr, true, null, useMaxRange);
    }

    public boolean applyTo(Character chr, Point pos) {
        return applyTo(chr, chr, true, pos, false);
    }

    // primary: the player caster of the buff
    private boolean applyTo(Character applyfrom, Character applyto, boolean primary, Point pos, boolean useMaxRange) {
        if (skill && (sourceid == GM.HIDE || sourceid == SuperGM.HIDE)) {
            applyto.toggleHide();
            return true;
        }

        int hpchange = calcHPChange(applyfrom, primary);
        int mpchange = calcMPChange(applyfrom, primary);
        if (primary) {
            if (itemConNo != 0) {
                if (!applyto.getAbstractPlayerInteraction().hasItem(itemCon, itemConNo)) {
                    applyto.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return false;
                }
                InventoryManipulator.removeById(applyto.getClient(), ItemConstants.getInventoryType(itemCon), itemCon, itemConNo, false, true);
            }
        } else {
            if (isResurrection()) {
                hpchange = applyto.getCurrentMaxHp();
                applyto.broadcastStance(applyto.isFacingLeft() ? 5 : 4);
            }
        }

        if (isDispel() && makeChanceResult()) {
            applyto.dispelDebuffs();
        } else if (isCureAllAbnormalStatus()) {
            applyto.purgeDebuffs();
        }

        if (!applyto.applyHpMpChange(hpCon, hpchange, mpchange)) {
            applyto.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return false;
        }

        if (moveTo != -1) {
            if (moveTo != applyto.getMapId()) {
                MapleMap target;
                Portal pt;

                if (moveTo == MapId.NONE) {
                    target = applyto.getMap().getReturnMap();
                    pt = target.getRandomPlayerSpawnpoint();
                } else {
                    target = applyto.getClient().getWorldServer().getChannel(applyto.getClient().getChannel()).getMapFactory().getMap(moveTo);
                    int targetid = target.getId() / 10000000;
                    if (targetid != 60 && applyto.getMapId() / 10000000 != 61 && targetid != applyto.getMapId() / 10000000 && targetid != 21 && targetid != 20 && targetid != 12 && (applyto.getMapId() / 10000000 != 10 && applyto.getMapId() / 10000000 != 12)) {
                        return false;
                    }

                    pt = target.getRandomPlayerSpawnpoint();
                }

                applyto.changeMap(target, pt);
            } else {
                return false;
            }
        }
        if (overTime) {
            applyBuffEffect(applyfrom, applyto, primary);
        }

        if (primary && isMonsterBuff()) {
            applyMonsterBuff(applyfrom);
        }

        if (this.getFatigue() != 0) {
            applyto.getMount().setTiredness(applyto.getMount().getTiredness() + this.getFatigue());
        }

        if (!cureDebuffs.isEmpty()) { // added by Drago (Dragohe4rt)
            for (final Disease debuff : cureDebuffs) {
                applyfrom.dispelDebuff(debuff);
            }
        } else if (mobSkill > 0 && mobSkillLevel > 0) {
            MobSkillType mobSkillType = MobSkillType.from(mobSkill).orElseThrow();
            MobSkill ms = MobSkillFactory.getMobSkillOrThrow(mobSkillType, mobSkillLevel);
            Disease dis = Disease.getBySkill(mobSkillType);

            if (target > 0) {
                for (Character chr : applyto.getMap().getAllPlayers()) {
                    if (chr.getId() != applyto.getId()) {
                        chr.giveDebuff(dis, ms);
                    }
                }
            } else {
                applyto.giveDebuff(dis, ms);
            }
        }
        return true;
    }

    private void applyMonsterBuff(Character applyfrom) {
        Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
        List<MapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, List.of(MapObjectType.MONSTER));
        Skill skill_ = SkillFactory.getSkill(sourceid);
        int i = 0;
        for (MapObject mo : affected) {
            Monster monster = (Monster) mo;
            if (isDispel()) {
                monster.debuffMob();
            } else {
                if (makeChanceResult()) {
                    monster.applyStatus(applyfrom, new MonsterStatusEffect(getMonsterStati(), skill_, null, false), false, getDuration());
                }
            }
            i++;
            if (i >= mobCount) {
                break;
            }
        }
    }

    private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        Point mylt;
        Point myrb;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            myrb = new Point(-lt.x + posFrom.x, rb.y + posFrom.y);  // thanks Conrad, April for noticing a disturbance in AoE skill behavior after a hitched refactor here
            mylt = new Point(-rb.x + posFrom.x, lt.y + posFrom.y);
        }
        Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }

    public int getBuffLocalDuration() {
        return !YamlConfig.config.server.USE_BUFF_EVERLASTING ? duration : Integer.MAX_VALUE;
    }

    public void silentApplyBuff(Character chr, long localStartTime) {
        int localDuration = getBuffLocalDuration();
        chr.registerEffect(this, localStartTime, localStartTime + localDuration, true);
    }

    public final void applyBeaconBuff(final Character applyto, int objectid) { // thanks Thora & Hyun for reporting an issue with homing beacon autoflagging mobs when changing maps
        final List<Pair<BuffStat, Integer>> stat = Collections.singletonList(new Pair<>(BuffStat.HOMING_BEACON, objectid));
        applyto.sendPacket(ChannelPacketCreator.getInstance().giveBuff(1, sourceid, stat));

        final long starttime = Server.getInstance().getCurrentTime();
        applyto.registerEffect(this, starttime, Long.MAX_VALUE, false);
    }

    public void updateBuffEffect(Character target, List<Pair<BuffStat, Integer>> activeStats, long starttime) {
        int localDuration = getBuffLocalDuration();
        long leftDuration = (starttime + localDuration) - Server.getInstance().getCurrentTime();
        if (leftDuration > 0) {
            target.sendPacket(ChannelPacketCreator.getInstance().giveBuff((skill ? sourceid : -sourceid), (int) leftDuration, activeStats));
        }
    }

    private void applyBuffEffect(Character applyfrom, Character applyto, boolean primary) {
        if (!isMonsterRiding() && !isCouponBuff()) {
            applyto.cancelEffect(this, true, -1);
        }

        List<Pair<BuffStat, Integer>> localstatups = statups;
        int localDuration = getBuffLocalDuration();
        int localsourceid = sourceid;
        int seconds = localDuration / 1000;
        Mount givemount = null;
        if (isMonsterRiding()) {
            int ridingMountId = 0;
            Item mount = applyfrom.getInventory(InventoryType.EQUIPPED).getItem((short) -18);
            if (mount != null) {
                ridingMountId = mount.getItemId();
            }

            // thanks inhyuk for noticing some skill mounts not acting properly for other players when changing maps
            givemount = applyto.mount(ridingMountId, sourceid);
            applyto.getClient().getWorldServer().registerMountHunger(applyto);

            localDuration = sourceid;
            localsourceid = ridingMountId;
            localstatups = Collections.singletonList(new Pair<>(BuffStat.MONSTER_RIDING, 0));
        }
        if (primary) {
            applyto.getMap().broadcastMessage(applyto, ChannelPacketCreator.getInstance().showBuffEffect(applyto.getId(), sourceid, 1, (byte) 3), false);
        }
        if (localstatups.size() > 0) {
            Packet buff = null;
            Packet mbuff = null;
            if (this.isActive(applyto)) {
                buff = ChannelPacketCreator.getInstance().giveBuff((skill ? sourceid : -sourceid), localDuration, localstatups);
            }
            if (isMonsterRiding()) {
                buff = ChannelPacketCreator.getInstance().giveBuff(localsourceid, localDuration, localstatups);
                mbuff = ChannelPacketCreator.getInstance().showMonsterRiding(applyto.getId(), givemount);
                localDuration = duration;
            }

            if (buff != null) {
                //Thanks flav for such a simple release! :)
                //Thanks Conrad, Atoot for noticing summons not using buff icon

                applyto.sendPacket(buff);
            }

            long starttime = Server.getInstance().getCurrentTime();
            applyto.registerEffect(this, starttime, starttime + localDuration, false);
            if (mbuff != null) {
                applyto.getMap().broadcastMessage(applyto, mbuff, false);
            }
        }
    }

    private int calcHPChange(Character applyfrom, boolean primary) {
        int hpchange = 0;
        if (hp != 0) {
            if (!skill) {
                hpchange += hp;

                if (applyfrom.hasDisease(Disease.ZOMBIFY)) {
                    hpchange /= 2;
                }
            } else { // assumption: this is heal
                float hpHeal = (applyfrom.getCurrentMaxHp() * (float) hp / (100.0f));
                hpchange += hpHeal;
                if (applyfrom.hasDisease(Disease.ZOMBIFY)) {
                    hpchange = -hpchange;
                    hpCon = 0;
                }
            }
        }
        if (hpR != 0) {
            hpchange += (int) (applyfrom.getCurrentMaxHp() * hpR) / (applyfrom.hasDisease(Disease.ZOMBIFY) ? 2 : 1);
        }
        if (primary) {
            if (hpCon != 0) {
                hpchange -= hpCon;
            }
        }
        if (sourceid == SuperGM.HEAL_PLUS_DISPEL) {
            hpchange += applyfrom.getCurrentMaxHp();
        }

        return hpchange;
    }

    private int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
        return (int) ((Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1)) + (int) (stat * lowerfactor * rate));
    }

    private int calcMPChange(Character applyfrom, boolean primary) {
        int mpchange = 0;
        if (mp != 0) {
            mpchange += mp;
        }
        if (mpR != 0) {
            mpchange += (int) (applyfrom.getCurrentMaxMp() * mpR);
        }
        if (primary) {
            if (mpCon != 0) {
                double mod = 1.0;
                mpchange -= mpCon * mod;
            }
        }
        if (sourceid == SuperGM.HEAL_PLUS_DISPEL) {
            mpchange += applyfrom.getCurrentMaxMp();
        }

        return mpchange;
    }

    private boolean isGmBuff() {
        return switch (sourceid) {
            case Beginner.ECHO_OF_HERO, SuperGM.HEAL_PLUS_DISPEL, SuperGM.HASTE, SuperGM.HOLY_SYMBOL, SuperGM.BLESS,
                 SuperGM.RESURRECTION, SuperGM.HYPER_BODY -> true;
            default -> false;
        };
    }

    private boolean isMonsterBuff() {
        if (!skill) {
            return false;
        }

        return sourceid == SuperGM.HEAL_PLUS_DISPEL;
    }

    private boolean isHeal() {
        return sourceid == SuperGM.HEAL_PLUS_DISPEL;
    }

    private boolean isResurrection() {
        return sourceid == GM.RESURRECTION || sourceid == SuperGM.RESURRECTION;
    }

    public boolean isRecovery() {
        return sourceid == Beginner.RECOVERY;
    }

    public boolean isMapChair() {
        return sourceid == Beginner.MAP_CHAIR;
    }

    private boolean isCouponBuff() {
        return ItemId.isRateCoupon(sourceid);
    }

    public boolean isMonsterRiding() {
        return skill && (
                sourceid % 10000000 == 1004 ||
                        sourceid == Beginner.SPACESHIP ||
                        sourceid == Beginner.YETI_MOUNT1 ||
                        sourceid == Beginner.YETI_MOUNT2 ||
                        sourceid == Beginner.WITCH_BROOMSTICK ||
                        sourceid == Beginner.BALROG_MOUNT
        );

    }

    public boolean isMorph() {
        return morphId > 0;
    }

    public boolean isMorphWithoutAttack() {
        return morphId > 0 && morphId < 100; // Every morph item I have found has been under 100, pirate skill transforms start at 1000.
    }


    private boolean isDispel() {
        return skill && (sourceid == SuperGM.HEAL_PLUS_DISPEL);
    }

    private boolean isCureAllAbnormalStatus() {
        return sourceid == ItemId.WHITE_ELIXIR;
    }


    private boolean isHyperBody() {
        return skill && (sourceid == GM.HYPER_BODY || sourceid == SuperGM.HYPER_BODY);
    }

    private int getFatigue() {
        return fatigue;
    }

    private int getMorph() {
        return morphId;
    }

    private int getMorph(Character chr) {
        if (morphId == 1000 || morphId == 1001 || morphId == 1003) { // morph skill
            return chr.getGender() == 0 ? morphId : morphId + 100;
        }
        return morphId;
    }

    public boolean isSkill() {
        return skill;
    }

    public int getSourceId() {
        return sourceid;
    }

    public int getBuffSourceId() {
        return skill ? sourceid : -sourceid;
    }

    public boolean makeChanceResult() {
        return prop == 1.0 || Math.random() < prop;
    }

    public short getHp() {
        return hp;
    }

    public short getMp() {
        return mp;
    }

    public double getHpRate() {
        return hpR;
    }

    public double getMpRate() {
        return mpR;
    }

    public byte getHpR() {
        return mhpR;
    }

    public byte getMpR() {
        return mmpR;
    }

    public short getHpRRate() {
        return mhpRRate;
    }

    public short getMpRRate() {
        return mmpRRate;
    }

    public short getHpCon() {
        return hpCon;
    }

    public short getMpCon() {
        return mpCon;
    }

    public short getMatk() {
        return matk;
    }

    public short getWatk() {
        return watk;
    }

    public int getDuration() {
        return duration;
    }

    public List<Pair<BuffStat, Integer>> getStatups() {
        return statups;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }

    public int getAttackCount() {
        return attackCount;
    }

    public int getMobCount() {
        return mobCount;
    }

    public int getFixDamage() {
        return fixdamage;
    }

    public short getBulletCount() {
        return bulletCount;
    }

    public short getBulletConsume() {
        return bulletConsume;
    }

    public int getMoneyCon() {
        return moneyCon;
    }

    public int getCooldown() {
        return cooldown;
    }

    public Map<MonsterStatus, Integer> getMonsterStati() {
        return monsterStatus;
    }

    private static class CardItemupStats {
        private final List<Pair<Integer, Integer>> areas;
        protected int itemCode, prob;
        protected boolean party;

        private CardItemupStats(int code, int prob, List<Pair<Integer, Integer>> areas, boolean inParty) {
            this.itemCode = code;
            this.prob = prob;
            this.areas = areas;
            this.party = inParty;
        }

        private boolean isInArea(int mapid) {
            if (this.areas == null) {
                return true;
            }

            for (Pair<Integer, Integer> a : this.areas) {
                if (mapid >= a.left && mapid <= a.right) {
                    return true;
                }
            }

            return false;
        }
    }
}
