package dev.jaczerob.delfino.maplestory.client.processor.stat;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.Job;
import dev.jaczerob.delfino.maplestory.client.Stat;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.Equip;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.packets.InPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AssignAPProcessor {

    public static void APAutoAssignAction(InPacket inPacket, Client c) {
        Character chr = c.getPlayer();
        if (chr.getRemainingAp() < 1) {
            return;
        }

        Collection<Item> equippedC = chr.getInventory(InventoryType.EQUIPPED).list();

        c.lockClient();
        try {
            int[] statGain = new int[4];
            int[] statUpdate = new int[4];
            statGain[0] = 0;
            statGain[1] = 0;
            statGain[2] = 0;
            statGain[3] = 0;

            int remainingAp = chr.getRemainingAp();
            inPacket.skip(8);

            if (YamlConfig.config.server.USE_SERVER_AUTOASSIGNER) {
                // --------- Ronan Lana's AUTOASSIGNER ---------
                // This method excels for assigning APs in such a way to cover all equipments AP requirements.
                byte opt = inPacket.readByte();     // useful for pirate autoassigning

                int str = 0, dex = 0, luk = 0, int_ = 0;
                List<Short> eqpStrList = new ArrayList<>();
                List<Short> eqpDexList = new ArrayList<>();
                List<Short> eqpLukList = new ArrayList<>();

                Equip nEquip;

                for (Item item : equippedC) {   //selecting the biggest AP value of each stat from each equipped item.
                    nEquip = (Equip) item;
                    if (nEquip.getStr() > 0) {
                        eqpStrList.add(nEquip.getStr());
                    }
                    str += nEquip.getStr();

                    if (nEquip.getDex() > 0) {
                        eqpDexList.add(nEquip.getDex());
                    }
                    dex += nEquip.getDex();

                    if (nEquip.getLuk() > 0) {
                        eqpLukList.add(nEquip.getLuk());
                    }
                    luk += nEquip.getLuk();

                    //if(nEquip.getInt() > 0) eqpIntList.add(nEquip.getInt()); //not needed...
                    int_ += nEquip.getInt();
                }

                statUpdate[0] = chr.getStr();
                statUpdate[1] = chr.getDex();
                statUpdate[2] = chr.getLuk();
                statUpdate[3] = chr.getInt();

                eqpStrList.sort(Collections.reverseOrder());
                eqpDexList.sort(Collections.reverseOrder());
                eqpLukList.sort(Collections.reverseOrder());

                //Autoassigner looks up the 1st/2nd placed equips for their stats to calculate the optimal upgrade.
                int eqpStr = getNthHighestStat(eqpStrList, (short) 0) + getNthHighestStat(eqpStrList, (short) 1);
                int eqpDex = getNthHighestStat(eqpDexList, (short) 0) + getNthHighestStat(eqpDexList, (short) 1);
                int eqpLuk = getNthHighestStat(eqpLukList, (short) 0) + getNthHighestStat(eqpLukList, (short) 1);

                //c.getPlayer().message("----------------------------------------");
                //c.getPlayer().message("SDL: s" + eqpStr + " d" + eqpDex + " l" + eqpLuk + " BASE STATS --> STR: " + chr.getStr() + " DEX: " + chr.getDex() + " INT: " + chr.getInt() + " LUK: " + chr.getLuk());
                //c.getPlayer().message("SUM EQUIP STATS -> STR: " + str + " DEX: " + dex + " LUK: " + luk + " INT: " + int_);

                Job stance = c.getPlayer().getJobStyle(opt);
                int prStat = 0, scStat = 0, trStat = 0, temp, tempAp = remainingAp, CAP;
                if (tempAp < 1) {
                    return;
                }

                Stat primary, secondary, tertiary = Stat.LUK;
                CAP = 300;

                boolean highDex = false;    // thanks lucasziron & Vcoc for finding out DEX autoassigning poorly for STR-based characters
                if (chr.getLevel() < 40) {
                    if (chr.getDex() >= (2 * chr.getLevel()) + 2) {
                        highDex = true;
                    }
                } else {
                    if (chr.getDex() >= chr.getLevel() + 42) {
                        highDex = true;
                    }
                }

                // other classes will start favoring more DEX only if a level-based threshold is reached.
                if (!highDex) {
                    scStat = 0;
                    if (chr.getDex() < 80) {
                        scStat = (2 * chr.getLevel()) - (chr.getDex() + dex - eqpDex);
                        if (scStat < 0) {
                            scStat = 0;
                        }

                        scStat = Math.min(80 - chr.getDex(), scStat);
                        scStat = Math.min(tempAp, scStat);
                        tempAp -= scStat;
                    }

                    temp = (chr.getLevel() + 40) - Math.max(80, scStat + chr.getDex() + dex - eqpDex);
                    if (temp < 0) {
                        temp = 0;
                    }
                    temp = Math.min(tempAp, temp);
                    scStat += temp;
                    tempAp -= temp;
                } else {
                    scStat = 0;
                    if (chr.getDex() < 96) {
                        scStat = (int) (2.4 * chr.getLevel()) - (chr.getDex() + dex - eqpDex);
                        if (scStat < 0) {
                            scStat = 0;
                        }

                        scStat = Math.min(96 - chr.getDex(), scStat);
                        scStat = Math.min(tempAp, scStat);
                        tempAp -= scStat;
                    }

                    temp = 96 + (int) (1.2 * (chr.getLevel() - 40)) - Math.max(96, scStat + chr.getDex() + dex - eqpDex);
                    if (temp < 0) {
                        temp = 0;
                    }
                    temp = Math.min(tempAp, temp);
                    scStat += temp;
                    tempAp -= temp;
                }

                prStat = tempAp;
                str = prStat;
                dex = scStat;
                int_ = 0;
                luk = 0;

                if (YamlConfig.config.server.USE_AUTOASSIGN_SECONDARY_CAP && dex + chr.getDex() > CAP) {
                    temp = dex + chr.getDex() - CAP;
                    scStat -= temp;
                    prStat += temp;
                }

                primary = Stat.STR;
                secondary = Stat.DEX;

                //-------------------------------------------------------------------------------------

                int extras = 0;

                extras = gainStatByType(primary, statGain, prStat + extras, statUpdate);
                extras = gainStatByType(secondary, statGain, scStat + extras, statUpdate);
                extras = gainStatByType(tertiary, statGain, trStat + extras, statUpdate);

                if (extras > 0) {    //redistribute surplus in priority order
                    extras = gainStatByType(primary, statGain, extras, statUpdate);
                    extras = gainStatByType(secondary, statGain, extras, statUpdate);
                    extras = gainStatByType(tertiary, statGain, extras, statUpdate);
                    gainStatByType(getQuaternaryStat(stance), statGain, extras, statUpdate);
                }

                chr.assignStrDexIntLuk(statGain[0], statGain[1], statGain[3], statGain[2]);
                c.sendPacket(ChannelPacketCreator.getInstance().enableActions());

                //----------------------------------------------------------------------------------------

                c.sendPacket(ChannelPacketCreator.getInstance().serverNotice(1, "Better AP applications detected:\r\nSTR: +" + statGain[0] + "\r\nDEX: +" + statGain[1] + "\r\nINT: +" + statGain[3] + "\r\nLUK: +" + statGain[2]));
            } else {
                if (inPacket.available() < 16) {
                    AutobanFactory.PACKET_EDIT.alert(chr, "Didn't send full packet for Auto Assign.");

                    c.disconnect(true, false);
                    return;
                }

                for (int i = 0; i < 2; i++) {
                    int type = inPacket.readInt();
                    int tempVal = inPacket.readInt();
                    if (tempVal < 0 || tempVal > remainingAp) {
                        return;
                    }

                    gainStatByType(Stat.getBy5ByteEncoding(type), statGain, tempVal, statUpdate);
                }

                chr.assignStrDexIntLuk(statGain[0], statGain[1], statGain[3], statGain[2]);
                c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            }
        } finally {
            c.unlockClient();
        }
    }

    private static int getNthHighestStat(List<Short> statList, short rank) {    // ranks from 0
        return (statList.size() <= rank ? 0 : statList.get(rank));
    }

    private static int gainStatByType(Stat type, int[] statGain, int gain, int[] statUpdate) {
        if (gain <= 0) {
            return 0;
        }

        int newVal = 0;
        switch (type) {
            case STR:
                newVal = statUpdate[0] + gain;
                if (newVal > YamlConfig.config.server.MAX_AP) {
                    statGain[0] += (gain - (newVal - YamlConfig.config.server.MAX_AP));
                    statUpdate[0] = YamlConfig.config.server.MAX_AP;
                } else {
                    statGain[0] += gain;
                    statUpdate[0] = newVal;
                }
                break;
            case INT:
                newVal = statUpdate[3] + gain;
                if (newVal > YamlConfig.config.server.MAX_AP) {
                    statGain[3] += (gain - (newVal - YamlConfig.config.server.MAX_AP));
                    statUpdate[3] = YamlConfig.config.server.MAX_AP;
                } else {
                    statGain[3] += gain;
                    statUpdate[3] = newVal;
                }
                break;
            case LUK:
                newVal = statUpdate[2] + gain;
                if (newVal > YamlConfig.config.server.MAX_AP) {
                    statGain[2] += (gain - (newVal - YamlConfig.config.server.MAX_AP));
                    statUpdate[2] = YamlConfig.config.server.MAX_AP;
                } else {
                    statGain[2] += gain;
                    statUpdate[2] = newVal;
                }
                break;
            case DEX:
                newVal = statUpdate[1] + gain;
                if (newVal > YamlConfig.config.server.MAX_AP) {
                    statGain[1] += (gain - (newVal - YamlConfig.config.server.MAX_AP));
                    statUpdate[1] = YamlConfig.config.server.MAX_AP;
                } else {
                    statGain[1] += gain;
                    statUpdate[1] = newVal;
                }
                break;
        }

        if (newVal > YamlConfig.config.server.MAX_AP) {
            return newVal - YamlConfig.config.server.MAX_AP;
        }
        return 0;
    }

    private static Stat getQuaternaryStat(Job stance) {
        return Stat.STR;
    }

    public static boolean APResetAction(Client c, int APFrom, int APTo) {
        c.lockClient();
        try {
            Character player = c.getPlayer();

            switch (APFrom) {
                case 64: // str
                    if (player.getStr() < 5) {
                        player.message("You don't have the minimum STR required to swap.");
                        c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return false;
                    }
                    if (!player.assignStr(-1)) {
                        player.message("Couldn't execute AP reset operation.");
                        c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return false;
                    }
                    break;
                case 128: // dex
                    if (player.getDex() < 5) {
                        player.message("You don't have the minimum DEX required to swap.");
                        c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return false;
                    }
                    if (!player.assignDex(-1)) {
                        player.message("Couldn't execute AP reset operation.");
                        c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return false;
                    }
                    break;
                case 256: // int
                    if (player.getInt() < 5) {
                        player.message("You don't have the minimum INT required to swap.");
                        c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return false;
                    }
                    if (!player.assignInt(-1)) {
                        player.message("Couldn't execute AP reset operation.");
                        c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return false;
                    }
                    break;
                case 512: // luk
                    if (player.getLuk() < 5) {
                        player.message("You don't have the minimum LUK required to swap.");
                        c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return false;
                    }
                    if (!player.assignLuk(-1)) {
                        player.message("Couldn't execute AP reset operation.");
                        c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return false;
                    }
                    break;
                case 2048: // HP
                    if (YamlConfig.config.server.USE_ENFORCE_HPMP_SWAP) {
                        if (APTo != 8192) {
                            player.message("You can only swap HP ability points to MP.");
                            c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                            return false;
                        }
                    }

                    if (player.getHpMpApUsed() < 1) {
                        player.message("You don't have enough HPMP stat points to spend on AP Reset.");
                        c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return false;
                    }

                    int hplose = -takeHp(player.getJob());
                    if (player.getMaxHp() + hplose < getMinHp(player.getJob(), player.getLevel())) {
                        player.message("You don't have the minimum HP pool required to swap.");
                        c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return false;
                    }

                    int curHp = player.getHp();
                    player.assignHP(hplose, -1);
                    if (!YamlConfig.config.server.USE_FIXED_RATIO_HPMP_UPDATE) {
                        player.updateHp(Math.max(1, curHp + hplose));
                    }

                    break;
                case 8192: // MP
                    if (YamlConfig.config.server.USE_ENFORCE_HPMP_SWAP) {
                        if (APTo != 2048) {
                            player.message("You can only swap MP ability points to HP.");
                            c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                            return false;
                        }
                    }

                    if (player.getHpMpApUsed() < 1) {
                        player.message("You don't have enough HPMP stat points to spend on AP Reset.");
                        c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return false;
                    }

                    int mplose = -takeMp(player.getJob());
                    if (player.getMaxMp() + mplose < getMinMp(player.getJob(), player.getLevel())) {
                        player.message("You don't have the minimum MP pool required to swap.");
                        c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return false;
                    }

                    int curMp = player.getMp();
                    player.assignMP(mplose, -1);
                    if (!YamlConfig.config.server.USE_FIXED_RATIO_HPMP_UPDATE) {
                        player.updateMp(Math.max(0, curMp + mplose));
                    }
                    break;
                default:
                    c.sendPacket(ChannelPacketCreator.getInstance().updatePlayerStats(ChannelPacketCreator.EMPTY_STATUPDATE, true, player));
                    return false;
            }

            addStat(player, APTo, true);
            return true;
        } finally {
            c.unlockClient();
        }
    }

    public static void APAssignAction(Client c, int num) {
        c.lockClient();
        try {
            addStat(c.getPlayer(), num, false);
        } finally {
            c.unlockClient();
        }
    }

    private static boolean addStat(Character chr, int apTo, boolean usedAPReset) {
        switch (apTo) {
            case 64:
                if (!chr.assignStr(1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return false;
                }
                break;
            case 128: // Dex
                if (!chr.assignDex(1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return false;
                }
                break;
            case 256: // Int
                if (!chr.assignInt(1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return false;
                }
                break;
            case 512: // Luk
                if (!chr.assignLuk(1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return false;
                }
                break;
            case 2048:
                if (!chr.assignHP(calcHpChange(chr, usedAPReset), 1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return false;
                }
                break;
            case 8192:
                if (!chr.assignMP(calcMpChange(chr, usedAPReset), 1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return false;
                }
                break;
            default:
                chr.sendPacket(ChannelPacketCreator.getInstance().updatePlayerStats(ChannelPacketCreator.EMPTY_STATUPDATE, true, chr));
                return false;
        }
        return true;
    }

    private static int calcHpChange(Character player, boolean usedAPReset) {
        return 10;
    }

    private static int calcMpChange(Character player, boolean usedAPReset) {
        return 6;
    }

    private static int takeHp(Job job) {
        return 12;
    }

    private static int takeMp(Job job) {
        return 8;
    }

    public static int getMinHp(Job job, int level) {
        return (12 * level) + 38;
    }

    public static int getMinMp(Job job, int level) {
        return (10 * level) + -5;
    }
}
