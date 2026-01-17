package dev.jaczerob.delfino.maplestory.constants.game;

import dev.jaczerob.delfino.maplestory.client.Disease;
import dev.jaczerob.delfino.maplestory.client.Job;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.id.MapId;
import dev.jaczerob.delfino.maplestory.provider.Data;
import dev.jaczerob.delfino.maplestory.provider.DataDirectoryEntry;
import dev.jaczerob.delfino.maplestory.provider.DataFileEntry;
import dev.jaczerob.delfino.maplestory.provider.DataProvider;
import dev.jaczerob.delfino.maplestory.provider.DataProviderFactory;
import dev.jaczerob.delfino.maplestory.provider.DataTool;
import dev.jaczerob.delfino.maplestory.provider.wz.WZFiles;
import dev.jaczerob.delfino.maplestory.server.quest.Quest;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class GameConstants {
    public static final String[] stats = {"tuc", "reqLevel", "reqJob", "reqSTR", "reqDEX", "reqINT", "reqLUK", "reqPOP", "cash", "cursed", "success", "setItemID", "equipTradeBlock", "durability", "randOption", "randStat", "masterLevel", "reqSkillLevel", "elemDefault", "incRMAS", "incRMAF", "incRMAI", "incRMAL", "canLevel", "skill", "charmEXP"};
    public static final int[] CASH_DATA = new int[]{50200004, 50200069, 50200117, 50100008, 50000047};
    public static final Disease[] CPQ_DISEASES = {Disease.SLOW, Disease.SEDUCE, Disease.STUN, Disease.POISON,
            Disease.SEAL, Disease.DARKNESS, Disease.WEAKEN, Disease.CURSE};
    public static final int MAX_FIELD_MOB_DAMAGE = getMaxObstacleMobDamageFromWz() * 2;
    private static final int[] DROP_RATE_GAIN = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
    private static final int[] MESO_RATE_GAIN = {1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 66, 78, 91, 105};
    private static final int[] EXP_RATE_GAIN = {1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610};    //fibonacci :3
    private static final int[] jobUpgradeBlob = {1, 20, 60, 110, 190};
    private static final int[] jobUpgradeSpUp = {0, 1, 2, 3, 6};
    private final static NumberFormat nfFormatter = new DecimalFormat("#,###,###,###");
    private static final int[] mobHpVal = {0, 15, 20, 25, 35, 50, 65, 80, 95, 110, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350,
            375, 405, 435, 465, 495, 525, 580, 650, 720, 790, 900, 990, 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800,
            1900, 2000, 2100, 2200, 2300, 2400, 2520, 2640, 2760, 2880, 3000, 3200, 3400, 3600, 3800, 4000, 4300, 4600, 4900, 5200,
            5500, 5900, 6300, 6700, 7100, 7500, 8000, 8500, 9000, 9500, 10000, 11000, 12000, 13000, 14000, 15000, 17000, 19000, 21000, 23000,
            25000, 27000, 29000, 31000, 33000, 35000, 37000, 39000, 41000, 43000, 45000, 47000, 49000, 51000, 53000, 55000, 57000, 59000, 61000, 63000,
            65000, 67000, 69000, 71000, 73000, 75000, 77000, 79000, 81000, 83000, 85000, 89000, 91000, 93000, 95000, 97000, 99000, 101000, 103000,
            105000, 107000, 109000, 111000, 113000, 115000, 118000, 120000, 125000, 130000, 135000, 140000, 145000, 150000, 155000, 160000, 165000, 170000, 175000, 180000,
            185000, 190000, 195000, 200000, 205000, 210000, 215000, 220000, 225000, 230000, 235000, 240000, 250000, 260000, 270000, 280000, 290000, 300000, 310000, 320000,
            330000, 340000, 350000, 360000, 370000, 380000, 390000, 400000, 410000, 420000, 430000, 440000, 450000, 460000, 470000, 480000, 490000, 500000, 510000, 520000,
            530000, 550000, 570000, 590000, 610000, 630000, 650000, 670000, 690000, 710000, 730000, 750000, 770000, 790000, 810000, 830000, 850000, 870000, 890000, 910000};
    public static String[] WORLD_NAMES = {"Scania", "Bera", "Broa", "Windia", "Khaini", "Bellocan", "Mardia", "Kradia", "Yellonde", "Demethos", "Galicia", "El Nido", "Zenith", "Arcenia", "Kastia", "Judis", "Plana", "Kalluna", "Stius", "Croa", "Medere"};

    public static int getPlayerBonusDropRate(int slot) {
        return (DROP_RATE_GAIN[slot]);
    }

    public static int getPlayerBonusMesoRate(int slot) {
        return (MESO_RATE_GAIN[slot]);
    }

    public static int getPlayerBonusExpRate(int slot) {
        return (EXP_RATE_GAIN[slot]);
    }

    public static int getJobUpgradeLevelRange(int jobbranch) {
        return jobUpgradeBlob[jobbranch];
    }

    public static int getChangeJobSpUpgrade(int jobbranch) {
        return jobUpgradeSpUp[jobbranch];
    }

    public static boolean isHallOfFameMap(int mapid) {
        switch (mapid) {
            case MapId.HALL_OF_WARRIORS:     // warrior
            case MapId.HALL_OF_MAGICIANS:     // magician
            case MapId.HALL_OF_BOWMEN:     // bowman
            case MapId.HALL_OF_THIEVES:     // thief
            case MapId.NAUTILUS_TRAINING_ROOM:     // pirate
            case MapId.KNIGHTS_CHAMBER:     // cygnus
            case MapId.KNIGHTS_CHAMBER_LARGE:     // other cygnus
            case MapId.KNIGHTS_CHAMBER_2:     // cygnus 2nd floor
            case MapId.KNIGHTS_CHAMBER_3:     // cygnus 3rd floor (beginners)
            case MapId.PALACE_OF_THE_MASTER:     // aran
                return true;

            default:
                return false;
        }
    }

    public static boolean isPodiumHallOfFameMap(int mapid) {
        switch (mapid) {
            case MapId.HALL_OF_WARRIORS:
            case MapId.HALL_OF_MAGICIANS:     // magician
            case MapId.HALL_OF_BOWMEN:     // bowman
            case MapId.HALL_OF_THIEVES:     // thief
            case MapId.NAUTILUS_TRAINING_ROOM:     // pirate
                return true;

            default:
                return false;
        }
    }

    public static byte getHallOfFameBranch(Job job, int mapid) {
        if (!isHallOfFameMap(mapid)) {
            return (byte) (26 + 4 * (mapid / 100000000));   // custom, 400 pnpcs available per continent
        }

        if (job.isA(Job.BEGINNER)) {
            return 22;
        } else {
            return 25;
        }
    }

    public static int getJobBranch(Job job) {
        int jobid = job.getId();

        if (jobid % 1000 == 0) {
            return 0;
        } else if (jobid % 100 == 0) {
            return 1;
        } else {
            return 2 + (jobid % 10);
        }
    }

    public static int getJobMaxLevel(Job job) {
        int jobBranch = getJobBranch(job);

        switch (jobBranch) {
            case 0:
                return 10;   // beginner

            case 1:
                return 30;   // 1st job

            case 2:
                return 70;   // 2nd job

            case 3:
                return 120;   // 3rd job

            default:
                return (job.getId() / 1000 == 1) ? 120 : 200;   // 4th job: cygnus is 120, rest is 200
        }
    }

    public static int getSkillBook(final int job) {
        if (job >= 2210 && job <= 2218) {
            return job - 2209;
        }
        return 0;
    }

    public static boolean isHiddenSkills(final int skill) {
        return false;
    }

    private static boolean isInBranchJobTree(int skillJobId, int jobId, int branchType) {
        int branch = (int) (Math.pow(10, branchType));

        int skillBranch = (skillJobId / branch) * branch;
        int jobBranch = (jobId / branch) * branch;

        return skillBranch == jobBranch;
    }

    private static boolean hasDivergedBranchJobTree(int skillJobId, int jobId, int branchType) {
        int branch = (int) (Math.pow(10, branchType));

        int skillBranch = skillJobId / branch;
        int jobBranch = jobId / branch;

        return skillBranch != jobBranch && skillBranch % 10 != 0;
    }

    public static boolean isInJobTree(int skillId, int jobId) {
        int skillJob = skillId / 10000;

        if (!isInBranchJobTree(skillJob, jobId, 0)) {
            for (int i = 1; i <= 3; i++) {
                if (hasDivergedBranchJobTree(skillJob, jobId, i)) {
                    return false;
                }
                if (isInBranchJobTree(skillJob, jobId, i)) {
                    return (skillJob <= jobId);
                }
            }
        } else {
            return (skillJob <= jobId);
        }

        return false;
    }

    public static boolean isPqSkill(final int skill) {
        return false;
    }

    public static boolean isGMSkills(final int skill) {
        return skill >= 9001000 && skill <= 9101008 || skill >= 8001000 && skill <= 8001001;
    }

    public static boolean isMedalQuest(short questid) {
        return Quest.getInstance(questid).getMedalRequirement() != -1;
    }

    public static int getMonsterHP(final int level) {
        if (level < 0 || level >= mobHpVal.length) {
            return Integer.MAX_VALUE;
        }
        return mobHpVal[level];
    }

    public synchronized static String numberWithCommas(int i) {
        if (!YamlConfig.config.server.USE_DISPLAY_NUMBERS_WITH_COMMA) {
            return nfFormatter.format(i);   // will display number on whatever locale is currently assigned on NumberFormat
        } else {
            return NumberFormat.getNumberInstance(Locale.UK).format(i);
        }
    }

    public synchronized static Number parseNumber(String value) {
        try {
            return NumberFormat.getNumberInstance().parse(value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    private static int getMaxObstacleMobDamageFromWz() {
        DataProvider mapSource = DataProviderFactory.getDataProvider(WZFiles.MAP);
        int maxMobDmg = 0;

        DataDirectoryEntry root = mapSource.getRoot();
        for (DataDirectoryEntry objData : root.getSubdirectories()) {
            if (!objData.getName().contentEquals("Obj")) {
                continue;
            }

            for (DataFileEntry obj : objData.getFiles()) {
                for (Data l0 : mapSource.getData(objData.getName() + "/" + obj.getName()).getChildren()) {
                    for (Data l1 : l0.getChildren()) {
                        for (Data l2 : l1.getChildren()) {
                            int objDmg = DataTool.getIntConvert("s1/mobdamage", l2, 0);
                            if (maxMobDmg < objDmg) {
                                maxMobDmg = objDmg;
                            }
                        }
                    }
                }
            }
        }

        return maxMobDmg;
    }

}
