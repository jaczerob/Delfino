package dev.jaczerob.delfino.maplestory.client;

import dev.jaczerob.delfino.maplestory.client.autoban.AutobanManager;
import dev.jaczerob.delfino.maplestory.client.inventory.Equip;
import dev.jaczerob.delfino.maplestory.client.inventory.Inventory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryProof;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.ItemFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.ModifyInventory;
import dev.jaczerob.delfino.maplestory.client.inventory.Pet;
import dev.jaczerob.delfino.maplestory.client.inventory.PetDataFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.WeaponType;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.client.keybind.KeyBinding;
import dev.jaczerob.delfino.maplestory.client.keybind.QuickslotBinding;
import dev.jaczerob.delfino.maplestory.client.processor.action.PetAutopotProcessor;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.game.ExpTable;
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.constants.id.MapId;
import dev.jaczerob.delfino.maplestory.constants.id.MobId;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.constants.skills.Beginner;
import dev.jaczerob.delfino.maplestory.net.server.PlayerBuffValueHolder;
import dev.jaczerob.delfino.maplestory.net.server.PlayerCoolDownValueHolder;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator;
import dev.jaczerob.delfino.maplestory.net.server.services.task.world.CharacterSaveService;
import dev.jaczerob.delfino.maplestory.net.server.services.type.WorldServices;
import dev.jaczerob.delfino.maplestory.net.server.world.Messenger;
import dev.jaczerob.delfino.maplestory.net.server.world.MessengerCharacter;
import dev.jaczerob.delfino.maplestory.net.server.world.Party;
import dev.jaczerob.delfino.maplestory.net.server.world.PartyCharacter;
import dev.jaczerob.delfino.maplestory.net.server.world.PartyOperation;
import dev.jaczerob.delfino.maplestory.net.server.world.World;
import dev.jaczerob.delfino.maplestory.server.CashShop;
import dev.jaczerob.delfino.maplestory.server.ExpLogger;
import dev.jaczerob.delfino.maplestory.server.ExpLogger.ExpLogRecord;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider.ScriptedItem;
import dev.jaczerob.delfino.maplestory.server.Shop;
import dev.jaczerob.delfino.maplestory.server.StatEffect;
import dev.jaczerob.delfino.maplestory.server.Storage;
import dev.jaczerob.delfino.maplestory.server.TimerManager;
import dev.jaczerob.delfino.maplestory.server.Trade;
import dev.jaczerob.delfino.maplestory.server.life.BanishInfo;
import dev.jaczerob.delfino.maplestory.server.life.MobSkill;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillFactory;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillId;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillType;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.server.maps.AbstractAnimatedMapObject;
import dev.jaczerob.delfino.maplestory.server.maps.Door;
import dev.jaczerob.delfino.maplestory.server.maps.DoorObject;
import dev.jaczerob.delfino.maplestory.server.maps.MapEffect;
import dev.jaczerob.delfino.maplestory.server.maps.MapItem;
import dev.jaczerob.delfino.maplestory.server.maps.MapManager;
import dev.jaczerob.delfino.maplestory.server.maps.MapObject;
import dev.jaczerob.delfino.maplestory.server.maps.MapObjectType;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.server.maps.Portal;
import dev.jaczerob.delfino.maplestory.server.maps.SavedLocation;
import dev.jaczerob.delfino.maplestory.server.maps.SavedLocationType;
import dev.jaczerob.delfino.maplestory.server.maps.Summon;
import dev.jaczerob.delfino.maplestory.server.quest.Quest;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;
import dev.jaczerob.delfino.maplestory.tools.LongTool;
import dev.jaczerob.delfino.maplestory.tools.Pair;
import dev.jaczerob.delfino.maplestory.tools.Randomizer;
import dev.jaczerob.delfino.network.packets.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Character extends AbstractCharacterObject {
    private static final Logger log = LoggerFactory.getLogger(Character.class);
    private static final String[] BLOCKED_NAMES = {"admin", "owner", "moderator", "intern", "donor", "administrator", "FREDRICK", "help", "helper", "alert", "notice", "maplestory", "fuck", "wizet", "fucking", "negro", "fuk", "fuc", "penis", "pussy", "asshole", "gay",
            "nigger", "homo", "suck", "cum", "shit", "shitty", "condom", "security", "official", "rape", "nigga", "sex", "tit", "boner", "orgy", "clit", "asshole", "fatass", "bitch", "support", "gamemaster", "cock", "gaay", "gm",
            "operate", "master", "sysop", "party", "GameMaster", "community", "message", "event", "test", "meso", "Scania", "yata", "AsiaSoft", "henesys"};
    private final AtomicBoolean mapTransitioning = new AtomicBoolean(true);  // player client is currently trying to change maps or log in the game map
    private final AtomicBoolean awayFromWorld = new AtomicBoolean(true);  // player is online, but on cash shop or mts
    private final AtomicInteger exp = new AtomicInteger();
    private final AtomicInteger gachaexp = new AtomicInteger();
    private final AtomicInteger meso = new AtomicInteger();
    private final AtomicInteger chair = new AtomicInteger(-1);
    private final Pet[] pets = new Pet[3];
    private final SavedLocation[] savedLocations;
    private final SkillMacro[] skillMacros = new SkillMacro[5];
    private final List<WeakReference<MapleMap>> lastVisitedMaps = new LinkedList<>();
    private final Map<Short, QuestStatus> quests;
    private final Set<Monster> controlled = new LinkedHashSet<>();
    private final Map<Integer, String> entered = new LinkedHashMap<>();
    private final Set<MapObject> visibleMapObjects = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<Skill, SkillEntry> skills = new LinkedHashMap<>();
    private final Map<Integer, Integer> activeCoupons = new LinkedHashMap<>();
    private final Map<Integer, Integer> activeCouponRates = new LinkedHashMap<>();
    private final EnumMap<BuffStat, BuffStatValueHolder> effects = new EnumMap<>(BuffStat.class);
    private final Map<BuffStat, Byte> buffEffectsCount = new LinkedHashMap<>();
    private final Map<Disease, Long> diseaseExpires = new LinkedHashMap<>();
    private final Map<Integer, Map<BuffStat, BuffStatValueHolder>> buffEffects = new LinkedHashMap<>(); // non-overriding buffs thanks to Ronan
    private final Map<Integer, Long> buffExpires = new LinkedHashMap<>();
    private final Map<Integer, KeyBinding> keymap = new LinkedHashMap<>();
    private final Map<Integer, Summon> summons = new LinkedHashMap<>();
    private final Map<Integer, CooldownValueHolder> coolDowns = new LinkedHashMap<>();
    private final EnumMap<Disease, Pair<DiseaseValueHolder, MobSkill>> diseases = new EnumMap<>(Disease.class);
    private final Lock chrLock = new ReentrantLock(true);
    private final Lock evtLock = new ReentrantLock(true);
    private final Lock petLock = new ReentrantLock(true);
    private final Lock prtLock = new ReentrantLock();
    private final Lock cpnLock = new ReentrantLock();
    private final Map<Integer, Set<Integer>> excluded = new LinkedHashMap<>();
    private final Set<Integer> excludedItems = new LinkedHashSet<>();
    private final Set<Integer> disabledPartySearchInvites = new LinkedHashSet<>();
    private final List<String> blockedPortals = new ArrayList<>();
    private final Map<Short, String> area_info = new LinkedHashMap<>();
    private final List<Integer> trockmaps = new ArrayList<>();
    private final List<Integer> viptrockmaps = new ArrayList<>();
    private final List<Pair<DelayedQuestUpdate, Object[]>> npcUpdateQuests = new LinkedList<>();
    private final boolean usedSafetyCharm = false;
    private final Inventory[] inventory;
    private final Map<Quest, Long> questExpirations = new LinkedHashMap<>();
    private final boolean allowExpGain = true;
    private int world;
    private int accountid, id, level;
    private int rank, rankMove, jobRank, jobRankMove;
    private int gender, hair, face;
    private int fame, quest_fame;
    private int initialSpawnPoint;
    private int mapid;
    private int currentPage, currentType = 0, currentTab = 1;
    private int itemEffect;
    private int guildid, guildRank, allianceRank;
    private int messengerposition = 4;
    private int slots = 0;
    private int energybar;
    private int gmLevel;
    private int ci = 0;
    private int familyId;
    private int bookCover;
    private int mesosTraded = 0;
    private int possibleReports = 10;
    private int expRate = 1, mesoRate = 1, dropRate = 1, expCoupon = 1, mesoCoupon = 1, dropCoupon = 1;
    private long lastfametime, lastUsedCashItem, lastExpression = 0, lastHealed, lastDeathtime, jailExpiration = -1;
    private transient int localstr, localdex, localluk, localint_, localmagic, localwatk;
    private transient int equipmaxhp, equipmaxmp, equipstr, equipdex, equipluk, equipint_, equipmagic, equipwatk, localchairhp, localchairmp;
    private int localchairrate;
    private boolean hidden, equipchanged = true, berserk, hasMerchant, hasSandboxItem = false, whiteChat = false, canRecvPartySearchInvite = true;
    private boolean equippedMesoMagnet = false, equippedItemPouch = false, equippedPetItemIgnore = false;
    private float autopotHpAlert, autopotMpAlert;
    private int linkedLevel = 0;
    private String linkedName = null;
    private boolean finishedDojoTutorial;
    private boolean usedStorage = false;
    private String name;
    private String chalktext;
    private String commandtext;
    private String dataString;
    private String search = null;
    private long totalExpGained = 0;
    private BuddyList buddylist;
    private Client client;
    private PartyCharacter mpc = null;
    private Job job = Job.BEGINNER;
    private Messenger messenger = null;
    private Mount maplemount;
    private Party party;
    private Shop shop = null;
    private SkinColor skinColor = SkinColor.LIGHT;
    private Storage storage = null;
    private Trade trade = null;
    private MonsterBook monsterbook;
    private CashShop cashshop;
    private List<Integer> lastmonthfameids;
    private byte[] m_aQuickslotLoaded;
    private QuickslotBinding m_pQuickslotKeyMapped;
    private Door pdoor = null;
    private ScheduledFuture<?> dragonBloodSchedule;
    private ScheduledFuture<?> beholderHealingSchedule, beholderBuffSchedule, berserkSchedule;
    private ScheduledFuture<?> skillCooldownTask = null;
    private ScheduledFuture<?> buffExpireTask = null;
    private ScheduledFuture<?> itemExpireTask = null;
    private ScheduledFuture<?> diseaseExpireTask = null;
    private ScheduledFuture<?> questExpireTask = null;
    private ScheduledFuture<?> recoveryTask = null;
    private ScheduledFuture<?> extraRecoveryTask = null;
    private ScheduledFuture<?> chairRecoveryTask = null;
    private ScheduledFuture<?> pendantOfSpirit = null; //1122017
    private long portaldelay = 0;
    private AutobanManager autoban;
    private boolean isbanned = false;
    private boolean blockCashShop = false;
    private byte pendantExp = 0;
    private byte doorSlot = -1;
    private boolean loggedIn = false;
    private boolean useCS;  //chaos scroll upon crafting item.
    private long npcCd;
    private int newWarpMap = -1;
    private boolean canWarpMap = true;  //only one "warp" must be used per call, and this will define the right one.
    private int canWarpCounter = 0;     //counts how many times "inner warps" have been called.
    private byte extraHpRec = 0, extraMpRec = 0;
    private short extraRecInterval;
    private int targetHpBarHash = 0;
    private long targetHpBarTime = 0;
    private long nextWarningTime = 0;
    private long lastExpGainTime;
    private boolean pendingNameChange; //only used to change name on logout, not to be relied upon elsewhere
    private long loginTime;
    private boolean chasing = false;
    //EVENTS
    private byte team = 0;

    private Character() {
        super.setListener(new AbstractCharacterListener() {
            @Override
            public void onHpChanged(int oldHp) {
                hpChangeAction(oldHp);
            }

            @Override
            public void onHpmpPoolUpdate() {
                List<Pair<Stat, Integer>> hpmpupdate = recalcLocalStats();
                for (Pair<Stat, Integer> p : hpmpupdate) {
                    statUpdates.put(p.getLeft(), p.getRight());
                }

                if (hp > localmaxhp) {
                    setHp(localmaxhp);
                    statUpdates.put(Stat.HP, hp);
                }

                if (mp > localmaxmp) {
                    setMp(localmaxmp);
                    statUpdates.put(Stat.MP, mp);
                }
            }

            @Override
            public void onStatUpdate() {
                recalcLocalStats();
            }

            @Override
            public void onAnnounceStatPoolUpdate() {
                List<Pair<Stat, Integer>> statup = new ArrayList<>(8);
                for (Entry<Stat, Integer> s : statUpdates.entrySet()) {
                    statup.add(new Pair<>(s.getKey(), s.getValue()));
                }

                sendPacket(ChannelPacketCreator.getInstance().updatePlayerStats(statup, true, Character.this));
            }
        });

        useCS = false;

        setStance(0);
        inventory = new Inventory[InventoryType.values().length];
        savedLocations = new SavedLocation[SavedLocationType.values().length];

        for (InventoryType type : InventoryType.values()) {
            byte b = 24;
            if (type == InventoryType.CASH) {
                b = 96;
            }
            inventory[type.ordinal()] = new Inventory(this, type, b);
        }
        inventory[InventoryType.CANHOLD.ordinal()] = new InventoryProof(this);

        for (int i = 0; i < SavedLocationType.values().length; i++) {
            savedLocations[i] = null;
        }
        quests = new LinkedHashMap<>();
        setPosition(new Point(0, 0));
    }

    private static Job getJobStyleInternal(int jobid, byte opt) {
        return Job.BEGINNER;
    }

    public static boolean ban(String id, String reason, boolean accountId) {
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            if (id.matches("/[0-9]{1,3}\\..*")) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)")) {
                    ps.setString(1, id);
                    ps.executeUpdate();
                    return true;
                }
            }

            final String query;
            if (accountId) {
                query = "SELECT id FROM accounts WHERE name = ?";
            } else {
                query = "SELECT accountid FROM characters WHERE name = ?";
            }

            boolean ret = false;
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement ps2 = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?")) {
                            ps2.setString(1, reason);
                            ps2.setInt(2, rs.getInt(1));
                            ps2.executeUpdate();
                        }
                        ret = true;
                    }
                }
            }
            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean canCreateChar(String name) {
        String lname = name.toLowerCase();
        for (String nameTest : BLOCKED_NAMES) {
            if (lname.contains(nameTest)) {
                return false;
            }
        }
        return getIdByName(name) < 0 && Pattern.compile("[a-zA-Z0-9]{3,12}").matcher(name).matches();
    }

    private static void addPartyPlayerDoor(Character target) {
        Door targetDoor = target.getPlayerDoor();
        if (targetDoor != null) {
            target.applyPartyDoor(targetDoor, true);
        }
    }

    private static void removePartyPlayerDoor(Party party, Character target) {
        target.removePartyDoor(party);
    }

    private static void updatePartyTownDoors(Party party, Character target, Character partyLeaver, List<Character> partyMembers) {
        if (partyLeaver != null) {
            removePartyPlayerDoor(party, target);
        } else {
            addPartyPlayerDoor(target);
        }

        Map<Integer, Door> partyDoors = null;
        if (!partyMembers.isEmpty()) {
            partyDoors = party.getDoors();

            for (Character pchr : partyMembers) {
                Door door = partyDoors.get(pchr.getId());
                if (door != null) {
                    door.updateDoorPortal(pchr);
                }
            }

            for (Door door : partyDoors.values()) {
                for (Character pchar : partyMembers) {
                    DoorObject mdo = door.getTownDoor();
                    mdo.sendDestroyData(pchar.getClient(), true);
                    pchar.removeVisibleMapObject(mdo);
                }
            }

            if (partyLeaver != null) {
                Collection<Door> leaverDoors = partyLeaver.getDoors();
                for (Door door : leaverDoors) {
                    for (Character pchar : partyMembers) {
                        DoorObject mdo = door.getTownDoor();
                        mdo.sendDestroyData(pchar.getClient(), true);
                        pchar.removeVisibleMapObject(mdo);
                    }
                }
            }

            List<Integer> histMembers = party.getMembersSortedByHistory();
            for (Integer chrid : histMembers) {
                Door door = partyDoors.get(chrid);

                if (door != null) {
                    for (Character pchar : partyMembers) {
                        DoorObject mdo = door.getTownDoor();
                        mdo.sendSpawnData(pchar.getClient());
                        pchar.addVisibleMapObject(mdo);
                    }
                }
            }
        }

        if (partyLeaver != null) {
            Collection<Door> leaverDoors = partyLeaver.getDoors();

            if (partyDoors != null) {
                for (Door door : partyDoors.values()) {
                    DoorObject mdo = door.getTownDoor();
                    mdo.sendDestroyData(partyLeaver.getClient(), true);
                    partyLeaver.removeVisibleMapObject(mdo);
                }
            }

            for (Door door : leaverDoors) {
                DoorObject mdo = door.getTownDoor();
                mdo.sendDestroyData(partyLeaver.getClient(), true);
                partyLeaver.removeVisibleMapObject(mdo);
            }

            for (Door door : leaverDoors) {
                door.updateDoorPortal(partyLeaver);

                DoorObject mdo = door.getTownDoor();
                mdo.sendSpawnData(partyLeaver.getClient());
                partyLeaver.addVisibleMapObject(mdo);
            }
        }
    }

    private static void deleteQuestProgressWhereCharacterId(Connection con, int cid) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM medalmaps WHERE characterid = ?")) {
            ps.setInt(1, cid);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = con.prepareStatement("DELETE FROM questprogress WHERE characterid = ?")) {
            ps.setInt(1, cid);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = con.prepareStatement("DELETE FROM queststatus WHERE characterid = ?")) {
            ps.setInt(1, cid);
            ps.executeUpdate();
        }
    }

    public static void deleteWhereCharacterId(Connection con, String sql, int cid) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cid);
            ps.executeUpdate();
        }
    }

    private static Pair<Integer, Pair<Integer, Integer>> getChairTaskIntervalRate(int maxhp, int maxmp) {
        float toHeal = Math.max(maxhp, maxmp);
        float maxDuration = SECONDS.toMillis(YamlConfig.config.server.CHAIR_EXTRA_HEAL_MAX_DELAY);

        int rate = 0;
        int minRegen = 1, maxRegen = (256 * YamlConfig.config.server.CHAIR_EXTRA_HEAL_MULTIPLIER) - 1, midRegen = 1;
        while (minRegen < maxRegen) {
            midRegen = (int) ((minRegen + maxRegen) * 0.94);

            float procs = toHeal / midRegen;
            float newRate = maxDuration / procs;
            rate = (int) newRate;

            if (newRate < 420) {
                minRegen = (int) (1.2 * midRegen);
            } else if (newRate > 5000) {
                maxRegen = (int) (0.8 * midRegen);
            } else {
                break;
            }
        }

        float procs = maxDuration / rate;
        int hpRegen, mpRegen;
        if (maxhp > maxmp) {
            hpRegen = midRegen;
            mpRegen = (int) Math.ceil(maxmp / procs);
        } else {
            hpRegen = (int) Math.ceil(maxhp / procs);
            mpRegen = midRegen;
        }

        return new Pair<>(rate, new Pair<>(hpRegen, mpRegen));
    }

    public static Map<String, String> getCharacterFromDatabase(String name) {
        Map<String, String> character = new LinkedHashMap<>();

        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, accountid, name FROM characters WHERE name = ?")) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    character.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return character;
    }

    private static StatEffect getEffectFromBuffSource(Map<BuffStat, BuffStatValueHolder> buffSource) {
        try {
            return buffSource.entrySet().iterator().next().getValue().effect;
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<StatEffect, Integer> topologicalSortLeafStatCount(Map<BuffStat, Stack<StatEffect>> buffStack) {
        Map<StatEffect, Integer> leafBuffCount = new LinkedHashMap<>();

        for (Entry<BuffStat, Stack<StatEffect>> e : buffStack.entrySet()) {
            Stack<StatEffect> mseStack = e.getValue();
            if (mseStack.isEmpty()) {
                continue;
            }

            StatEffect mse = mseStack.peek();
            Integer count = leafBuffCount.get(mse);
            if (count == null) {
                leafBuffCount.put(mse, 1);
            } else {
                leafBuffCount.put(mse, count + 1);
            }
        }

        return leafBuffCount;
    }

    private static List<StatEffect> topologicalSortRemoveLeafStats(Map<StatEffect, Set<BuffStat>> stackedBuffStats, Map<BuffStat, Stack<StatEffect>> buffStack, Map<StatEffect, Integer> leafStatCount) {
        List<StatEffect> clearedStatEffects = new LinkedList<>();
        Set<BuffStat> clearedStats = new LinkedHashSet<>();

        for (Entry<StatEffect, Integer> e : leafStatCount.entrySet()) {
            StatEffect mse = e.getKey();

            if (stackedBuffStats.get(mse).size() <= e.getValue()) {
                clearedStatEffects.add(mse);

                for (BuffStat mbs : stackedBuffStats.get(mse)) {
                    clearedStats.add(mbs);
                }
            }
        }

        for (BuffStat mbs : clearedStats) {
            StatEffect mse = buffStack.get(mbs).pop();
            stackedBuffStats.get(mse).remove(mbs);
        }

        return clearedStatEffects;
    }

    private static void topologicalSortRebaseLeafStats(Map<StatEffect, Set<BuffStat>> stackedBuffStats, Map<BuffStat, Stack<StatEffect>> buffStack) {
        for (Entry<BuffStat, Stack<StatEffect>> e : buffStack.entrySet()) {
            Stack<StatEffect> mseStack = e.getValue();

            if (!mseStack.isEmpty()) {
                StatEffect mse = mseStack.pop();
                stackedBuffStats.get(mse).remove(e.getKey());
            }
        }
    }

    private static List<StatEffect> topologicalSortEffects(Map<BuffStat, List<Pair<StatEffect, Integer>>> buffEffects) {
        Map<StatEffect, Set<BuffStat>> stackedBuffStats = new LinkedHashMap<>();
        Map<BuffStat, Stack<StatEffect>> buffStack = new LinkedHashMap<>();

        for (Entry<BuffStat, List<Pair<StatEffect, Integer>>> e : buffEffects.entrySet()) {
            BuffStat mbs = e.getKey();

            Stack<StatEffect> mbsStack = new Stack<>();
            buffStack.put(mbs, mbsStack);

            for (Pair<StatEffect, Integer> emse : e.getValue()) {
                StatEffect mse = emse.getLeft();
                mbsStack.push(mse);

                Set<BuffStat> mbsStats = stackedBuffStats.get(mse);
                if (mbsStats == null) {
                    mbsStats = new LinkedHashSet<>();
                    stackedBuffStats.put(mse, mbsStats);
                }

                mbsStats.add(mbs);
            }
        }

        List<StatEffect> buffList = new LinkedList<>();
        while (true) {
            Map<StatEffect, Integer> leafStatCount = topologicalSortLeafStatCount(buffStack);
            if (leafStatCount.isEmpty()) {
                break;
            }

            List<StatEffect> clearedNodes = topologicalSortRemoveLeafStats(stackedBuffStats, buffStack, leafStatCount);
            if (clearedNodes.isEmpty()) {
                topologicalSortRebaseLeafStats(stackedBuffStats, buffStack);
            } else {
                buffList.addAll(clearedNodes);
            }
        }

        return buffList;
    }

    private static List<StatEffect> sortEffectsList(Map<StatEffect, Integer> updateEffectsList) {
        Map<BuffStat, List<Pair<StatEffect, Integer>>> buffEffects = new LinkedHashMap<>();

        for (Entry<StatEffect, Integer> p : updateEffectsList.entrySet()) {
            StatEffect mse = p.getKey();

            for (Pair<BuffStat, Integer> statup : mse.getStatups()) {
                BuffStat stat = statup.getLeft();

                List<Pair<StatEffect, Integer>> statBuffs = buffEffects.get(stat);
                if (statBuffs == null) {
                    statBuffs = new ArrayList<>();
                    buffEffects.put(stat, statBuffs);
                }

                statBuffs.add(new Pair<>(mse, statup.getRight()));
            }
        }

        Comparator cmp = new Comparator<Pair<StatEffect, Integer>>() {
            @Override
            public int compare(Pair<StatEffect, Integer> o1, Pair<StatEffect, Integer> o2) {
                return o2.getRight().compareTo(o1.getRight());
            }
        };

        for (Entry<BuffStat, List<Pair<StatEffect, Integer>>> statBuffs : buffEffects.entrySet()) {
            Collections.sort(statBuffs.getValue(), cmp);
        }

        return topologicalSortEffects(buffEffects);
    }

    private static BuffStat getSingletonStatupFromEffect(StatEffect mse) {
        for (Pair<BuffStat, Integer> mbs : mse.getStatups()) {
            if (isSingletonStatup(mbs.getLeft())) {
                return mbs.getLeft();
            }
        }

        return null;
    }

    private static boolean isSingletonStatup(BuffStat mbs) {
        switch (mbs) {           //HPREC and MPREC are supposed to be singleton
            case COUPON_EXP1:
            case COUPON_EXP2:
            case COUPON_EXP3:
            case COUPON_EXP4:
            case COUPON_DRP1:
            case COUPON_DRP2:
            case COUPON_DRP3:
            case MESO_UP_BY_ITEM:
            case ITEM_UP_BY_ITEM:
            case RESPECT_PIMMUNE:
            case RESPECT_MIMMUNE:
            case DEFENSE_ATT:
            case DEFENSE_STATE:
            case WATK:
            case WDEF:
            case MATK:
            case MDEF:
            case ACC:
            case AVOID:
            case SPEED:
            case JUMP:
                return false;

            default:
                return true;
        }
    }

    private static boolean isPriorityBuffSourceid(int sourceid) {
        switch (sourceid) {
            case -ItemId.ROSE_SCENT:
            case -ItemId.FREESIA_SCENT:
            case -ItemId.LAVENDER_SCENT:
                return true;

            default:
                return false;
        }
    }

    private static int getJobMapChair() {
        return Beginner.MAP_CHAIR;
    }

    public static int getIdByName(String name) {
        final int id;
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id FROM characters WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return -1;
                }
                id = rs.getInt("id");
            }
            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getNameById(int id) {
        final String name;
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("SELECT name FROM characters WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                name = rs.getString("name");
            }
            return name;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Character loadCharFromDB(final int charid, Client client, boolean channelserver) throws SQLException {
        Character ret = new Character();
        ret.client = client;
        ret.id = charid;

        try (Connection con = DatabaseConnection.getStaticConnection()) {
            final int mountexp;
            final int mountlevel;
            final int mounttiredness;
            final World wserv;

            // Character info
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?")) {
                ps.setInt(1, charid);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Loading char failed (not found)");
                    }

                    ret.name = rs.getString("name");
                    ret.level = rs.getInt("level");
                    ret.fame = rs.getInt("fame");
                    ret.quest_fame = rs.getInt("fquest");
                    ret.str = rs.getInt("str");
                    ret.dex = rs.getInt("dex");
                    ret.int_ = rs.getInt("int");
                    ret.luk = rs.getInt("luk");
                    ret.exp.set(rs.getInt("exp"));
                    ret.gachaexp.set(rs.getInt("gachaexp"));
                    ret.hp = rs.getInt("hp");
                    ret.setMaxHp(rs.getInt("maxhp"));
                    ret.mp = rs.getInt("mp");
                    ret.setMaxMp(rs.getInt("maxmp"));
                    ret.hpMpApUsed = rs.getInt("hpMpUsed");
                    ret.hasMerchant = rs.getInt("HasMerchant") == 1;
                    ret.remainingAp = rs.getInt("ap");
                    ret.loadCharSkillPoints(rs.getString("sp").split(","));
                    ret.meso.set(rs.getInt("meso"));
                    rs.getInt("MerchantMesos");
                    ret.setGMLevel(rs.getInt("gm"));
                    ret.skinColor = SkinColor.getById(rs.getInt("skincolor"));
                    ret.gender = rs.getInt("gender");
                    ret.job = Job.getById(rs.getInt("job"));
                    ret.finishedDojoTutorial = rs.getInt("finishedDojoTutorial") == 1;
                    rs.getInt("vanquisherKills");
                    rs.getInt("omokwins");
                    rs.getInt("omoklosses");
                    rs.getInt("omokties");
                    rs.getInt("matchcardwins");
                    rs.getInt("matchcardlosses");
                    rs.getInt("matchcardties");
                    ret.hair = rs.getInt("hair");
                    ret.face = rs.getInt("face");
                    ret.accountid = rs.getInt("accountid");
                    ret.mapid = rs.getInt("map");
                    ret.jailExpiration = rs.getLong("jailexpire");
                    ret.initialSpawnPoint = rs.getInt("spawnpoint");
                    ret.world = rs.getByte("world");
                    ret.rank = rs.getInt("rank");
                    ret.rankMove = rs.getInt("rankMove");
                    ret.jobRank = rs.getInt("jobRank");
                    ret.jobRankMove = rs.getInt("jobRankMove");
                    mountexp = rs.getInt("mountexp");
                    mountlevel = rs.getInt("mountlevel");
                    mounttiredness = rs.getInt("mounttiredness");
                    ret.guildid = rs.getInt("guildid");
                    ret.guildRank = rs.getInt("guildrank");
                    ret.allianceRank = rs.getInt("allianceRank");
                    ret.familyId = rs.getInt("familyId");
                    ret.bookCover = rs.getInt("monsterbookcover");
                    ret.monsterbook = new MonsterBook();
                    ret.monsterbook.loadCards(charid);
                    rs.getInt("vanquisherStage");
                    rs.getInt("ariantPoints");
                    rs.getInt("dojoPoints");
                    rs.getInt("lastDojoStage");
                    ret.dataString = rs.getString("dataString");
                    int buddyCapacity = rs.getInt("buddyCapacity");
                    ret.buddylist = new BuddyList(buddyCapacity);
                    ret.lastExpGainTime = rs.getTimestamp("lastExpGainTime").getTime();
                    ret.canRecvPartySearchInvite = rs.getBoolean("partySearch");

                    wserv = Server.getInstance().getWorld(ret.world);

                    ret.getInventory(InventoryType.EQUIP).setSlotLimit(rs.getByte("equipslots"));
                    ret.getInventory(InventoryType.USE).setSlotLimit(rs.getByte("useslots"));
                    ret.getInventory(InventoryType.SETUP).setSlotLimit(rs.getByte("setupslots"));
                    ret.getInventory(InventoryType.ETC).setSlotLimit(rs.getByte("etcslots"));

                    short sandboxCheck = 0x0;
                    for (Pair<Item, InventoryType> item : ItemFactory.INVENTORY.loadItems(ret.id, !channelserver)) {
                        sandboxCheck |= item.getLeft().getFlag();

                        ret.getInventory(item.getRight()).addItemFromDB(item.getLeft());
                        Item itemz = item.getLeft();
                        if (itemz.getPetId() > -1) {
                            Pet pet = itemz.getPet();
                            if (pet != null && pet.isSummoned()) {
                                ret.addPet(pet);
                            }
                        }
                    }

                    if ((sandboxCheck & ItemConstants.SANDBOX) == ItemConstants.SANDBOX) {
                        ret.setHasSandboxItem();
                    }

                    //PreparedStatement ps2, ps3;
                    //ResultSet rs2, rs3;

                    // Items excluded from pet loot
                    try (PreparedStatement psPet = con.prepareStatement("SELECT petid FROM inventoryitems WHERE characterid = ? AND petid > -1")) {
                        psPet.setInt(1, charid);

                        try (ResultSet rsPet = psPet.executeQuery()) {
                            while (rsPet.next()) {
                                final int petId = rsPet.getInt("petid");

                                try (PreparedStatement psItem = con.prepareStatement("SELECT itemid FROM petignores WHERE petid = ?")) {
                                    psItem.setInt(1, petId);

                                    ret.resetExcluded(petId);

                                    try (ResultSet rsItem = psItem.executeQuery()) {
                                        while (rsItem.next()) {
                                            ret.addExcluded(petId, rsItem.getInt("itemid"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ret.commitExcludedItems();


                    if (channelserver) {
                        MapManager mapManager = client.getChannelServer().getMapFactory();
                        ret.map = mapManager.getMap(ret.mapid);

                        if (ret.map == null) {
                            ret.map = mapManager.getMap(MapId.HENESYS);
                        }
                        Portal portal = ret.map.getPortal(ret.initialSpawnPoint);
                        if (portal == null) {
                            portal = ret.map.getPortal(0);
                            ret.initialSpawnPoint = 0;
                        }
                        ret.setPosition(portal.getPosition());
                        int partyid = rs.getInt("party");
                        Party party = wserv.getParty(partyid);
                        if (party != null) {
                            ret.mpc = party.getMemberById(ret.id);
                            if (ret.mpc != null) {
                                ret.mpc = new PartyCharacter(ret);
                                ret.party = party;
                            }
                        }
                        int messengerid = rs.getInt("messengerid");
                        int position = rs.getInt("messengerposition");
                        if (messengerid > 0 && position < 4 && position > -1) {
                            Messenger messenger = wserv.getMessenger(messengerid);
                            if (messenger != null) {
                                ret.messenger = messenger;
                                ret.messengerposition = position;
                            }
                        }
                        ret.loggedIn = true;
                    }
                }
            }

            // Teleport rocks
            try (PreparedStatement ps = con.prepareStatement("SELECT mapid,vip FROM trocklocations WHERE characterid = ? LIMIT 15")) {
                ps.setInt(1, charid);

                try (ResultSet rs = ps.executeQuery()) {
                    byte vip = 0;
                    byte reg = 0;
                    while (rs.next()) {
                        if (rs.getInt("vip") == 1) {
                            ret.viptrockmaps.add(rs.getInt("mapid"));
                            vip++;
                        } else {
                            ret.trockmaps.add(rs.getInt("mapid"));
                            reg++;
                        }
                    }
                    while (vip < 10) {
                        ret.viptrockmaps.add(MapId.NONE);
                        vip++;
                    }
                    while (reg < 5) {
                        ret.trockmaps.add(MapId.NONE);
                        reg++;
                    }
                }
            }

            // Account info
            try (PreparedStatement ps = con.prepareStatement("SELECT name, characterslots, language FROM accounts WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, ret.accountid);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Client retClient = ret.getClient();

                        retClient.setAccountName(rs.getString("name"));
                        retClient.setCharacterSlots(rs.getByte("characterslots"));
                        retClient.setLanguage(rs.getInt("language"));   // thanks Zein for noticing user language not overriding default once player is in-game
                    }
                }
            }

            // Area info
            try (PreparedStatement ps = con.prepareStatement("SELECT area,info FROM area_info WHERE charid = ?")) {
                ps.setInt(1, ret.id);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ret.area_info.put(rs.getShort("area"), rs.getString("info"));
                    }
                }
            }

            ret.cashshop = new CashShop(ret.accountid, ret.id, ret.getJobType());
            ret.autoban = new AutobanManager(ret);

            // Blessing of the Fairy
            try (PreparedStatement ps = con.prepareStatement("SELECT name, level FROM characters WHERE accountid = ? AND id != ? ORDER BY level DESC limit 1")) {
                ps.setInt(1, ret.accountid);
                ps.setInt(2, charid);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ret.linkedName = rs.getString("name");
                        ret.linkedLevel = rs.getInt("level");
                    }
                }
            }

            if (channelserver) {
                final Map<Integer, QuestStatus> loadedQuestStatus = new LinkedHashMap<>();

                // Quest status
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?")) {
                    ps.setInt(1, charid);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Quest q = Quest.getInstance(rs.getShort("quest"));
                            QuestStatus status = new QuestStatus(q, QuestStatus.Status.getById(rs.getInt("status")));
                            long cTime = rs.getLong("time");
                            if (cTime > -1) {
                                status.setCompletionTime(SECONDS.toMillis(cTime));
                            }

                            long eTime = rs.getLong("expires");
                            if (eTime > 0) {
                                status.setExpirationTime(eTime);
                            }

                            status.setForfeited(rs.getInt("forfeited"));
                            status.setCompleted(rs.getInt("completed"));
                            ret.quests.put(q.getId(), status);
                            loadedQuestStatus.put(rs.getInt("queststatusid"), status);
                        }
                    }
                }

                // Quest progress
                // opportunity for improvement on questprogress/medalmaps calls to DB
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM questprogress WHERE characterid = ?")) {
                    ps.setInt(1, charid);
                    try (ResultSet rsProgress = ps.executeQuery()) {
                        while (rsProgress.next()) {
                            QuestStatus status = loadedQuestStatus.get(rsProgress.getInt("queststatusid"));
                            if (status != null) {
                                status.setProgress(rsProgress.getInt("progressid"), rsProgress.getString("progress"));
                            }
                        }
                    }
                }

                // Medal map visit progress
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM medalmaps WHERE characterid = ?")) {
                    ps.setInt(1, charid);
                    try (ResultSet rsMedalMaps = ps.executeQuery()) {
                        while (rsMedalMaps.next()) {
                            QuestStatus status = loadedQuestStatus.get(rsMedalMaps.getInt("queststatusid"));
                            if (status != null) {
                                status.addMedalMap(rsMedalMaps.getInt("mapid"));
                            }
                        }
                    }
                }

                loadedQuestStatus.clear();

                // Skills
                try (PreparedStatement ps = con.prepareStatement("SELECT skillid,skilllevel,masterlevel,expiration FROM skills WHERE characterid = ?")) {
                    ps.setInt(1, charid);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Skill pSkill = SkillFactory.getSkill(rs.getInt("skillid"));
                            if (pSkill != null) { // edit reported by Shavit (=＾● ⋏ ●＾=), thanks Zein for noticing an NPE here
                                ret.skills.put(pSkill, new SkillEntry(rs.getByte("skilllevel"), rs.getInt("masterlevel"), rs.getLong("expiration")));
                            }
                        }
                    }
                }

                // Cooldowns (load)
                try (PreparedStatement ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM cooldowns WHERE charid = ?")) {
                    ps.setInt(1, ret.getId());

                    try (ResultSet rs = ps.executeQuery()) {
                        long curTime = Server.getInstance().getCurrentTime();
                        while (rs.next()) {
                            final int skillid = rs.getInt("SkillID");
                            final long length = rs.getLong("length"), startTime = rs.getLong("StartTime");
                            if (skillid != 5221999 && (length + startTime < curTime)) {
                                continue;
                            }
                            ret.giveCoolDowns(skillid, startTime, length);
                        }
                    }
                }

                // Cooldowns (delete)
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM cooldowns WHERE charid = ?")) {
                    ps.setInt(1, ret.getId());
                    ps.executeUpdate();
                }

                // Debuffs (load)
                Map<Disease, Pair<Long, MobSkill>> loadedDiseases = new LinkedHashMap<>();
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM playerdiseases WHERE charid = ?")) {
                    ps.setInt(1, ret.getId());

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            final Disease disease = Disease.ordinal(rs.getInt("disease"));
                            if (disease == Disease.NULL) {
                                continue;
                            }

                            final int skillid = rs.getInt("mobskillid");
                            final int skilllv = rs.getInt("mobskilllv");
                            final long length = rs.getInt("length");

                            MobSkillType type = MobSkillType.from(skillid).orElseThrow();
                            MobSkill ms = MobSkillFactory.getMobSkillOrThrow(type, skilllv);
                            loadedDiseases.put(disease, new Pair<>(length, ms));
                        }
                    }
                }

                // Debuffs (delete)
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM playerdiseases WHERE charid = ?")) {
                    ps.setInt(1, ret.getId());
                    ps.executeUpdate();
                }

                if (!loadedDiseases.isEmpty()) {
                    Server.getInstance().getPlayerBuffStorage().addDiseasesToStorage(ret.id, loadedDiseases);
                }

                // Skill macros
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?")) {
                    ps.setInt(1, charid);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int position = rs.getInt("position");
                            SkillMacro macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
                            ret.skillMacros[position] = macro;
                        }
                    }
                }

                // Key config
                try (PreparedStatement ps = con.prepareStatement("SELECT key,type,action FROM keymap WHERE characterid = ?")) {
                    ps.setInt(1, charid);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int key = rs.getInt("key");
                            int type = rs.getInt("type");
                            int action = rs.getInt("action");
                            ret.keymap.put(key, new KeyBinding(type, action));
                        }
                    }
                }

                // Saved locations
                try (PreparedStatement ps = con.prepareStatement("SELECT locationtype,map,portal FROM savedlocations WHERE characterid = ?")) {
                    ps.setInt(1, charid);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ret.savedLocations[SavedLocationType.valueOf(rs.getString("locationtype")).ordinal()] = new SavedLocation(rs.getInt("map"), rs.getInt("portal"));
                        }
                    }
                }

                // Fame history
                final var query = """
                        SELECT characterid_to,"when"
                        FROM famelog
                        WHERE characterid = ?
                        AND NOW()::date - "when"::date < 30
                        """;
                try (PreparedStatement ps = con.prepareStatement(query)) {
                    ps.setInt(1, charid);

                    try (ResultSet rs = ps.executeQuery()) {
                        ret.lastfametime = 0;
                        ret.lastmonthfameids = new ArrayList<>(31);
                        while (rs.next()) {
                            ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                            ret.lastmonthfameids.add(rs.getInt("characterid_to"));
                        }
                    }
                }

                ret.buddylist.loadFromDb(charid);
                ret.storage = wserv.getAccountStorage(ret.accountid);

                /* Double-check storage incase player is first time on server
                 * The storage won't exist so nothing to load
                 */
                if (ret.storage == null) {
                    wserv.loadAccountStorage(ret.accountid);
                    ret.storage = wserv.getAccountStorage(ret.accountid);
                }

                int startHp = ret.hp, startMp = ret.mp;
                ret.reapplyLocalStats();
                ret.changeHpMp(startHp, startMp, true);
                //ret.resetBattleshipHp();
            }

            final int mountid = ret.getJobType() * 10000000 + 1004;
            if (ret.getInventory(InventoryType.EQUIPPED).getItem((short) -18) != null) {
                ret.maplemount = new Mount(ret, ret.getInventory(InventoryType.EQUIPPED).getItem((short) -18).getItemId(), mountid);
            } else {
                ret.maplemount = new Mount(ret, 0, mountid);
            }
            ret.maplemount.setExp(mountexp);
            ret.maplemount.setLevel(mountlevel);
            ret.maplemount.setTiredness(mounttiredness);
            ret.maplemount.setActive(false);

            // Quickslot key config
            try (final PreparedStatement pSelectQuickslotKeyMapped = con.prepareStatement("SELECT keymap FROM quickslotkeymapped WHERE accountid = ?;")) {
                pSelectQuickslotKeyMapped.setInt(1, ret.getAccountID());

                try (final ResultSet pResultSet = pSelectQuickslotKeyMapped.executeQuery()) {
                    if (pResultSet.next()) {
                        ret.m_aQuickslotLoaded = LongTool.LongToBytes(pResultSet.getLong(1));
                        ret.m_pQuickslotKeyMapped = new QuickslotBinding(ret.m_aQuickslotLoaded);
                    }
                }
            }

            return ret;
        } catch (SQLException | RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String makeMapleReadable(String in) {
        String i = in.replace('I', 'i');
        i = i.replace('l', 'L');
        i = i.replace("rn", "Rn");
        i = i.replace("vv", "Vv");
        i = i.replace("VV", "Vv");

        return i;
    }

    private static int calcTransientRatio(float transientpoint) {
        int ret = (int) transientpoint;
        return !(ret <= 0 && transientpoint > 0.0f) ? ret : 1;
    }

    public static boolean doNameChange(Connection con, int characterId, String oldName, String newName, int nameChangeId) {
        try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET name = ? WHERE id = ?")) {
            ps.setString(1, newName);
            ps.setInt(2, characterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to perform chr name change in database for chrId {}", characterId, e);
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement("UPDATE rings SET partnername = ? WHERE partnername = ?")) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update rings during chr name change for chrId {}", characterId, e);
            return false;
        }

        /*try (PreparedStatement ps = con.prepareStatement("UPDATE playernpcs SET name = ? WHERE name = ?")) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            FilePrinter.printError(FilePrinter.CHANGE_CHARACTER_NAME, e, "Character ID : " + characterId);
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement("UPDATE gifts SET from = ? WHERE from = ?")) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            FilePrinter.printError(FilePrinter.CHANGE_CHARACTER_NAME, e, "Character ID : " + characterId);
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement("UPDATE dueypackages SET SenderName = ? WHERE SenderName = ?")) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            FilePrinter.printError(FilePrinter.CHANGE_CHARACTER_NAME, e, "Character ID : " + characterId);
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement("UPDATE dueypackages SET SenderName = ? WHERE SenderName = ?")) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            FilePrinter.printError(FilePrinter.CHANGE_CHARACTER_NAME, e, "Character ID : " + characterId);
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement("UPDATE inventoryitems SET owner = ? WHERE owner = ?")) { //GMS doesn't do this
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            FilePrinter.printError(FilePrinter.CHANGE_CHARACTER_NAME, e, "Character ID : " + characterId);
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement("UPDATE mts_items SET owner = ? WHERE owner = ?")) { //GMS doesn't do this
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            FilePrinter.printError(FilePrinter.CHANGE_CHARACTER_NAME, e, "Character ID : " + characterId);
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement("UPDATE newyear SET sendername = ? WHERE sendername = ?")) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            FilePrinter.printError(FilePrinter.CHANGE_CHARACTER_NAME, e, "Character ID : " + characterId);
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement("UPDATE newyear SET receivername = ? WHERE receivername = ?")) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            FilePrinter.printError(FilePrinter.CHANGE_CHARACTER_NAME, e, "Character ID : " + characterId);
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement("UPDATE notes SET to = ? WHERE to = ?")) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            FilePrinter.printError(FilePrinter.CHANGE_CHARACTER_NAME, e, "Character ID : " + characterId);
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement("UPDATE notes SET from = ? WHERE from = ?")) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            FilePrinter.printError(FilePrinter.CHANGE_CHARACTER_NAME, e, "Character ID : " + characterId);
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement("UPDATE nxcode SET retriever = ? WHERE retriever = ?")) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            FilePrinter.printError(FilePrinter.CHANGE_CHARACTER_NAME, e, "Character ID : " + characterId);
            return false;
        }*/

        if (nameChangeId != -1) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE namechanges SET completionTime = ? WHERE id = ?")) {
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ps.setInt(2, nameChangeId);
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("Failed to save chr name change for chrId {}", nameChangeId, e);
                return false;
            }
        }
        return true;
    }

    public static String checkWorldTransferEligibility(Connection con, int characterId, int oldWorld, int newWorld) {
        if (!YamlConfig.config.server.ALLOW_CASHSHOP_WORLD_TRANSFER) {
            return "World transfers disabled.";
        }
        int accountId = -1;
        try (PreparedStatement ps = con.prepareStatement("SELECT accountid, level, guildid, guildrank, partnerId, familyId FROM characters WHERE id = ?")) {
            ps.setInt(1, characterId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return "Character does not exist.";
            }
            accountId = rs.getInt("accountid");
            if (rs.getInt("level") < 20) {
                return "Character is under level 20.";
            }
            if (rs.getInt("familyId") != -1) {
                return "Character is in family.";
            }
            if (rs.getInt("partnerId") != 0) {
                return "Character is married.";
            }
            if (rs.getInt("guildid") != 0 && rs.getInt("guildrank") < 2) {
                return "Character is the leader of a guild.";
            }
        } catch (SQLException e) {
            log.error("Change character name", e);
            return "SQL Error";
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT tempban FROM accounts WHERE id = ?")) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return "Account does not exist.";
            }
            LocalDateTime tempban = rs.getTimestamp("tempban").toLocalDateTime();
            if (!tempban.equals(DefaultDates.getTempban())) {
                return "Account has been banned.";
            }
        } catch (SQLException e) {
            log.error("Change character name", e);
            return "SQL Error";
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) AS rowcount FROM characters WHERE accountid = ? AND world = ?")) {
            ps.setInt(1, accountId);
            ps.setInt(2, newWorld);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return "SQL Error";
            }
            if (rs.getInt("rowcount") >= 3) {
                return "Too many characters on destination world.";
            }
        } catch (SQLException e) {
            log.error("Change character name", e);
            return "SQL Error";
        }
        return null;
    }

    public static boolean doWorldTransfer(Connection con, int characterId, int oldWorld, int newWorld, int worldTransferId) {
        int mesos = 0;
        try (PreparedStatement ps = con.prepareStatement("SELECT meso FROM characters WHERE id = ?")) {
            ps.setInt(1, characterId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                log.warn("Character data invalid for world transfer? chrId {}", characterId);
                return false;
            }
            mesos = rs.getInt("meso");
        } catch (SQLException e) {
            log.error("Failed to do world transfer for chrId {}", characterId, e);
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET world = ?, meso = ?, guildid = ?, guildrank = ? WHERE id = ?")) {
            ps.setInt(1, newWorld);
            ps.setInt(2, Math.min(mesos, 1000000)); // might want a limit in "YamlConfig.config.server" for this
            ps.setInt(3, 0);
            ps.setInt(4, 5);
            ps.setInt(5, characterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update chrId {} during world transfer", characterId, e);
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM buddies WHERE characterid = ? OR buddyid = ?")) {
            ps.setInt(1, characterId);
            ps.setInt(2, characterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete buddies for chrId {} during world transfer", characterId, e);
            return false;
        }
        if (worldTransferId != -1) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE worldtransfers SET completionTime = ? WHERE id = ?")) {
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ps.setInt(2, worldTransferId);
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("Failed to update world transfer for chrId {}", characterId, e);
                return false;
            }
        }
        return true;
    }

    public Job getJobStyle(byte opt) {
        return getJobStyleInternal(this.getJob().getId(), opt);
    }

    public Job getJobStyle() {
        return getJobStyle((byte) ((this.getStr() > this.getDex()) ? 0x80 : 0x40));
    }

    public boolean isLoggedinWorld() {
        return this.isLoggedin() && !this.isAwayFromWorld();
    }

    public boolean isAwayFromWorld() {
        return awayFromWorld.get();
    }

    public void setEnteredChannelWorld() {
        awayFromWorld.set(false);
        client.getChannelServer().removePlayerAway(id);

        if (canRecvPartySearchInvite) {
            this.getWorldServer().getPartySearchCoordinator().attachPlayer(this);
        }
    }

    public void setAwayFromChannelWorld() {
        setAwayFromChannelWorld(false);
    }

    public void setDisconnectedFromChannelWorld() {
        setAwayFromChannelWorld(true);
    }

    private void setAwayFromChannelWorld(boolean disconnect) {
        awayFromWorld.set(true);

        if (!disconnect) {
            client.getChannelServer().insertPlayerAway(id);
        } else {
            client.getChannelServer().removePlayerAway(id);
        }
    }

    public void updatePartySearchAvailability(boolean psearchAvailable) {
        if (psearchAvailable) {
            if (canRecvPartySearchInvite && getParty() == null) {
                this.getWorldServer().getPartySearchCoordinator().attachPlayer(this);
            }
        } else {
            if (canRecvPartySearchInvite) {
                this.getWorldServer().getPartySearchCoordinator().detachPlayer(this);
            }
        }
    }

    public void resetPartySearchInvite(int fromLeaderid) {
        disabledPartySearchInvites.remove(fromLeaderid);
    }

    public void disablePartySearchInvite(int fromLeaderid) {
        disabledPartySearchInvites.add(fromLeaderid);
    }

    public boolean hasDisabledPartySearchInvite(int fromLeaderid) {
        return disabledPartySearchInvites.contains(fromLeaderid);
    }

    public void setSessionTransitionState() {
        client.setCharacterOnSessionTransitionState(this.getId());
    }

    public boolean getCS() {
        return useCS;
    }

    public void setCS(boolean cs) {
        useCS = cs;
    }

    public long getNpcCooldown() {
        return npcCd;
    }

    public void setNpcCooldown(long d) {
        npcCd = d;
    }

    public void addCooldown(int skillId, long startTime, long length) {
        effLock.lock();
        chrLock.lock();
        try {
            this.coolDowns.put(Integer.valueOf(skillId), new CooldownValueHolder(skillId, startTime, length));
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public void addFame(int famechange) {
        this.fame += famechange;
    }

    public void addMesosTraded(int gain) {
        this.mesosTraded += gain;
    }

    public void addPet(Pet pet) {
        petLock.lock();
        try {
            for (int i = 0; i < 3; i++) {
                if (pets[i] == null) {
                    pets[i] = pet;
                    return;
                }
            }
        } finally {
            petLock.unlock();
        }
    }

    public void addVisibleMapObject(MapObject mo) {
        visibleMapObjects.add(mo);
    }

    public void ban(String reason) {
        this.isbanned = true;
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?")) {
            ps.setString(1, reason);
            ps.setInt(2, accountid);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int calculateMaxBaseDamage(int watk, WeaponType weapon) {
        int mainstat, secondarystat;

        if (weapon == WeaponType.BOW || weapon == WeaponType.CROSSBOW || weapon == WeaponType.GUN) {
            mainstat = localdex;
            secondarystat = localstr;
        } else if (weapon == WeaponType.CLAW || weapon == WeaponType.DAGGER_THIEVES) {
            mainstat = localluk;
            secondarystat = localdex + localstr;
        } else {
            mainstat = localstr;
            secondarystat = localdex;
        }
        return (int) Math.ceil(((weapon.getMaxDamageMultiplier() * mainstat + secondarystat) / 100.0) * watk);
    }

    public int calculateMaxBaseDamage() {
        return 1;
    }

    public int calculateMaxBaseMagicDamage(int matk) {
        int maxbasedamage = matk;
        int totalint = getTotalInt();

        if (totalint > 2000) {
            maxbasedamage -= 2000;
            maxbasedamage += (int) ((0.09033024267 * totalint) + 3823.8038);
        } else {
            maxbasedamage -= totalint;

            if (totalint > 1700) {
                maxbasedamage += (int) (0.1996049769 * Math.pow(totalint, 1.300631341));
            } else {
                maxbasedamage += (int) (0.1996049769 * Math.pow(totalint, 1.290631341));
            }
        }

        return (maxbasedamage * 107) / 100;
    }

    public boolean cannotEnterCashShop() {
        return blockCashShop;
    }

    public void toggleBlockCashShop() {
        blockCashShop = !blockCashShop;
    }

    public void newClient(Client c) {
        this.loggedIn = true;
        c.setAccountName(this.client.getAccountName());//No null's for accountName
        this.setClient(c);
        this.map = c.getChannelServer().getMapFactory().getMap(getMapId());
        Portal portal = map.findClosestPlayerSpawnpoint(getPosition());
        if (portal == null) {
            portal = map.getPortal(0);
        }
        this.setPosition(portal.getPosition());
        this.initialSpawnPoint = portal.getId();
    }

    public String getMedalText() {
        String medal = "";
        final Item medalItem = getInventory(InventoryType.EQUIPPED).getItem((short) -49);
        if (medalItem != null) {
            medal = "<" + ItemInformationProvider.getInstance().getName(medalItem.getItemId()) + "> ";
        }
        return medal;
    }

    public void Hide(boolean hide, boolean login) {
        if (isGM() && hide != this.hidden) {
            if (!hide) {
                this.hidden = false;
                sendPacket(ChannelPacketCreator.getInstance().getGMEffect(0x10, (byte) 0));
                List<BuffStat> dsstat = Collections.singletonList(BuffStat.DARKSIGHT);
                getMap().broadcastGMMessage(this, ChannelPacketCreator.getInstance().cancelForeignBuff(id, dsstat), false);
                getMap().broadcastSpawnPlayerMapObjectMessage(this, this, false);

                for (Summon ms : this.getSummonsValues()) {
                    getMap().broadcastNONGMMessage(this, ChannelPacketCreator.getInstance().spawnSummon(ms, false), false);
                }

                for (MapObject mo : this.getMap().getMonsters()) {
                    Monster m = (Monster) mo;
                    m.aggroUpdateController();
                }
            } else {
                this.hidden = true;
                sendPacket(ChannelPacketCreator.getInstance().getGMEffect(0x10, (byte) 1));
                if (!login) {
                    getMap().broadcastNONGMMessage(this, ChannelPacketCreator.getInstance().removePlayerFromMap(getId()), false);
                }
                List<Pair<BuffStat, Integer>> ldsstat = Collections.singletonList(new Pair<BuffStat, Integer>(BuffStat.DARKSIGHT, 0));
                getMap().broadcastGMMessage(this, ChannelPacketCreator.getInstance().giveForeignBuff(id, ldsstat), false);
                this.releaseControlledMonsters();
            }
            sendPacket(ChannelPacketCreator.getInstance().enableActions());
        }
    }

    public void Hide(boolean hide) {
        Hide(hide, false);
    }

    public void toggleHide() {
        Hide(!hidden);
    }

    private void cancelPlayerBuffs(List<BuffStat> buffstats) {
        if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            updateLocalStats();
            sendPacket(ChannelPacketCreator.getInstance().cancelBuff(buffstats));
            if (buffstats.size() > 0) {
                getMap().broadcastMessage(this, ChannelPacketCreator.getInstance().cancelForeignBuff(getId(), buffstats), false);
            }
        }
    }

    public boolean canDoor() {
        Door door = getPlayerDoor();
        return door == null || (door.isActive() && door.getElapsedDeployTime() > 5000);
    }

    public void setHasSandboxItem() {
        hasSandboxItem = true;
    }

    public void removeSandboxItems() {  // sandbox idea thanks to Morty
        if (!hasSandboxItem) {
            return;
        }

        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        for (InventoryType invType : InventoryType.values()) {
            Inventory inv = this.getInventory(invType);

            inv.lockInventory();
            try {
                for (Item item : new ArrayList<>(inv.list())) {
                    if (InventoryManipulator.isSandboxItem(item)) {
                        InventoryManipulator.removeFromSlot(client, invType, item.getPosition(), item.getQuantity(), false);
                        dropMessage(5, "[" + ii.getName(item.getItemId()) + "] has passed its trial conditions and will be removed from your inventory.");
                    }
                }
            } finally {
                inv.unlockInventory();
            }
        }

        hasSandboxItem = false;
    }

    public FameStatus canGiveFame(Character from) {
        if (this.isGM()) {
            return FameStatus.OK;
        } else if (lastfametime >= System.currentTimeMillis() - 3600000 * 24) {
            return FameStatus.NOT_TODAY;
        } else if (lastmonthfameids.contains(Integer.valueOf(from.getId()))) {
            return FameStatus.NOT_THIS_MONTH;
        } else {
            return FameStatus.OK;
        }
    }

    public void changeCI(int type) {
        this.ci = type;
    }

    public void changeKeybinding(int key, KeyBinding keybinding) {
        if (keybinding.getType() != 0) {
            keymap.put(Integer.valueOf(key), keybinding);
        } else {
            keymap.remove(Integer.valueOf(key));
        }
    }

    public void changeQuickslotKeybinding(byte[] aQuickslotKeyMapped) {
        this.m_pQuickslotKeyMapped = new QuickslotBinding(aQuickslotKeyMapped);
    }

    public void broadcastStance(int newStance) {
        setStance(newStance);
        broadcastStance();
    }

    public void broadcastStance() {
        map.broadcastMessage(this, ChannelPacketCreator.getInstance().movePlayer(id, this.getIdleMovement(), AbstractAnimatedMapObject.IDLE_MOVEMENT_PACKET_LENGTH), false);
    }

    public MapleMap getWarpMap(int map) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        return warpMap;
    }

    private void eventChangedMap(int map) {

    }

    private void eventAfterChangedMap(int map) {

    }

    public void changeMapBanish(BanishInfo banishInfo) {
        if (banishInfo.msg() != null) {
            dropMessage(5, banishInfo.msg());
        }

        MapleMap map_ = getWarpMap(mapid);
        Portal portal_ = map_.getPortal(banishInfo.portal());
        changeMap(map_, portal_ != null ? portal_ : map_.getRandomPlayerSpawnpoint());
    }

    public void changeMap(int map) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);

        changeMap(warpMap, warpMap.getRandomPlayerSpawnpoint());
    }

    public void changeMap(int map, int portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);

        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, String portal) {
        MapleMap warpMap = warpMap = client.getChannelServer().getMapFactory().getMap(map);

        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, Portal portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);

        changeMap(warpMap, portal);
    }

    public void changeMap(MapleMap to) {
        changeMap(to, 0);
    }

    public void changeMap(MapleMap to, int portal) {
        changeMap(to, to.getPortal(portal));
    }

    public void changeMap(final MapleMap target, Portal pto) {
        canWarpCounter++;

        eventChangedMap(target.getId());    // player can be dropped from an event here, hence the new warping target.
        MapleMap to = getWarpMap(target.getId());
        if (pto == null) {
            pto = to.getPortal(0);
        }
        changeMapInternal(to, pto.getPosition(), ChannelPacketCreator.getInstance().getWarpToMap(to, pto.getId(), this));
        canWarpMap = false;

        canWarpCounter--;
        if (canWarpCounter == 0) {
            canWarpMap = true;
        }

        eventAfterChangedMap(this.getMapId());
    }

    public void changeMap(final MapleMap target, final Point pos) {
        canWarpCounter++;

        eventChangedMap(target.getId());
        MapleMap to = getWarpMap(target.getId());
        changeMapInternal(to, pos, ChannelPacketCreator.getInstance().getWarpToMap(to, 0x80, pos, this));
        canWarpMap = false;

        canWarpCounter--;
        if (canWarpCounter == 0) {
            canWarpMap = true;
        }

        eventAfterChangedMap(this.getMapId());
    }

    public void forceChangeMap(final MapleMap target, Portal pto) {
        // will actually enter the map given as parameter, regardless of being an eventmap or whatnot

        canWarpCounter++;
        eventChangedMap(MapId.NONE);

        MapleMap to = target; // warps directly to the target intead of the target's map id, this allows GMs to patrol players inside instances.
        if (pto == null) {
            pto = to.getPortal(0);
        }
        changeMapInternal(to, pto.getPosition(), ChannelPacketCreator.getInstance().getWarpToMap(to, pto.getId(), this));
        canWarpMap = false;

        canWarpCounter--;
        if (canWarpCounter == 0) {
            canWarpMap = true;
        }

        eventAfterChangedMap(this.getMapId());
    }

    private boolean buffMapProtection() {
        int thisMapid = mapid;
        int returnMapid = client.getChannelServer().getMapFactory().getMap(thisMapid).getReturnMapId();

        effLock.lock();
        chrLock.lock();
        try {
            for (Entry<BuffStat, BuffStatValueHolder> mbs : effects.entrySet()) {
                if (mbs.getKey() == BuffStat.MAP_PROTECTION) {
                    byte value = (byte) mbs.getValue().value;

                    if (value == 1 && ((returnMapid == MapId.EL_NATH && thisMapid != MapId.ORBIS_TOWER_BOTTOM) || returnMapid == MapId.INTERNET_CAFE)) {
                        return true;        //protection from cold
                    } else {
                        return value == 2 && (returnMapid == MapId.AQUARIUM || thisMapid == MapId.ORBIS_TOWER_BOTTOM);        //breathing underwater
                    }
                }
            }
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }

        for (Item it : this.getInventory(InventoryType.EQUIPPED).list()) {
            if ((it.getFlag() & ItemConstants.COLD) == ItemConstants.COLD &&
                    ((returnMapid == MapId.EL_NATH && thisMapid != MapId.ORBIS_TOWER_BOTTOM) || returnMapid == MapId.INTERNET_CAFE)) {
                return true;        //protection from cold
            }
        }

        return false;
    }

    public void partyOperationUpdate(Party party, List<Character> exPartyMembers) {
        List<WeakReference<MapleMap>> mapids;

        petLock.lock();
        try {
            mapids = new LinkedList<>(lastVisitedMaps);
        } finally {
            petLock.unlock();
        }

        List<Character> partyMembers = new LinkedList<>();
        for (Character mc : (exPartyMembers != null) ? exPartyMembers : this.getPartyMembersOnline()) {
            if (mc.isLoggedinWorld()) {
                partyMembers.add(mc);
            }
        }

        Character partyLeaver = null;
        if (exPartyMembers != null) {
            partyMembers.remove(this);
            partyLeaver = this;
        }

        MapleMap map = this.getMap();
        List<MapItem> partyItems = null;

        int partyId = exPartyMembers != null ? -1 : this.getPartyId();
        for (WeakReference<MapleMap> mapRef : mapids) {
            MapleMap mapObj = mapRef.get();

            if (mapObj != null) {
                List<MapItem> partyMapItems = mapObj.updatePlayerItemDropsToParty(partyId, id, partyMembers, partyLeaver);
                if (map.hashCode() == mapObj.hashCode()) {
                    partyItems = partyMapItems;
                }
            }
        }

        if (partyItems != null && exPartyMembers == null) {
            map.updatePartyItemDropsToNewcomer(this, partyItems);
        }

        updatePartyTownDoors(party, this, partyLeaver, partyMembers);
    }

    private Integer getVisitedMapIndex(MapleMap map) {
        int idx = 0;

        for (WeakReference<MapleMap> mapRef : lastVisitedMaps) {
            if (map.equals(mapRef.get())) {
                return idx;
            }

            idx++;
        }

        return -1;
    }

    public void visitMap(MapleMap map) {
        petLock.lock();
        try {
            int idx = getVisitedMapIndex(map);

            if (idx == -1) {
                if (lastVisitedMaps.size() == YamlConfig.config.server.MAP_VISITED_SIZE) {
                    lastVisitedMaps.remove(0);
                }
            } else {
                WeakReference<MapleMap> mapRef = lastVisitedMaps.remove(idx);
                lastVisitedMaps.add(mapRef);
                return;
            }

            lastVisitedMaps.add(new WeakReference<>(map));
        } finally {
            petLock.unlock();
        }
    }

    public void notifyMapTransferToPartner(int mapid) {
    }

    public void removeIncomingInvites() {
        InviteCoordinator.removePlayerIncomingInvites(id);
    }

    private void changeMapInternal(final MapleMap to, final Point pos, Packet warpPacket) {
        if (!canWarpMap) {
            return;
        }

        this.mapTransitioning.set(true);

        this.unregisterChairBuff();
        Trade.cancelTrade(this, Trade.TradeResult.UNSUCCESSFUL_ANOTHER_MAP);
        this.closePlayerInteractions();

        Party e = null;
        if (this.getParty() != null && this.getParty().getEnemy() != null) {
            e = this.getParty().getEnemy();
        }
        final Party k = e;

        sendPacket(warpPacket);
        map.removePlayer(this);
        if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            map = to;
            setPosition(pos);
            map.addPlayer(this);
            visitMap(map);

            prtLock.lock();
            try {
                if (party != null) {
                    mpc.setMapId(to.getId());
                    sendPacket(ChannelPacketCreator.getInstance().updateParty(client.getChannel(), party, PartyOperation.SILENT_UPDATE, null));
                    updatePartyMemberHPInternal();
                }
            } finally {
                prtLock.unlock();
            }
            if (Character.this.getParty() != null) {
                Character.this.getParty().setEnemy(k);
            }
            silentPartyUpdateInternal(getParty());  // EIM script calls inside
        } else {
            log.warn("Chr {} got stuck when moving to map {}", getName(), map.getId());
            client.disconnect(true, false);     // thanks BHB for noticing a player storage stuck case here
            return;
        }

        notifyMapTransferToPartner(map.getId());

        //alas, new map has been specified when a warping was being processed...
        if (newWarpMap != -1) {
            canWarpMap = true;

            int temp = newWarpMap;
            newWarpMap = -1;
            changeMap(temp);
        } else {
            // if this map has obstacle components moving, make it do so for this client
            sendPacket(ChannelPacketCreator.getInstance().environmentMoveList(map.getEnvironment().entrySet()));
        }
    }

    public boolean isChangingMaps() {
        return this.mapTransitioning.get();
    }

    public void setMapTransitionComplete() {
        this.mapTransitioning.set(false);
    }

    public void changePage(int page) {
        this.currentPage = page;
    }

    public void changeSkillLevel(Skill skill, byte newLevel, int newMasterlevel, long expiration) {
        if (newLevel > -1) {
            skills.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
            if (!GameConstants.isHiddenSkills(skill.getId())) {
                sendPacket(ChannelPacketCreator.getInstance().updateSkill(skill.getId(), newLevel, newMasterlevel, expiration));
            }
        } else {
            skills.remove(skill);
            sendPacket(ChannelPacketCreator.getInstance().updateSkill(skill.getId(), newLevel, newMasterlevel, -1)); //Shouldn't use expiration anymore :)
            try (Connection con = DatabaseConnection.getStaticConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM skills WHERE skillid = ? AND characterid = ?")) {
                ps.setInt(1, skill.getId());
                ps.setInt(2, id);
                ps.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void changeTab(int tab) {
        this.currentTab = tab;
    }

    public void changeType(int type) {
        this.currentType = type;
    }

    public void checkMessenger() {
        if (messenger != null && messengerposition < 4 && messengerposition > -1) {
            World worldz = getWorldServer();
            worldz.silentJoinMessenger(messenger.getId(), new MessengerCharacter(this, messengerposition), messengerposition);
            worldz.updateMessenger(getMessenger().getId(), name, client.getChannel());
        }
    }

    public void controlMonster(Monster monster) {
        if (cpnLock.tryLock()) {
            try {
                controlled.add(monster);
            } finally {
                cpnLock.unlock();
            }
        }
    }

    public void stopControllingMonster(Monster monster) {
        if (cpnLock.tryLock()) {
            try {
                controlled.remove(monster);
            } finally {
                cpnLock.unlock();
            }
        }
    }

    public int getNumControlledMonsters() {
        cpnLock.lock();
        try {
            return controlled.size();
        } finally {
            cpnLock.unlock();
        }
    }

    public Collection<Monster> getControlledMonsters() {
        cpnLock.lock();
        try {
            return new ArrayList<>(controlled);
        } finally {
            cpnLock.unlock();
        }
    }

    public void releaseControlledMonsters() {
        Collection<Monster> controlledMonsters;

        cpnLock.lock();
        try {
            controlledMonsters = new ArrayList<>(controlled);
            controlled.clear();
        } finally {
            cpnLock.unlock();
        }

        for (Monster monster : controlledMonsters) {
            monster.aggroRedirectController();
        }
    }

    public boolean applyConsumeOnPickup(final int itemId) {
        if (itemId / 1000000 == 2) {
            ItemInformationProvider ii = ItemInformationProvider.getInstance();
            if (ii.isConsumeOnPickup(itemId)) {
                if (ItemConstants.isPartyItem(itemId)) {
                    List<Character> partyMembers = this.getPartyMembersOnSameMap();
                    if (!ItemId.isPartyAllCure(itemId)) {
                        StatEffect mse = ii.getItemEffect(itemId);
                        if (!partyMembers.isEmpty()) {
                            for (Character mc : partyMembers) {
                                if (mc.isAlive()) {
                                    mse.applyTo(mc);
                                }
                            }
                        } else if (this.isAlive()) {
                            mse.applyTo(this);
                        }
                    } else {
                        if (!partyMembers.isEmpty()) {
                            for (Character mc : partyMembers) {
                                mc.dispelDebuffs();
                            }
                        } else {
                            this.dispelDebuffs();
                        }
                    }
                } else {
                    ii.getItemEffect(itemId).applyTo(this);
                }

                if (itemId / 10000 == 238) {
                    this.getMonsterBook().addCard(client, itemId);
                }
                return true;
            }
        }
        return false;
    }

    public final void pickupItem(MapObject ob) {
        pickupItem(ob, -1);
    }

    public final void pickupItem(MapObject ob, int petIndex) {     // yes, one picks the MapObject, not the MapItem
        if (ob == null) {                                               // pet index refers to the one picking up the item
            return;
        }

        if (ob instanceof MapItem mapitem) {
            if (System.currentTimeMillis() - mapitem.getDropTime() < 400 || !mapitem.canBePickedBy(this)) {
                sendPacket(ChannelPacketCreator.getInstance().enableActions());
                return;
            }

            List<Character> mpcs = new LinkedList<>();
            if (mapitem.getMeso() > 0 && !mapitem.isPickedUp()) {
                mpcs = getPartyMembersOnSameMap();
            }

            ScriptedItem itemScript = null;
            mapitem.lockItem();
            try {
                if (mapitem.isPickedUp()) {
                    sendPacket(ChannelPacketCreator.getInstance().showItemUnavailable());
                    sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                boolean isPet = petIndex > -1;
                final Packet pickupPacket = ChannelPacketCreator.getInstance().removeItemFromMap(mapitem.getObjectId(), (isPet) ? 5 : 2, this.getId(), isPet, petIndex);

                Item mItem = mapitem.getItem();
                boolean hasSpaceInventory = true;
                ItemInformationProvider ii = ItemInformationProvider.getInstance();
                if (ItemId.isNxCard(mapitem.getItemId()) || mapitem.getMeso() > 0 || ii.isConsumeOnPickup(mapitem.getItemId()) || (hasSpaceInventory = InventoryManipulator.checkSpace(client, mapitem.getItemId(), mItem.getQuantity(), mItem.getOwner()))) {
                    int mapId = this.getMapId();

                    if ((MapId.isSelfLootableOnly(mapId))) {//happyville trees and guild PQ
                        if (!mapitem.isPlayerDrop() || mapitem.getDropper().getObjectId() == client.getPlayer().getObjectId()) {
                            if (mapitem.getMeso() > 0) {
                                if (!mpcs.isEmpty()) {
                                    int mesosamm = mapitem.getMeso() / mpcs.size();
                                    for (Character partymem : mpcs) {
                                        if (partymem.isLoggedinWorld()) {
                                            partymem.gainMeso(mesosamm, true, true, false);
                                        }
                                    }
                                } else {
                                    this.gainMeso(mapitem.getMeso(), true, true, false);
                                }

                                this.getMap().pickItemDrop(pickupPacket, mapitem);
                            } else if (ItemId.isNxCard(mapitem.getItemId())) {
                                // Add NX to account, show effect and make item disappear
                                int nxGain = mapitem.getItemId() == ItemId.NX_CARD_100 ? 100 : 250;
                                this.getCashShop().gainCash(1, nxGain);

                                if (YamlConfig.config.server.USE_ANNOUNCE_NX_COUPON_LOOT) {
                                    showHint("You have earned #e#b" + nxGain + " NX#k#n. (" + this.getCashShop().getCash(CashShop.NX_CREDIT) + " NX)", 300);
                                }

                                this.getMap().pickItemDrop(pickupPacket, mapitem);
                            } else if (InventoryManipulator.addFromDrop(client, mItem, true)) {
                                this.getMap().pickItemDrop(pickupPacket, mapitem);
                            } else {
                                sendPacket(ChannelPacketCreator.getInstance().enableActions());
                                return;
                            }
                        } else {
                            sendPacket(ChannelPacketCreator.getInstance().showItemUnavailable());
                            sendPacket(ChannelPacketCreator.getInstance().enableActions());
                            return;
                        }
                        sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return;
                    }

                    if (!this.needQuestItem(mapitem.getQuest(), mapitem.getItemId())) {
                        sendPacket(ChannelPacketCreator.getInstance().showItemUnavailable());
                        sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return;
                    }

                    if (mapitem.getMeso() > 0) {
                        if (!mpcs.isEmpty()) {
                            int mesosamm = mapitem.getMeso() / mpcs.size();
                            for (Character partymem : mpcs) {
                                if (partymem.isLoggedinWorld()) {
                                    partymem.gainMeso(mesosamm, true, true, false);
                                }
                            }
                        } else {
                            this.gainMeso(mapitem.getMeso(), true, true, false);
                        }
                    } else if (mItem.getItemId() / 10000 == 243) {
                        ScriptedItem info = ii.getScriptedItemInfo(mItem.getItemId());
                        if (info != null && info.runOnPickup()) {
                            itemScript = info;
                        } else {
                            if (!InventoryManipulator.addFromDrop(client, mItem, true)) {
                                sendPacket(ChannelPacketCreator.getInstance().enableActions());
                                return;
                            }
                        }
                    } else if (ItemId.isNxCard(mapitem.getItemId())) {
                        // Add NX to account, show effect and make item disappear
                        int nxGain = mapitem.getItemId() == ItemId.NX_CARD_100 ? 100 : 250;
                        this.getCashShop().gainCash(1, nxGain);

                        if (YamlConfig.config.server.USE_ANNOUNCE_NX_COUPON_LOOT) {
                            showHint("You have earned #e#b" + nxGain + " NX#k#n. (" + this.getCashShop().getCash(CashShop.NX_CREDIT) + " NX)", 300);
                        }
                    } else if (applyConsumeOnPickup(mItem.getItemId())) {
                    } else if (InventoryManipulator.addFromDrop(client, mItem, true)) {
                        log.debug("Player picked up item: {}", mItem);
                    } else {
                        sendPacket(ChannelPacketCreator.getInstance().enableActions());
                        return;
                    }

                    this.getMap().pickItemDrop(pickupPacket, mapitem);
                } else if (!hasSpaceInventory) {
                    sendPacket(ChannelPacketCreator.getInstance().getInventoryFull());
                    sendPacket(ChannelPacketCreator.getInstance().getShowInventoryFull());
                }
            } finally {
                mapitem.unlockItem();
            }

            if (itemScript != null) {
                ItemScriptManager ism = ItemScriptManager.getInstance();
                ism.runItemScript(client, itemScript);
            }
        }
        sendPacket(ChannelPacketCreator.getInstance().enableActions());
    }

    public int countItem(int itemid) {
        return inventory[ItemConstants.getInventoryType(itemid).ordinal()].countById(itemid);
    }

    public boolean canHold(int itemid) {
        return canHold(itemid, 1);
    }

    public boolean canHold(int itemid, int quantity) {
        return client.getAbstractPlayerInteraction().canHold(itemid, quantity);
    }

    public boolean canHoldUniques(List<Integer> itemids) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        for (Integer itemid : itemids) {
            if (ii.isPickupRestricted(itemid) && this.haveItem(itemid)) {
                return false;
            }
        }

        return true;
    }

    public void decreaseReports() {
        this.possibleReports--;
    }

    private void nextPendingRequest(Client c) {
        CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
        if (pendingBuddyRequest != null) {
            c.sendPacket(ChannelPacketCreator.getInstance().requestBuddylistAdd(pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
        }
    }

    private void notifyRemoteChannel(Client c, int remoteChannel, int otherCid, BuddyList.BuddyOperation operation) {
        Character player = c.getPlayer();
        if (remoteChannel != -1) {
            c.getWorldServer().buddyChanged(otherCid, player.getId(), player.getName(), c.getChannel(), operation);
        }
    }

    public void deleteBuddy(int otherCid) {
        BuddyList bl = getBuddylist();

        if (bl.containsVisible(otherCid)) {
            notifyRemoteChannel(client, getWorldServer().find(otherCid), otherCid, BuddyList.BuddyOperation.DELETED);
        }
        bl.remove(otherCid);
        sendPacket(ChannelPacketCreator.getInstance().updateBuddylist(getBuddylist().getBuddies()));
        nextPendingRequest(client);
    }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void stopChairTask() {
        chrLock.lock();
        try {
            if (chairRecoveryTask != null) {
                chairRecoveryTask.cancel(false);
                chairRecoveryTask = null;
            }
        } finally {
            chrLock.unlock();
        }
    }

    private void updateChairHealStats() {
        statRlock.lock();
        try {
            if (localchairrate != -1) {
                return;
            }
        } finally {
            statRlock.unlock();
        }

        effLock.lock();
        statWlock.lock();
        try {
            Pair<Integer, Pair<Integer, Integer>> p = getChairTaskIntervalRate(localmaxhp, localmaxmp);

            localchairrate = p.getLeft();
            localchairhp = p.getRight().getLeft();
            localchairmp = p.getRight().getRight();
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    private void startChairTask() {
        if (chair.get() < 0) {
            return;
        }

        int healInterval;
        effLock.lock();
        try {
            updateChairHealStats();
            healInterval = localchairrate;
        } finally {
            effLock.unlock();
        }

        chrLock.lock();
        try {
            if (chairRecoveryTask != null) {
                stopChairTask();
            }

            chairRecoveryTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    updateChairHealStats();
                    final int healHP = localchairhp;
                    final int healMP = localchairmp;

                    if (Character.this.getHp() < localmaxhp) {
                        byte recHP = (byte) (healHP / YamlConfig.config.server.CHAIR_EXTRA_HEAL_MULTIPLIER);

                        sendPacket(ChannelPacketCreator.getInstance().showOwnRecovery(recHP));
                        getMap().broadcastMessage(Character.this, ChannelPacketCreator.getInstance().showRecovery(id, recHP), false);
                    } else if (Character.this.getMp() >= localmaxmp) {
                        stopChairTask();    // optimizing schedule management when player is already with full pool.
                    }

                    addMPHP(healHP, healMP);
                }
            }, healInterval, healInterval);
        } finally {
            chrLock.unlock();
        }
    }

    private void stopExtraTask() {
        chrLock.lock();
        try {
            if (extraRecoveryTask != null) {
                extraRecoveryTask.cancel(false);
                extraRecoveryTask = null;
            }
        } finally {
            chrLock.unlock();
        }
    }

    private void startExtraTask(final byte healHP, final byte healMP, final short healInterval) {
        chrLock.lock();
        try {
            startExtraTaskInternal(healHP, healMP, healInterval);
        } finally {
            chrLock.unlock();
        }
    }

    private void startExtraTaskInternal(final byte healHP, final byte healMP, final short healInterval) {
        extraRecInterval = healInterval;

        extraRecoveryTask = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if (getBuffSource(BuffStat.HPREC) == -1 && getBuffSource(BuffStat.MPREC) == -1) {
                    stopExtraTask();
                    return;
                }

                if (Character.this.getHp() < localmaxhp) {
                    if (healHP > 0) {
                        sendPacket(ChannelPacketCreator.getInstance().showOwnRecovery(healHP));
                        getMap().broadcastMessage(Character.this, ChannelPacketCreator.getInstance().showRecovery(id, healHP), false);
                    }
                }

                addMPHP(healHP, healMP);
            }
        }, healInterval, healInterval);
    }

    public void dispel() {
        if (YamlConfig.config.server.USE_UNDISPEL_HOLY_SHIELD) {
            List<BuffStatValueHolder> mbsvhList = getAllStatups();
            for (BuffStatValueHolder mbsvh : mbsvhList) {
                if (mbsvh.effect.isSkill()) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            }
        }
    }

    public final boolean hasDisease(final Disease dis) {
        chrLock.lock();
        try {
            return diseases.containsKey(dis);
        } finally {
            chrLock.unlock();
        }
    }

    public final int getDiseasesSize() {
        chrLock.lock();
        try {
            return diseases.size();
        } finally {
            chrLock.unlock();
        }
    }

    public Map<Disease, Pair<Long, MobSkill>> getAllDiseases() {
        chrLock.lock();
        try {
            long curtime = Server.getInstance().getCurrentTime();
            Map<Disease, Pair<Long, MobSkill>> ret = new LinkedHashMap<>();

            for (Entry<Disease, Long> de : diseaseExpires.entrySet()) {
                Pair<DiseaseValueHolder, MobSkill> dee = diseases.get(de.getKey());
                DiseaseValueHolder mdvh = dee.getLeft();

                ret.put(de.getKey(), new Pair<>(mdvh.length - (curtime - mdvh.startTime), dee.getRight()));
            }

            return ret;
        } finally {
            chrLock.unlock();
        }
    }

    public void silentApplyDiseases(Map<Disease, Pair<Long, MobSkill>> diseaseMap) {
        chrLock.lock();
        try {
            long curTime = Server.getInstance().getCurrentTime();

            for (Entry<Disease, Pair<Long, MobSkill>> di : diseaseMap.entrySet()) {
                long expTime = curTime + di.getValue().getLeft();

                diseaseExpires.put(di.getKey(), expTime);
                diseases.put(di.getKey(), new Pair<>(new DiseaseValueHolder(curTime, di.getValue().getLeft()), di.getValue().getRight()));
            }
        } finally {
            chrLock.unlock();
        }
    }

    public void announceDiseases() {
        Set<Entry<Disease, Pair<DiseaseValueHolder, MobSkill>>> chrDiseases;

        chrLock.lock();
        try {
            // Poison damage visibility and diseases status visibility, extended through map transitions thanks to Ronan
            if (!this.isLoggedinWorld()) {
                return;
            }

            chrDiseases = new LinkedHashSet<>(diseases.entrySet());
        } finally {
            chrLock.unlock();
        }

        for (Entry<Disease, Pair<DiseaseValueHolder, MobSkill>> di : chrDiseases) {
            Disease disease = di.getKey();
            MobSkill skill = di.getValue().getRight();
            final List<Pair<Disease, Integer>> debuff = Collections.singletonList(new Pair<>(disease, Integer.valueOf(skill.getX())));

            if (disease != Disease.SLOW) {
                map.broadcastMessage(ChannelPacketCreator.getInstance().giveForeignDebuff(id, debuff, skill));
            } else {
                map.broadcastMessage(ChannelPacketCreator.getInstance().giveForeignSlowDebuff(id, debuff, skill));
            }
        }
    }

    public void collectDiseases() {
        for (Character chr : map.getAllPlayers()) {
            int cid = chr.getId();

            for (Entry<Disease, Pair<Long, MobSkill>> di : chr.getAllDiseases().entrySet()) {
                Disease disease = di.getKey();
                MobSkill skill = di.getValue().getRight();
                final List<Pair<Disease, Integer>> debuff = Collections.singletonList(new Pair<>(disease, Integer.valueOf(skill.getX())));

                if (disease != Disease.SLOW) {
                    this.sendPacket(ChannelPacketCreator.getInstance().giveForeignDebuff(cid, debuff, skill));
                } else {
                    this.sendPacket(ChannelPacketCreator.getInstance().giveForeignSlowDebuff(cid, debuff, skill));
                }
            }
        }
    }

    public void giveDebuff(final Disease disease, MobSkill skill) {
        if (!hasDisease(disease) && getDiseasesSize() < 2) {
            chrLock.lock();
            try {
                long curTime = Server.getInstance().getCurrentTime();
                diseaseExpires.put(disease, curTime + skill.getDuration());
                diseases.put(disease, new Pair<>(new DiseaseValueHolder(curTime, skill.getDuration()), skill));
            } finally {
                chrLock.unlock();
            }

            if (disease == Disease.SEDUCE && chair.get() < 0) {
                sitChair(-1);
            }

            final List<Pair<Disease, Integer>> debuff = Collections.singletonList(new Pair<>(disease, Integer.valueOf(skill.getX())));
            sendPacket(ChannelPacketCreator.getInstance().giveDebuff(debuff, skill));

            if (disease != Disease.SLOW) {
                map.broadcastMessage(this, ChannelPacketCreator.getInstance().giveForeignDebuff(id, debuff, skill), false);
            } else {
                map.broadcastMessage(this, ChannelPacketCreator.getInstance().giveForeignSlowDebuff(id, debuff, skill), false);
            }
        }
    }

    public void dispelDebuff(Disease debuff) {
        if (hasDisease(debuff)) {
            long mask = debuff.getValue();
            sendPacket(ChannelPacketCreator.getInstance().cancelDebuff(mask));

            if (debuff != Disease.SLOW) {
                map.broadcastMessage(this, ChannelPacketCreator.getInstance().cancelForeignDebuff(id, mask), false);
            } else {
                map.broadcastMessage(this, ChannelPacketCreator.getInstance().cancelForeignSlowDebuff(id), false);
            }

            chrLock.lock();
            try {
                diseases.remove(debuff);
                diseaseExpires.remove(debuff);
            } finally {
                chrLock.unlock();
            }
        }
    }

    public void dispelDebuffs() {
        dispelDebuff(Disease.CURSE);
        dispelDebuff(Disease.DARKNESS);
        dispelDebuff(Disease.POISON);
        dispelDebuff(Disease.SEAL);
        dispelDebuff(Disease.WEAKEN);
        dispelDebuff(Disease.SLOW);    // thanks Conrad for noticing ZOMBIFY isn't dispellable
    }

    public void purgeDebuffs() {
        dispelDebuff(Disease.SEDUCE);
        dispelDebuff(Disease.ZOMBIFY);
        dispelDebuff(Disease.CONFUSE);
        dispelDebuffs();
    }

    public void cancelAllDebuffs() {
        chrLock.lock();
        try {
            diseases.clear();
            diseaseExpires.clear();
        } finally {
            chrLock.unlock();
        }
    }

    public void dispelSkill(int skillid) {
        List<BuffStatValueHolder> allBuffs = getAllStatups();
        for (BuffStatValueHolder mbsvh : allBuffs) {
            if (skillid == 0) {
                if (mbsvh.effect.isSkill() && (mbsvh.effect.getSourceId() % 10000000 == 1004)) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            } else if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void changeFaceExpression(int emote) {
        long timeNow = Server.getInstance().getCurrentTime();
        // Client allows changing every 2 seconds. Give it a little bit of overhead for packet delays.
        if (timeNow - lastExpression > 1500) {
            lastExpression = timeNow;
            getMap().broadcastMessage(this, ChannelPacketCreator.getInstance().facialExpression(this, emote), false);
        }
    }

    public void doHurtHp() {
        if (!(this.getInventory(InventoryType.EQUIPPED).findById(getMap().getHPDecProtect()) != null || buffMapProtection())) {
            addHP(-getMap().getHPDec());
        }
    }

    public void dropMessage(String message) {
        dropMessage(0, message);
    }

    public void dropMessage(int type, String message) {
        sendPacket(ChannelPacketCreator.getInstance().serverNotice(type, message));
    }

    public void enteredScript(String script, int mapid) {
        if (!entered.containsKey(mapid)) {
            entered.put(mapid, script);
        }
    }

    public void equipChanged() {
        getMap().broadcastUpdateCharLookMessage(this, this);
        equipchanged = true;
        updateLocalStats();
        if (getMessenger() != null) {
            getWorldServer().updateMessenger(getMessenger(), getName(), getWorld(), client.getChannel());
        }
    }

    public void cancelDiseaseExpireTask() {
        if (diseaseExpireTask != null) {
            diseaseExpireTask.cancel(false);
            diseaseExpireTask = null;
        }
    }

    public void diseaseExpireTask() {
        if (diseaseExpireTask == null) {
            diseaseExpireTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    Set<Disease> toExpire = new LinkedHashSet<>();

                    chrLock.lock();
                    try {
                        long curTime = Server.getInstance().getCurrentTime();

                        for (Entry<Disease, Long> de : diseaseExpires.entrySet()) {
                            if (de.getValue() < curTime) {
                                toExpire.add(de.getKey());
                            }
                        }
                    } finally {
                        chrLock.unlock();
                    }

                    for (Disease d : toExpire) {
                        dispelDebuff(d);
                    }
                }
            }, 1500);
        }
    }

    public void cancelBuffExpireTask() {
        if (buffExpireTask != null) {
            buffExpireTask.cancel(false);
            buffExpireTask = null;
        }
    }

    public void buffExpireTask() {
        if (buffExpireTask == null) {
            buffExpireTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    Set<Entry<Integer, Long>> es;
                    List<BuffStatValueHolder> toCancel = new ArrayList<>();

                    effLock.lock();
                    chrLock.lock();
                    try {
                        es = new LinkedHashSet<>(buffExpires.entrySet());

                        long curTime = Server.getInstance().getCurrentTime();
                        for (Entry<Integer, Long> bel : es) {
                            if (curTime >= bel.getValue()) {
                                toCancel.add(buffEffects.get(bel.getKey()).entrySet().iterator().next().getValue());    //rofl
                            }
                        }
                    } finally {
                        chrLock.unlock();
                        effLock.unlock();
                    }

                    for (BuffStatValueHolder mbsvh : toCancel) {
                        cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                    }
                }
            }, 1500);
        }
    }

    public void cancelSkillCooldownTask() {
        if (skillCooldownTask != null) {
            skillCooldownTask.cancel(false);
            skillCooldownTask = null;
        }
    }

    public void skillCooldownTask() {
        if (skillCooldownTask == null) {
            skillCooldownTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    Set<Entry<Integer, CooldownValueHolder>> es;

                    effLock.lock();
                    chrLock.lock();
                    try {
                        es = new LinkedHashSet<>(coolDowns.entrySet());
                    } finally {
                        chrLock.unlock();
                        effLock.unlock();
                    }

                    long curTime = Server.getInstance().getCurrentTime();
                    for (Entry<Integer, CooldownValueHolder> bel : es) {
                        CooldownValueHolder mcdvh = bel.getValue();
                        if (curTime >= mcdvh.startTime + mcdvh.length) {
                            removeCooldown(mcdvh.skillId);
                            sendPacket(ChannelPacketCreator.getInstance().skillCooldown(mcdvh.skillId, 0));
                        }
                    }
                }
            }, 1500);
        }
    }

    public void cancelExpirationTask() {
        if (itemExpireTask != null) {
            itemExpireTask.cancel(false);
            itemExpireTask = null;
        }
    }

    public void expirationTask() {
        if (itemExpireTask == null) {
            itemExpireTask = TimerManager.getInstance().register(() -> {
                boolean deletedCoupon = false;

                long expiration, currenttime = System.currentTimeMillis();
                Set<Skill> keys = getSkills().keySet();
                for (Iterator<Skill> i = keys.iterator(); i.hasNext(); ) {
                    Skill key = i.next();
                    SkillEntry skill = getSkills().get(key);
                    if (skill.expiration != -1 && skill.expiration < currenttime) {
                        changeSkillLevel(key, (byte) -1, 0, -1);
                    }
                }

                List<Item> toberemove = new ArrayList<>();
                for (Inventory inv : inventory) {
                    for (Item item : inv.list()) {
                        expiration = item.getExpiration();

                        if (expiration != -1 && (expiration < currenttime) && ((item.getFlag() & ItemConstants.LOCK) == ItemConstants.LOCK)) {
                            short lock = item.getFlag();
                            lock &= ~(ItemConstants.LOCK);
                            item.setFlag(lock); //Probably need a check, else people can make expiring items into permanent items...
                            item.setExpiration(-1);
                            forceUpdateItem(item);   //TEST :3
                        } else if (expiration != -1 && expiration < currenttime) {
                            if (!ItemConstants.isPet(item.getItemId())) {
                                sendPacket(ChannelPacketCreator.getInstance().itemExpired(item.getItemId()));
                                toberemove.add(item);
                                if (ItemConstants.isRateCoupon(item.getItemId())) {
                                    deletedCoupon = true;
                                }
                            } else {
                                Pet pet = item.getPet();   // thanks Lame for noticing pets not getting despawned after expiration time
                                if (pet != null) {
                                    unequipPet(pet, true);
                                }

                                if (ItemConstants.isExpirablePet(item.getItemId())) {
                                    sendPacket(ChannelPacketCreator.getInstance().itemExpired(item.getItemId()));
                                    toberemove.add(item);
                                } else {
                                    item.setExpiration(-1);
                                    forceUpdateItem(item);
                                }
                            }
                        }
                    }

                    if (!toberemove.isEmpty()) {
                        for (Item item : toberemove) {
                            InventoryManipulator.removeFromSlot(client, inv.getType(), item.getPosition(), item.getQuantity(), true);
                        }

                        ItemInformationProvider ii = ItemInformationProvider.getInstance();
                        for (Item item : toberemove) {
                            List<Integer> toadd = new ArrayList<>();
                            Pair<Integer, String> replace = ii.getReplaceOnExpire(item.getItemId());
                            if (replace.left > 0) {
                                toadd.add(replace.left);
                                if (!replace.right.isEmpty()) {
                                    dropMessage(replace.right);
                                }
                            }
                            for (Integer itemid : toadd) {
                                InventoryManipulator.addById(client, itemid, (short) 1);
                            }
                        }

                        toberemove.clear();
                    }

                }
            }, 60000);
        }
    }

    public void forceUpdateItem(Item item) {
        final List<ModifyInventory> mods = new LinkedList<>();
        mods.add(new ModifyInventory(3, item));
        mods.add(new ModifyInventory(0, item));
        sendPacket(ChannelPacketCreator.getInstance().modifyInventory(true, mods));
    }

    public void gainGachaExp() {
        int expgain = 0;
        long currentgexp = gachaexp.get();
        if ((currentgexp + exp.get()) >= ExpTable.getExpNeededForLevel(level)) {
            expgain += ExpTable.getExpNeededForLevel(level) - exp.get();

            int nextneed = ExpTable.getExpNeededForLevel(level + 1);
            if (currentgexp - expgain >= nextneed) {
                expgain += nextneed;
            }

            this.gachaexp.set((int) (currentgexp - expgain));
        } else {
            expgain = this.gachaexp.getAndSet(0);
        }
        gainExp(expgain, false, true);
        updateSingleStat(Stat.GACHAEXP, this.gachaexp.get());
    }

    public void addGachaExp(int gain) {
        updateSingleStat(Stat.GACHAEXP, gachaexp.addAndGet(gain));
    }

    public void gainExp(int gain) {
        gainExp(gain, true, true);
    }

    public void gainExp(int gain, boolean show, boolean inChat) {
        gainExp(gain, show, inChat, true);
    }

    public void gainExp(int gain, boolean show, boolean inChat, boolean white) {
        gainExp(gain, 0, show, inChat, white);
    }

    public void gainExp(int gain, int party, boolean show, boolean inChat, boolean white) {
        if (hasDisease(Disease.CURSE)) {
            gain *= 0.5;
            party *= 0.5;
        }

        if (gain < 0) {
            gain = Integer.MAX_VALUE;   // integer overflow, heh.
        }

        if (party < 0) {
            party = Integer.MAX_VALUE;  // integer overflow, heh.
        }

        int equip = (int) Math.min((long) (gain / 10) * pendantExp, Integer.MAX_VALUE);

        gainExpInternal(gain, equip, party, show, inChat, white);
    }

    public void loseExp(int loss, boolean show, boolean inChat) {
        loseExp(loss, show, inChat, true);
    }

    public void loseExp(int loss, boolean show, boolean inChat, boolean white) {
        gainExpInternal(-loss, 0, 0, show, inChat, white);
    }

    private void announceExpGain(long gain, int equip, int party, boolean inChat, boolean white) {
        gain = Math.min(gain, Integer.MAX_VALUE);
        if (gain == 0) {
            if (party == 0) {
                return;
            }

            gain = party;
            party = 0;
            white = false;
        }

        sendPacket(ChannelPacketCreator.getInstance().getShowExpGain((int) gain, equip, party, inChat, white));
    }

    private synchronized void gainExpInternal(long gain, int equip, int party, boolean show, boolean inChat, boolean white) {   // need of method synchonization here detected thanks to MedicOP
        long total = Math.max(gain + equip + party, -exp.get());

        if (level < getMaxLevel() && allowExpGain) {
            long leftover = 0;
            long nextExp = exp.get() + total;

            if (nextExp > (long) Integer.MAX_VALUE) {
                total = Integer.MAX_VALUE - exp.get();
                leftover = nextExp - Integer.MAX_VALUE;
            }
            updateSingleStat(Stat.EXP, exp.addAndGet((int) total));
            totalExpGained += total;
            if (show) {
                announceExpGain(gain, equip, party, inChat, white);
            }
            while (exp.get() >= ExpTable.getExpNeededForLevel(level)) {
                levelUp(true);
                if (level == getMaxLevel()) {
                    setExp(0);
                    updateSingleStat(Stat.EXP, 0);
                    break;
                }
            }

            if (leftover > 0) {
                gainExpInternal(leftover, equip, party, false, inChat, white);
            } else {
                lastExpGainTime = System.currentTimeMillis();

                if (YamlConfig.config.server.USE_EXP_GAIN_LOG) {
                    ExpLogRecord expLogRecord = new ExpLogRecord(
                            getWorldServer().getExpRate(),
                            expCoupon,
                            totalExpGained,
                            exp.get(),
                            new Timestamp(lastExpGainTime),
                            id
                    );
                    ExpLogger.putExpLogRecord(expLogRecord);
                }

                totalExpGained = 0;
            }
        }
    }

    private Pair<Integer, Integer> applyFame(int delta) {
        petLock.lock();
        try {
            int newFame = fame + delta;
            if (newFame < -30000) {
                delta = -(30000 + fame);
            } else if (newFame > 30000) {
                delta = 30000 - fame;
            }

            fame += delta;
            return new Pair<>(fame, delta);
        } finally {
            petLock.unlock();
        }
    }

    public void gainFame(int delta) {
        gainFame(delta, null, 0);
    }

    public boolean gainFame(int delta, Character fromPlayer, int mode) {
        Pair<Integer, Integer> fameRes = applyFame(delta);
        delta = fameRes.getRight();
        if (delta != 0) {
            int thisFame = fameRes.getLeft();
            updateSingleStat(Stat.FAME, thisFame);

            if (fromPlayer != null) {
                fromPlayer.sendPacket(ChannelPacketCreator.getInstance().giveFameResponse(mode, getName(), thisFame));
                sendPacket(ChannelPacketCreator.getInstance().receiveFame(mode, fromPlayer.getName()));
            } else {
                sendPacket(ChannelPacketCreator.getInstance().getShowFameGain(delta));
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean canHoldMeso(int gain) {  // thanks lucasziron for pointing out a need to check space availability for mesos on player transactions
        long nextMeso = (long) meso.get() + gain;
        return nextMeso <= Integer.MAX_VALUE;
    }

    public void gainMeso(int gain) {
        gainMeso(gain, true, false, true);
    }

    public void gainMeso(int gain, boolean show) {
        gainMeso(gain, show, false, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions, boolean inChat) {
        long nextMeso;
        petLock.lock();
        try {
            nextMeso = (long) meso.get() + gain;  // thanks Thora for pointing integer overflow here
            if (nextMeso > Integer.MAX_VALUE) {
                gain -= (nextMeso - Integer.MAX_VALUE);
            } else if (nextMeso < 0) {
                gain = -meso.get();
            }
            nextMeso = meso.addAndGet(gain);
        } finally {
            petLock.unlock();
        }

        if (gain != 0) {
            updateSingleStat(Stat.MESO, (int) nextMeso, enableActions);
            if (show) {
                sendPacket(ChannelPacketCreator.getInstance().getShowMesoGain(gain, inChat));
            }
        } else {
            sendPacket(ChannelPacketCreator.getInstance().enableActions());
        }
    }

    public int getAccountID() {
        return accountid;
    }

    public List<PlayerCoolDownValueHolder> getAllCooldowns() {
        List<PlayerCoolDownValueHolder> ret = new ArrayList<>();

        effLock.lock();
        chrLock.lock();
        try {
            for (CooldownValueHolder mcdvh : coolDowns.values()) {
                ret.add(new PlayerCoolDownValueHolder(mcdvh.skillId, mcdvh.startTime, mcdvh.length));
            }
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }

        return ret;
    }

    public BuddyList getBuddylist() {
        return buddylist;
    }

    public Long getBuffedStarttime(BuffStat effect) {
        effLock.lock();
        chrLock.lock();
        try {
            BuffStatValueHolder mbsvh = effects.get(effect);
            if (mbsvh == null) {
                return null;
            }
            return Long.valueOf(mbsvh.startTime);
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public Integer getBuffedValue(BuffStat effect) {
        effLock.lock();
        chrLock.lock();
        try {
            BuffStatValueHolder mbsvh = effects.get(effect);
            if (mbsvh == null) {
                return null;
            }
            return Integer.valueOf(mbsvh.value);
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public int getBuffSource(BuffStat stat) {
        effLock.lock();
        chrLock.lock();
        try {
            BuffStatValueHolder mbsvh = effects.get(stat);
            if (mbsvh == null) {
                return -1;
            }
            return mbsvh.effect.getSourceId();
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public StatEffect getBuffEffect(BuffStat stat) {
        effLock.lock();
        chrLock.lock();
        try {
            BuffStatValueHolder mbsvh = effects.get(stat);
            if (mbsvh == null) {
                return null;
            } else {
                return mbsvh.effect;
            }
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    private List<BuffStatValueHolder> getAllStatups() {
        effLock.lock();
        chrLock.lock();
        try {
            List<BuffStatValueHolder> ret = new ArrayList<>();
            for (Map<BuffStat, BuffStatValueHolder> bel : buffEffects.values()) {
                for (BuffStatValueHolder mbsvh : bel.values()) {
                    ret.add(mbsvh);
                }
            }
            return ret;
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {  // buff values will be stored in an arbitrary order
        effLock.lock();
        chrLock.lock();
        try {
            long curtime = Server.getInstance().getCurrentTime();

            Map<Integer, PlayerBuffValueHolder> ret = new LinkedHashMap<>();
            for (Map<BuffStat, BuffStatValueHolder> bel : buffEffects.values()) {
                for (BuffStatValueHolder mbsvh : bel.values()) {
                    int srcid = mbsvh.effect.getBuffSourceId();
                    if (!ret.containsKey(srcid)) {
                        ret.put(srcid, new PlayerBuffValueHolder((int) (curtime - mbsvh.startTime), mbsvh.effect));
                    }
                }
            }
            return new ArrayList<>(ret.values());
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public boolean hasBuffFromSourceid(int sourceid) {
        effLock.lock();
        chrLock.lock();
        try {
            return buffEffects.containsKey(sourceid);
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public boolean hasActiveBuff(int sourceid) {
        LinkedList<BuffStatValueHolder> allBuffs;

        effLock.lock();
        chrLock.lock();
        try {
            allBuffs = new LinkedList<>(effects.values());
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }

        for (BuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.getBuffSourceId() == sourceid) {
                return true;
            }
        }
        return false;
    }

    private List<Pair<BuffStat, Integer>> getActiveStatupsFromSourceid(int sourceid) { // already under effLock & chrLock
        List<Pair<BuffStat, Integer>> ret = new ArrayList<>();
        List<Pair<BuffStat, Integer>> singletonStatups = new ArrayList<>();
        for (Entry<BuffStat, BuffStatValueHolder> bel : buffEffects.get(sourceid).entrySet()) {
            BuffStat mbs = bel.getKey();
            BuffStatValueHolder mbsvh = effects.get(bel.getKey());

            Pair<BuffStat, Integer> p;
            if (mbsvh != null) {
                p = new Pair<>(mbs, mbsvh.value);
            } else {
                p = new Pair<>(mbs, 0);
            }

            if (!isSingletonStatup(mbs)) {   // thanks resinate, Daddy Egg for pointing out morph issues when updating it along with other statups
                ret.add(p);
            } else {
                singletonStatups.add(p);
            }
        }

        Collections.sort(ret, new Comparator<Pair<BuffStat, Integer>>() {
            @Override
            public int compare(Pair<BuffStat, Integer> p1, Pair<BuffStat, Integer> p2) {
                return p1.getLeft().compareTo(p2.getLeft());
            }
        });

        if (!singletonStatups.isEmpty()) {
            Collections.sort(singletonStatups, new Comparator<Pair<BuffStat, Integer>>() {
                @Override
                public int compare(Pair<BuffStat, Integer> p1, Pair<BuffStat, Integer> p2) {
                    return p1.getLeft().compareTo(p2.getLeft());
                }
            });

            ret.addAll(singletonStatups);
        }

        return ret;
    }

    private void addItemEffectHolder(Integer sourceid, long expirationtime, Map<BuffStat, BuffStatValueHolder> statups) {
        buffEffects.put(sourceid, statups);
        buffExpires.put(sourceid, expirationtime);
    }

    private boolean removeEffectFromItemEffectHolder(Integer sourceid, BuffStat buffStat) {
        Map<BuffStat, BuffStatValueHolder> lbe = buffEffects.get(sourceid);

        if (lbe.remove(buffStat) != null) {
            buffEffectsCount.put(buffStat, (byte) (buffEffectsCount.get(buffStat) - 1));

            if (lbe.isEmpty()) {
                buffEffects.remove(sourceid);
                buffExpires.remove(sourceid);
            }

            return true;
        }

        return false;
    }

    private void removeItemEffectHolder(Integer sourceid) {
        Map<BuffStat, BuffStatValueHolder> be = buffEffects.remove(sourceid);
        if (be != null) {
            for (Entry<BuffStat, BuffStatValueHolder> bei : be.entrySet()) {
                buffEffectsCount.put(bei.getKey(), (byte) (buffEffectsCount.get(bei.getKey()) - 1));
            }
        }

        buffExpires.remove(sourceid);
    }

    private BuffStatValueHolder fetchBestEffectFromItemEffectHolder(BuffStat mbs) {
        Pair<Integer, Integer> max = new Pair<>(Integer.MIN_VALUE, 0);
        BuffStatValueHolder mbsvh = null;
        for (Entry<Integer, Map<BuffStat, BuffStatValueHolder>> bpl : buffEffects.entrySet()) {
            BuffStatValueHolder mbsvhi = bpl.getValue().get(mbs);
            if (mbsvhi != null) {
                if (!mbsvhi.effect.isActive(this)) {
                    continue;
                }

                if (mbsvhi.value > max.left) {
                    max = new Pair<>(mbsvhi.value, mbsvhi.effect.getStatups().size());
                    mbsvh = mbsvhi;
                } else if (mbsvhi.value == max.left && mbsvhi.effect.getStatups().size() > max.right) {
                    max = new Pair<>(mbsvhi.value, mbsvhi.effect.getStatups().size());
                    mbsvh = mbsvhi;
                }
            }
        }

        if (mbsvh != null) {
            effects.put(mbs, mbsvh);
        }
        return mbsvh;
    }

    private void extractBuffValue(int sourceid, BuffStat stat) {
        chrLock.lock();
        try {
            removeEffectFromItemEffectHolder(sourceid, stat);
        } finally {
            chrLock.unlock();
        }
    }

    public void cancelAllBuffs(boolean softcancel) {
        if (softcancel) {
            effLock.lock();
            chrLock.lock();
            try {
                cancelEffectFromBuffStat(BuffStat.SUMMON);
                cancelEffectFromBuffStat(BuffStat.PUPPET);
                cancelEffectFromBuffStat(BuffStat.COMBO);

                effects.clear();

                for (Integer srcid : new ArrayList<>(buffEffects.keySet())) {
                    removeItemEffectHolder(srcid);
                }
            } finally {
                chrLock.unlock();
                effLock.unlock();
            }
        } else {
            Map<StatEffect, Long> mseBuffs = new LinkedHashMap<>();

            effLock.lock();
            chrLock.lock();
            try {
                for (Entry<Integer, Map<BuffStat, BuffStatValueHolder>> bpl : buffEffects.entrySet()) {
                    for (Entry<BuffStat, BuffStatValueHolder> mbse : bpl.getValue().entrySet()) {
                        mseBuffs.put(mbse.getValue().effect, mbse.getValue().startTime);
                    }
                }
            } finally {
                chrLock.unlock();
                effLock.unlock();
            }

            for (Entry<StatEffect, Long> mse : mseBuffs.entrySet()) {
                cancelEffect(mse.getKey(), false, mse.getValue());
            }
        }
    }

    private void dropBuffStats(List<Pair<BuffStat, BuffStatValueHolder>> effectsToCancel) {
        for (Pair<BuffStat, BuffStatValueHolder> cancelEffectCancelTasks : effectsToCancel) {
            //boolean nestedCancel = false;

            chrLock.lock();
            try {
                /*
                if (buffExpires.get(cancelEffectCancelTasks.getRight().effect.getBuffSourceId()) != null) {
                    nestedCancel = true;
                }*/

                if (cancelEffectCancelTasks.getRight().bestApplied) {
                    fetchBestEffectFromItemEffectHolder(cancelEffectCancelTasks.getLeft());
                }
            } finally {
                chrLock.unlock();
            }

            /*
            if (nestedCancel) {
                this.cancelEffect(cancelEffectCancelTasks.getRight().effect, false, -1, false);
            }*/
        }
    }

    private List<Pair<BuffStat, BuffStatValueHolder>> deregisterBuffStats(Map<BuffStat, BuffStatValueHolder> stats) {
        chrLock.lock();
        try {
            List<Pair<BuffStat, BuffStatValueHolder>> effectsToCancel = new ArrayList<>(stats.size());
            for (Entry<BuffStat, BuffStatValueHolder> stat : stats.entrySet()) {
                int sourceid = stat.getValue().effect.getBuffSourceId();

                if (!buffEffects.containsKey(sourceid)) {
                    buffExpires.remove(sourceid);
                }

                BuffStat mbs = stat.getKey();
                effectsToCancel.add(new Pair<>(mbs, stat.getValue()));

                BuffStatValueHolder mbsvh = effects.get(mbs);
                if (mbsvh != null && mbsvh.effect.getBuffSourceId() == sourceid) {
                    mbsvh.bestApplied = true;
                    effects.remove(mbs);

                    if (mbs == BuffStat.RECOVERY) {
                        if (recoveryTask != null) {
                            recoveryTask.cancel(false);
                            recoveryTask = null;
                        }
                    } else if (mbs == BuffStat.SUMMON || mbs == BuffStat.PUPPET) {
                        int summonId = mbsvh.effect.getSourceId();

                        Summon summon = summons.get(summonId);
                        if (summon != null) {
                            getMap().broadcastMessage(ChannelPacketCreator.getInstance().removeSummon(summon, true), summon.getPosition());
                            getMap().removeMapObject(summon);
                            removeVisibleMapObject(summon);

                            summons.remove(summonId);
                            if (summon.isPuppet()) {
                                map.removePlayerPuppet(this);
                            }
                        }
                    } else if (mbs == BuffStat.DRAGONBLOOD) {
                        dragonBloodSchedule.cancel(false);
                        dragonBloodSchedule = null;
                    } else if (mbs == BuffStat.HPREC || mbs == BuffStat.MPREC) {
                        if (mbs == BuffStat.HPREC) {
                            extraHpRec = 0;
                        } else {
                            extraMpRec = 0;
                        }

                        if (extraRecoveryTask != null) {
                            extraRecoveryTask.cancel(false);
                            extraRecoveryTask = null;
                        }

                        if (extraHpRec != 0 || extraMpRec != 0) {
                            startExtraTaskInternal(extraHpRec, extraMpRec, extraRecInterval);
                        }
                    }
                }
            }

            return effectsToCancel;
        } finally {
            chrLock.unlock();
        }
    }

    public void cancelEffect(int itemId) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        cancelEffect(ii.getItemEffect(itemId), false, -1);
    }

    public boolean cancelEffect(StatEffect effect, boolean overwrite, long startTime) {
        boolean ret;

        prtLock.lock();
        effLock.lock();
        try {
            ret = cancelEffect(effect, overwrite, startTime, true);
        } finally {
            effLock.unlock();
            prtLock.unlock();
        }

        return ret;
    }

    private boolean isUpdatingEffect(Set<StatEffect> activeEffects, StatEffect mse) {
        if (mse == null) {
            return false;
        }

        // thanks xinyifly for noticing "Speed Infusion" crashing game when updating buffs during map transition
        boolean active = mse.isActive(this);
        if (active) {
            return !activeEffects.contains(mse);
        } else {
            return activeEffects.contains(mse);
        }
    }

    public void updateActiveEffects() {
        effLock.lock();     // thanks davidlafriniere, maple006, RedHat for pointing a deadlock occurring here
        try {
            Set<BuffStat> updatedBuffs = new LinkedHashSet<>();
            Set<StatEffect> activeEffects = new LinkedHashSet<>();

            for (BuffStatValueHolder mse : effects.values()) {
                activeEffects.add(mse.effect);
            }

            for (Map<BuffStat, BuffStatValueHolder> buff : buffEffects.values()) {
                StatEffect mse = getEffectFromBuffSource(buff);
                if (isUpdatingEffect(activeEffects, mse)) {
                    for (Pair<BuffStat, Integer> p : mse.getStatups()) {
                        updatedBuffs.add(p.getLeft());
                    }
                }
            }

            for (BuffStat mbs : updatedBuffs) {
                effects.remove(mbs);
            }

            updateEffects(updatedBuffs);
        } finally {
            effLock.unlock();
        }
    }

    private void updateEffects(Set<BuffStat> removedStats) {
        effLock.lock();
        chrLock.lock();
        try {
            Set<BuffStat> retrievedStats = new LinkedHashSet<>();

            for (BuffStat mbs : removedStats) {
                fetchBestEffectFromItemEffectHolder(mbs);

                BuffStatValueHolder mbsvh = effects.get(mbs);
                if (mbsvh != null) {
                    for (Pair<BuffStat, Integer> statup : mbsvh.effect.getStatups()) {
                        retrievedStats.add(statup.getLeft());
                    }
                }
            }

            propagateBuffEffectUpdates(new LinkedHashMap<Integer, Pair<StatEffect, Long>>(), retrievedStats, removedStats);
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    private boolean cancelEffect(StatEffect effect, boolean overwrite, long startTime, boolean firstCancel) {
        Set<BuffStat> removedStats = new LinkedHashSet<>();
        dropBuffStats(cancelEffectInternal(effect, overwrite, startTime, removedStats));
        updateLocalStats();
        updateEffects(removedStats);

        return !removedStats.isEmpty();
    }

    private List<Pair<BuffStat, BuffStatValueHolder>> cancelEffectInternal(StatEffect effect, boolean overwrite, long startTime, Set<BuffStat> removedStats) {
        Map<BuffStat, BuffStatValueHolder> buffstats = null;
        BuffStat ombs;
        if (!overwrite) {   // is removing the source effect, meaning every effect from this srcid is being purged
            buffstats = extractCurrentBuffStats(effect);
        } else if ((ombs = getSingletonStatupFromEffect(effect)) != null) {   // removing all effects of a buff having non-shareable buff stat.
            BuffStatValueHolder mbsvh = effects.get(ombs);
            if (mbsvh != null) {
                buffstats = extractCurrentBuffStats(mbsvh.effect);
            }
        }

        if (buffstats == null) {            // all else, is dropping ALL current statups that uses same stats as the given effect
            buffstats = extractLeastRelevantStatEffectsIfFull(effect);
        }

        if (effect.isMapChair()) {
            stopChairTask();
        }

        List<Pair<BuffStat, BuffStatValueHolder>> toCancel = deregisterBuffStats(buffstats);
        if (effect.isMonsterRiding()) {
            this.getClient().getWorldServer().unregisterMountHunger(this);
            this.getMount().setActive(false);
        }

        if (!overwrite) {
            removedStats.addAll(buffstats.keySet());
        }

        return toCancel;
    }

    public void cancelEffectFromBuffStat(BuffStat stat) {
        BuffStatValueHolder effect;

        effLock.lock();
        chrLock.lock();
        try {
            effect = effects.get(stat);
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
        if (effect != null) {
            cancelEffect(effect.effect, false, -1);
        }
    }

    public void cancelBuffStats(BuffStat stat) {
        effLock.lock();
        try {
            List<Pair<Integer, BuffStatValueHolder>> cancelList = new LinkedList<>();

            chrLock.lock();
            try {
                for (Entry<Integer, Map<BuffStat, BuffStatValueHolder>> bel : this.buffEffects.entrySet()) {
                    BuffStatValueHolder beli = bel.getValue().get(stat);
                    if (beli != null) {
                        cancelList.add(new Pair<>(bel.getKey(), beli));
                    }
                }
            } finally {
                chrLock.unlock();
            }

            Map<BuffStat, BuffStatValueHolder> buffStatList = new LinkedHashMap<>();
            for (Pair<Integer, BuffStatValueHolder> p : cancelList) {
                buffStatList.put(stat, p.getRight());
                extractBuffValue(p.getLeft(), stat);
                dropBuffStats(deregisterBuffStats(buffStatList));
            }
        } finally {
            effLock.unlock();
        }

        cancelPlayerBuffs(Collections.singletonList(stat));
    }

    private Map<BuffStat, BuffStatValueHolder> extractCurrentBuffStats(StatEffect effect) {
        chrLock.lock();
        try {
            Map<BuffStat, BuffStatValueHolder> stats = new LinkedHashMap<>();
            Map<BuffStat, BuffStatValueHolder> buffList = buffEffects.remove(effect.getBuffSourceId());

            if (buffList != null) {
                for (Entry<BuffStat, BuffStatValueHolder> stateffect : buffList.entrySet()) {
                    stats.put(stateffect.getKey(), stateffect.getValue());
                    buffEffectsCount.put(stateffect.getKey(), (byte) (buffEffectsCount.get(stateffect.getKey()) - 1));
                }
            }

            return stats;
        } finally {
            chrLock.unlock();
        }
    }

    private Map<BuffStat, BuffStatValueHolder> extractLeastRelevantStatEffectsIfFull(StatEffect effect) {
        Map<BuffStat, BuffStatValueHolder> extractedStatBuffs = new LinkedHashMap<>();

        chrLock.lock();
        try {
            Map<BuffStat, Byte> stats = new LinkedHashMap<>();
            Map<BuffStat, BuffStatValueHolder> minStatBuffs = new LinkedHashMap<>();

            for (Entry<Integer, Map<BuffStat, BuffStatValueHolder>> mbsvhi : buffEffects.entrySet()) {
                for (Entry<BuffStat, BuffStatValueHolder> mbsvhe : mbsvhi.getValue().entrySet()) {
                    BuffStat mbs = mbsvhe.getKey();
                    Byte b = stats.get(mbs);

                    if (b != null) {
                        stats.put(mbs, (byte) (b + 1));
                        if (mbsvhe.getValue().value < minStatBuffs.get(mbs).value) {
                            minStatBuffs.put(mbs, mbsvhe.getValue());
                        }
                    } else {
                        stats.put(mbs, (byte) 1);
                        minStatBuffs.put(mbs, mbsvhe.getValue());
                    }
                }
            }

            Set<BuffStat> effectStatups = new LinkedHashSet<>();
            for (Pair<BuffStat, Integer> efstat : effect.getStatups()) {
                effectStatups.add(efstat.getLeft());
            }

            for (Entry<BuffStat, Byte> it : stats.entrySet()) {
                boolean uniqueBuff = isSingletonStatup(it.getKey());

                if (it.getValue() >= (!uniqueBuff ? YamlConfig.config.server.MAX_MONITORED_BUFFSTATS : 1) && effectStatups.contains(it.getKey())) {
                    BuffStatValueHolder mbsvh = minStatBuffs.get(it.getKey());

                    Map<BuffStat, BuffStatValueHolder> lpbe = buffEffects.get(mbsvh.effect.getBuffSourceId());
                    lpbe.remove(it.getKey());
                    buffEffectsCount.put(it.getKey(), (byte) (buffEffectsCount.get(it.getKey()) - 1));

                    if (lpbe.isEmpty()) {
                        buffEffects.remove(mbsvh.effect.getBuffSourceId());
                    }
                    extractedStatBuffs.put(it.getKey(), mbsvh);
                }
            }
        } finally {
            chrLock.unlock();
        }

        return extractedStatBuffs;
    }

    private void cancelInactiveBuffStats(Set<BuffStat> retrievedStats, Set<BuffStat> removedStats) {
        List<BuffStat> inactiveStats = new LinkedList<>();
        for (BuffStat mbs : removedStats) {
            if (!retrievedStats.contains(mbs)) {
                inactiveStats.add(mbs);
            }
        }

        if (!inactiveStats.isEmpty()) {
            sendPacket(ChannelPacketCreator.getInstance().cancelBuff(inactiveStats));
            getMap().broadcastMessage(this, ChannelPacketCreator.getInstance().cancelForeignBuff(getId(), inactiveStats), false);
        }
    }

    private List<Pair<Integer, Pair<StatEffect, Long>>> propagatePriorityBuffEffectUpdates(Set<BuffStat> retrievedStats) {
        List<Pair<Integer, Pair<StatEffect, Long>>> priorityUpdateEffects = new LinkedList<>();
        Map<BuffStatValueHolder, StatEffect> yokeStats = new LinkedHashMap<>();

        // priority buffsources: override buffstats for the client to perceive those as "currently buffed"
        Set<BuffStatValueHolder> mbsvhList = new LinkedHashSet<>();
        for (BuffStatValueHolder mbsvh : getAllStatups()) {
            mbsvhList.add(mbsvh);
        }

        for (BuffStatValueHolder mbsvh : mbsvhList) {
            StatEffect mse = mbsvh.effect;
            int buffSourceId = mse.getBuffSourceId();
            if (isPriorityBuffSourceid(buffSourceId) && !hasActiveBuff(buffSourceId)) {
                for (Pair<BuffStat, Integer> ps : mse.getStatups()) {
                    BuffStat mbs = ps.getLeft();
                    if (retrievedStats.contains(mbs)) {
                        BuffStatValueHolder mbsvhe = effects.get(mbs);

                        // this shouldn't even be null...
                        //if (mbsvh != null) {
                        yokeStats.put(mbsvh, mbsvhe.effect);
                        //}
                    }
                }
            }
        }

        for (Entry<BuffStatValueHolder, StatEffect> e : yokeStats.entrySet()) {
            BuffStatValueHolder mbsvhPriority = e.getKey();
            StatEffect mseActive = e.getValue();

            priorityUpdateEffects.add(new Pair<>(mseActive.getBuffSourceId(), new Pair<>(mbsvhPriority.effect, mbsvhPriority.startTime)));
        }

        return priorityUpdateEffects;
    }

    private void propagateBuffEffectUpdates(Map<Integer, Pair<StatEffect, Long>> retrievedEffects, Set<BuffStat> retrievedStats, Set<BuffStat> removedStats) {
        cancelInactiveBuffStats(retrievedStats, removedStats);
        if (retrievedStats.isEmpty()) {
            return;
        }

        Map<BuffStat, Pair<Integer, StatEffect>> maxBuffValue = new LinkedHashMap<>();
        for (BuffStat mbs : retrievedStats) {
            BuffStatValueHolder mbsvh = effects.get(mbs);
            if (mbsvh != null) {
                retrievedEffects.put(mbsvh.effect.getBuffSourceId(), new Pair<>(mbsvh.effect, mbsvh.startTime));
            }

            maxBuffValue.put(mbs, new Pair<>(Integer.MIN_VALUE, null));
        }

        Map<StatEffect, Integer> updateEffects = new LinkedHashMap<>();

        List<StatEffect> recalcMseList = new LinkedList<>();
        for (Entry<Integer, Pair<StatEffect, Long>> re : retrievedEffects.entrySet()) {
            recalcMseList.add(re.getValue().getLeft());
        }

        do {
            List<StatEffect> mseList = recalcMseList;
            recalcMseList = new LinkedList<>();

            for (StatEffect mse : mseList) {
                int maxEffectiveStatup = Integer.MIN_VALUE;
                for (Pair<BuffStat, Integer> st : mse.getStatups()) {
                    BuffStat mbs = st.getLeft();

                    boolean relevantStatup = mbs != BuffStat.MATK;
                    // not relevant for non-mages

                    Pair<Integer, StatEffect> mbv = maxBuffValue.get(mbs);
                    if (mbv == null) {
                        continue;
                    }

                    if (mbv.getLeft() < st.getRight()) {
                        StatEffect msbe = mbv.getRight();
                        if (msbe != null) {
                            recalcMseList.add(msbe);
                        }

                        maxBuffValue.put(mbs, new Pair<>(st.getRight(), mse));

                        if (relevantStatup) {
                            if (maxEffectiveStatup < st.getRight()) {
                                maxEffectiveStatup = st.getRight();
                            }
                        }
                    }
                }

                updateEffects.put(mse, maxEffectiveStatup);
            }
        } while (!recalcMseList.isEmpty());

        List<StatEffect> updateEffectsList = sortEffectsList(updateEffects);

        List<Pair<Integer, Pair<StatEffect, Long>>> toUpdateEffects = new LinkedList<>();
        for (StatEffect mse : updateEffectsList) {
            toUpdateEffects.add(new Pair<>(mse.getBuffSourceId(), retrievedEffects.get(mse.getBuffSourceId())));
        }

        List<Pair<BuffStat, Integer>> activeStatups = new LinkedList<>();
        for (Pair<Integer, Pair<StatEffect, Long>> lmse : toUpdateEffects) {
            Pair<StatEffect, Long> msel = lmse.getRight();

            for (Pair<BuffStat, Integer> statup : getActiveStatupsFromSourceid(lmse.getLeft())) {
                activeStatups.add(statup);
            }

            msel.getLeft().updateBuffEffect(this, activeStatups, msel.getRight());
            activeStatups.clear();
        }

        List<Pair<Integer, Pair<StatEffect, Long>>> priorityEffects = propagatePriorityBuffEffectUpdates(retrievedStats);
        for (Pair<Integer, Pair<StatEffect, Long>> lmse : priorityEffects) {
            Pair<StatEffect, Long> msel = lmse.getRight();

            for (Pair<BuffStat, Integer> statup : getActiveStatupsFromSourceid(lmse.getLeft())) {
                activeStatups.add(statup);
            }

            msel.getLeft().updateBuffEffect(this, activeStatups, msel.getRight());
            activeStatups.clear();
        }
    }

    private void addItemEffectHolderCount(BuffStat stat) {
        Byte val = buffEffectsCount.get(stat);
        if (val != null) {
            val = (byte) (val + 1);
        } else {
            val = (byte) 1;
        }

        buffEffectsCount.put(stat, val);
    }

    public void registerEffect(StatEffect effect, long starttime, long expirationtime, boolean isSilent) {
        if (effect.isRecovery()) {
            int healInterval = (YamlConfig.config.server.USE_ULTRA_RECOVERY) ? 2000 : 5000;
            final byte heal = (byte) effect.getX();

            chrLock.lock();
            try {
                if (recoveryTask != null) {
                    recoveryTask.cancel(false);
                }

                recoveryTask = TimerManager.getInstance().register(new Runnable() {
                    @Override
                    public void run() {
                        if (getBuffSource(BuffStat.RECOVERY) == -1) {
                            chrLock.lock();
                            try {
                                if (recoveryTask != null) {
                                    recoveryTask.cancel(false);
                                    recoveryTask = null;
                                }
                            } finally {
                                chrLock.unlock();
                            }

                            return;
                        }

                        addHP(heal);
                        sendPacket(ChannelPacketCreator.getInstance().showOwnRecovery(heal));
                        getMap().broadcastMessage(Character.this, ChannelPacketCreator.getInstance().showRecovery(id, heal), false);
                    }
                }, healInterval, healInterval);
            } finally {
                chrLock.unlock();
            }
        } else if (effect.getHpRRate() > 0 || effect.getMpRRate() > 0) {
            if (effect.getHpRRate() > 0) {
                extraHpRec = effect.getHpR();
                extraRecInterval = effect.getHpRRate();
            }

            if (effect.getMpRRate() > 0) {
                extraMpRec = effect.getMpR();
                extraRecInterval = effect.getMpRRate();
            }

            chrLock.lock();
            try {
                stopExtraTask();
                startExtraTask(extraHpRec, extraMpRec, extraRecInterval);   // HP & MP sharing the same task holder
            } finally {
                chrLock.unlock();
            }

        } else if (effect.isMapChair()) {
            startChairTask();
        }

        prtLock.lock();
        effLock.lock();
        chrLock.lock();
        try {
            Integer sourceid = effect.getBuffSourceId();
            Map<BuffStat, BuffStatValueHolder> toDeploy;
            Map<BuffStat, BuffStatValueHolder> appliedStatups = new LinkedHashMap<>();

            for (Pair<BuffStat, Integer> ps : effect.getStatups()) {
                appliedStatups.put(ps.getLeft(), new BuffStatValueHolder(effect, starttime, ps.getRight()));
            }

            boolean active = effect.isActive(this);
            if (YamlConfig.config.server.USE_BUFF_MOST_SIGNIFICANT) {
                toDeploy = new LinkedHashMap<>();
                Map<Integer, Pair<StatEffect, Long>> retrievedEffects = new LinkedHashMap<>();
                Set<BuffStat> retrievedStats = new LinkedHashSet<>();
                for (Entry<BuffStat, BuffStatValueHolder> statup : appliedStatups.entrySet()) {
                    BuffStatValueHolder mbsvh = effects.get(statup.getKey());
                    BuffStatValueHolder statMbsvh = statup.getValue();

                    if (active) {
                        if (mbsvh == null || mbsvh.value < statMbsvh.value || (mbsvh.value == statMbsvh.value && mbsvh.effect.getStatups().size() <= statMbsvh.effect.getStatups().size())) {
                            toDeploy.put(statup.getKey(), statMbsvh);
                        } else {
                            if (!isSingletonStatup(statup.getKey())) {
                                for (Pair<BuffStat, Integer> mbs : mbsvh.effect.getStatups()) {
                                    retrievedStats.add(mbs.getLeft());
                                }
                            }
                        }
                    }

                    addItemEffectHolderCount(statup.getKey());
                }

                // should also propagate update from buffs shared with priority sourceids
                Set<BuffStat> updated = appliedStatups.keySet();
                for (BuffStatValueHolder mbsvh : this.getAllStatups()) {
                    if (isPriorityBuffSourceid(mbsvh.effect.getBuffSourceId())) {
                        for (Pair<BuffStat, Integer> p : mbsvh.effect.getStatups()) {
                            if (updated.contains(p.getLeft())) {
                                retrievedStats.add(p.getLeft());
                            }
                        }
                    }
                }

                if (!isSilent) {
                    addItemEffectHolder(sourceid, expirationtime, appliedStatups);
                    for (Entry<BuffStat, BuffStatValueHolder> statup : toDeploy.entrySet()) {
                        effects.put(statup.getKey(), statup.getValue());
                    }

                    if (active) {
                        retrievedEffects.put(sourceid, new Pair<>(effect, starttime));
                    }

                    propagateBuffEffectUpdates(retrievedEffects, retrievedStats, new LinkedHashSet<BuffStat>());
                }
            } else {
                for (Entry<BuffStat, BuffStatValueHolder> statup : appliedStatups.entrySet()) {
                    addItemEffectHolderCount(statup.getKey());
                }

                toDeploy = (active ? appliedStatups : new LinkedHashMap<BuffStat, BuffStatValueHolder>());
            }

            addItemEffectHolder(sourceid, expirationtime, appliedStatups);
            for (Entry<BuffStat, BuffStatValueHolder> statup : toDeploy.entrySet()) {
                effects.put(statup.getKey(), statup.getValue());
            }
        } finally {
            chrLock.unlock();
            effLock.unlock();
            prtLock.unlock();
        }

        updateLocalStats();
    }

    public boolean unregisterChairBuff() {
        if (!YamlConfig.config.server.USE_CHAIR_EXTRAHEAL) {
            return false;
        }

        int skillId = getJobMapChair();
        int skillLv = getSkillLevel(skillId);
        if (skillLv > 0) {
            StatEffect mapChairSkill = SkillFactory.getSkill(skillId).getEffect(skillLv);
            return cancelEffect(mapChairSkill, false, -1);
        }

        return false;
    }

    public boolean registerChairBuff() {
        if (!YamlConfig.config.server.USE_CHAIR_EXTRAHEAL) {
            return false;
        }

        int skillId = getJobMapChair();
        int skillLv = getSkillLevel(skillId);
        if (skillLv > 0) {
            StatEffect mapChairSkill = SkillFactory.getSkill(skillId).getEffect(skillLv);
            mapChairSkill.applyTo(this);
            return true;
        }

        return false;
    }

    public int getChair() {
        return chair.get();
    }

    private void setChair(int chair) {
        this.chair.set(chair);
    }

    public String getChalkboard() {
        return this.chalktext;
    }

    public void setChalkboard(String text) {
        this.chalktext = text;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client c) {
        this.client = c;
    }

    private List<QuestStatus> getQuests() {
        synchronized (quests) {
            return new ArrayList<>(quests.values());
        }
    }

    public final List<QuestStatus> getCompletedQuests() {
        List<QuestStatus> ret = new LinkedList<>();
        for (QuestStatus qs : getQuests()) {
            if (qs.getStatus().equals(QuestStatus.Status.COMPLETED)) {
                ret.add(qs);
            }
        }

        return Collections.unmodifiableList(ret);
    }

    public int getCurrentCI() {
        return ci;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public int getCurrentType() {
        return currentType;
    }

    public Collection<Door> getDoors() {
        prtLock.lock();
        try {
            return (party != null ? Collections.unmodifiableCollection(party.getDoors().values()) : (pdoor != null ? Collections.singleton(pdoor) : new LinkedHashSet<Door>()));
        } finally {
            prtLock.unlock();
        }
    }

    public Door getPlayerDoor() {
        prtLock.lock();
        try {
            return pdoor;
        } finally {
            prtLock.unlock();
        }
    }

    public Door getMainTownDoor() {
        for (Door door : getDoors()) {
            if (door.getTownPortal().getId() == 0x80) {
                return door;
            }
        }

        return null;
    }

    public void applyPartyDoor(Door door, boolean partyUpdate) {
        Party chrParty;
        prtLock.lock();
        try {
            if (!partyUpdate) {
                pdoor = door;
            }

            chrParty = getParty();
            if (chrParty != null) {
                chrParty.addDoor(id, door);
            }
        } finally {
            prtLock.unlock();
        }

        silentPartyUpdateInternal(chrParty);
    }

    public Door removePartyDoor(boolean partyUpdate) {
        Door ret = null;
        Party chrParty;

        prtLock.lock();
        try {
            chrParty = getParty();
            if (chrParty != null) {
                chrParty.removeDoor(id);
            }

            if (!partyUpdate) {
                ret = pdoor;
                pdoor = null;
            }
        } finally {
            prtLock.unlock();
        }

        silentPartyUpdateInternal(chrParty);
        return ret;
    }

    private void removePartyDoor(Party formerParty) {    // player is no longer registered at this party
        formerParty.removeDoor(id);
    }

    public int getEnergyBar() {
        return energybar;
    }

    public void setEnergyBar(int set) {
        energybar = set;
    }

    public void resetExcluded(int petId) {
        chrLock.lock();
        try {
            Set<Integer> petExclude = excluded.get(petId);

            if (petExclude != null) {
                petExclude.clear();
            } else {
                excluded.put(petId, new LinkedHashSet<Integer>());
            }
        } finally {
            chrLock.unlock();
        }
    }

    public void addExcluded(int petId, int x) {
        chrLock.lock();
        try {
            excluded.get(petId).add(x);
        } finally {
            chrLock.unlock();
        }
    }

    public void commitExcludedItems() {
        Map<Integer, Set<Integer>> petExcluded = this.getExcluded();

        chrLock.lock();
        try {
            excludedItems.clear();
        } finally {
            chrLock.unlock();
        }

        for (Entry<Integer, Set<Integer>> pe : petExcluded.entrySet()) {
            byte petIndex = this.getPetIndex(pe.getKey());
            if (petIndex < 0) {
                continue;
            }

            Set<Integer> exclItems = pe.getValue();
            if (!exclItems.isEmpty()) {
                sendPacket(ChannelPacketCreator.getInstance().loadExceptionList(this.getId(), pe.getKey(), petIndex, new ArrayList<>(exclItems)));

                chrLock.lock();
                try {
                    for (Integer itemid : exclItems) {
                        excludedItems.add(itemid);
                    }
                } finally {
                    chrLock.unlock();
                }
            }
        }
    }

    public void exportExcludedItems(Client c) {
        Map<Integer, Set<Integer>> petExcluded = this.getExcluded();
        for (Entry<Integer, Set<Integer>> pe : petExcluded.entrySet()) {
            byte petIndex = this.getPetIndex(pe.getKey());
            if (petIndex < 0) {
                continue;
            }

            Set<Integer> exclItems = pe.getValue();
            if (!exclItems.isEmpty()) {
                c.sendPacket(ChannelPacketCreator.getInstance().loadExceptionList(this.getId(), pe.getKey(), petIndex, new ArrayList<>(exclItems)));
            }
        }
    }

    public Map<Integer, Set<Integer>> getExcluded() {
        chrLock.lock();
        try {
            return Collections.unmodifiableMap(excluded);
        } finally {
            chrLock.unlock();
        }
    }

    public Set<Integer> getExcludedItems() {
        chrLock.lock();
        try {
            return Collections.unmodifiableSet(excludedItems);
        } finally {
            chrLock.unlock();
        }
    }

    public int getExp() {
        return exp.get();
    }

    public void setExp(int amount) {
        this.exp.set(amount);
    }

    public int getGachaExp() {
        return gachaexp.get();
    }

    public void setGachaExp(int amount) {
        this.gachaexp.set(amount);
    }

    public boolean hasNoviceExpRate() {
        return YamlConfig.config.server.USE_ENFORCE_NOVICE_EXPRATE && isBeginnerJob() && level < 11;
    }

    public int getExpRate() {
        if (hasNoviceExpRate()) {   // base exp rate 1x for early levels idea thanks to Vcoc
            return 1;
        }

        return expRate;
    }

    public int getCouponExpRate() {
        return expCoupon;
    }

    public int getRawExpRate() {
        return expRate / (expCoupon * getWorldServer().getExpRate());
    }

    public int getDropRate() {
        return dropRate;
    }

    public int getCouponDropRate() {
        return dropCoupon;
    }

    public int getRawDropRate() {
        return dropRate / (dropCoupon * getWorldServer().getDropRate());
    }

    public int getBossDropRate() {
        World w = getWorldServer();
        return (dropRate / w.getDropRate()) * w.getBossDropRate();
    }

    public int getMesoRate() {
        return mesoRate;
    }

    public int getCouponMesoRate() {
        return mesoCoupon;
    }

    public int getRawMesoRate() {
        return mesoRate / (mesoCoupon * getWorldServer().getMesoRate());
    }

    public int getQuestExpRate() {
        if (hasNoviceExpRate()) {
            return 1;
        }

        World w = getWorldServer();
        return w.getExpRate() * w.getQuestRate();
    }

    public int getQuestMesoRate() {
        World w = getWorldServer();
        return w.getMesoRate() * w.getQuestRate();
    }

    public float getCardRate(int itemid) {
        float rate = 100.0f;

        if (itemid == 0) {
            StatEffect mseMeso = getBuffEffect(BuffStat.MESO_UP_BY_ITEM);
            if (mseMeso != null) {
                rate += mseMeso.getCardRate(itemid);
            }
        } else {
            StatEffect mseItem = getBuffEffect(BuffStat.ITEM_UP_BY_ITEM);
            if (mseItem != null) {
                rate += mseItem.getCardRate(itemid);
            }
        }

        return rate / 100;
    }

    public int getFace() {
        return face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public int getFame() {
        return fame;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public int getFamilyId() {
        return familyId;
    }

    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    public void setUsedStorage() {
        usedStorage = true;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getGuildId() {
        return guildid;
    }

    public void setGuildId(int _id) {
        guildid = _id;
    }

    public int getGuildRank() {
        return guildRank;
    }

    public void setGuildRank(int _rank) {
        guildRank = _rank;
    }

    public int getHair() {
        return hair;
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public int getId() {
        return id;
    }

    public int getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public Inventory getInventory(InventoryType type) {
        return inventory[type.ordinal()];
    }

    public int getItemEffect() {
        return itemEffect;
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    public boolean haveItemWithId(int itemid, boolean checkEquipped) {
        return (inventory[ItemConstants.getInventoryType(itemid).ordinal()].findById(itemid) != null)
                || (checkEquipped && inventory[InventoryType.EQUIPPED.ordinal()].findById(itemid) != null);
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        int count = inventory[ItemConstants.getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped) {
            count += inventory[InventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return count;
    }

    public int getCleanItemQuantity(int itemid, boolean checkEquipped) {
        int count = inventory[ItemConstants.getInventoryType(itemid).ordinal()].countNotOwnedById(itemid);
        if (checkEquipped) {
            count += inventory[InventoryType.EQUIPPED.ordinal()].countNotOwnedById(itemid);
        }
        return count;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public int getJobRank() {
        return jobRank;
    }

    public int getJobRankMove() {
        return jobRankMove;
    }

    public int getJobType() {
        return job.getId() / 1000;
    }

    public Map<Integer, KeyBinding> getKeymap() {
        return keymap;
    }

    public long getLastHealed() {
        return lastHealed;
    }

    public void setLastHealed(long time) {
        this.lastHealed = time;
    }

    public long getLastUsedCashItem() {
        return lastUsedCashItem;
    }

    public void setLastUsedCashItem(long time) {
        this.lastUsedCashItem = time;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFh() {
        Point pos = this.getPosition();
        pos.y -= 6;

        if (map.getFootholds().findBelow(pos) == null) {
            return 0;
        } else {
            return map.getFootholds().findBelow(pos).getY1();
        }
    }

    public int getMapId() {
        if (map != null) {
            return map.getId();
        }
        return mapid;
    }

    public void setMapId(int mapid) {
        this.mapid = mapid;
    }

    public int getMasterLevel(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return 0;
        }
        return ret.masterlevel;
    }

    public int getMasterLevel(Skill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).masterlevel;
    }

    public int getTotalStr() {
        return localstr;
    }

    public int getTotalDex() {
        return localdex;
    }

    public int getTotalInt() {
        return localint_;
    }

    public int getTotalLuk() {
        return localluk;
    }

    public int getTotalMagic() {
        return localmagic;
    }

    public int getTotalWatk() {
        return localwatk;
    }

    public int getMaxClassLevel() {
        return isCygnus() ? 120 : 200;
    }

    public int getMaxLevel() {
        if (!YamlConfig.config.server.USE_ENFORCE_JOB_LEVEL_RANGE || isGmJob()) {
            return getMaxClassLevel();
        }

        return GameConstants.getJobMaxLevel(job);
    }

    public int getMeso() {
        return meso.get();
    }

    public int getMesosTraded() {
        return mesosTraded;
    }

    public int getMessengerPosition() {
        return messengerposition;
    }

    public void setMessengerPosition(int position) {
        this.messengerposition = position;
    }

    public PartyCharacter getMPC() {
        if (mpc == null) {
            mpc = new PartyCharacter(this);
        }
        return mpc;
    }

    public void setMPC(PartyCharacter mpc) {
        this.mpc = mpc;
    }

    public int getTargetHpBarHash() {
        return this.targetHpBarHash;
    }

    public void setTargetHpBarHash(int mobHash) {
        this.targetHpBarHash = mobHash;
    }

    public long getTargetHpBarTime() {
        return this.targetHpBarTime;
    }

    public void setTargetHpBarTime(long timeNow) {
        this.targetHpBarTime = timeNow;
    }

    public void setPlayerAggro(int mobHash) {
        setTargetHpBarHash(mobHash);
        setTargetHpBarTime(System.currentTimeMillis());
    }

    public void resetPlayerAggro() {
        if (getWorldServer().unregisterDisabledServerMessage(id)) {
            client.announceServerMessage();
        }

        setTargetHpBarHash(0);
        setTargetHpBarTime(0);
    }

    public MonsterBook getMonsterBook() {
        return monsterbook;
    }

    public int getMonsterBookCover() {
        return bookCover;
    }

    public void setMonsterBookCover(int bookCover) {
        this.bookCover = bookCover;
    }

    public Mount getMount() {
        return maplemount;
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public void setMessenger(Messenger messenger) {
        this.messenger = messenger;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNoPets() {
        petLock.lock();
        try {
            int ret = 0;
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    ret++;
                }
            }
            return ret;
        } finally {
            petLock.unlock();
        }
    }

    public Party getParty() {
        prtLock.lock();
        try {
            return party;
        } finally {
            prtLock.unlock();
        }
    }

    public void setParty(Party p) {
        prtLock.lock();
        try {
            if (p == null) {
                this.mpc = null;
                doorSlot = -1;

                party = null;
            } else {
                party = p;
            }
        } finally {
            prtLock.unlock();
        }
    }

    public int getPartyId() {
        prtLock.lock();
        try {
            return (party != null ? party.getId() : -1);
        } finally {
            prtLock.unlock();
        }
    }

    public List<Character> getPartyMembersOnline() {
        List<Character> list = new LinkedList<>();

        prtLock.lock();
        try {
            if (party != null) {
                for (PartyCharacter mpc : party.getMembers()) {
                    Character mc = mpc.getPlayer();
                    if (mc != null) {
                        list.add(mc);
                    }
                }
            }
        } finally {
            prtLock.unlock();
        }

        return list;
    }

    public List<Character> getPartyMembersOnSameMap() {
        List<Character> list = new LinkedList<>();
        int thisMapHash = this.getMap().hashCode();

        prtLock.lock();
        try {
            if (party != null) {
                for (PartyCharacter mpc : party.getMembers()) {
                    Character chr = mpc.getPlayer();
                    if (chr != null) {
                        MapleMap chrMap = chr.getMap();
                        if (chrMap != null && chrMap.hashCode() == thisMapHash && chr.isLoggedinWorld()) {
                            list.add(chr);
                        }
                    }
                }
            }
        } finally {
            prtLock.unlock();
        }

        return list;
    }

    public boolean isPartyMember(Character chr) {
        return isPartyMember(chr.getId());
    }

    public boolean isPartyMember(int cid) {
        prtLock.lock();
        try {
            if (party != null) {
                return party.getMemberById(cid) != null;
            }
        } finally {
            prtLock.unlock();
        }

        return false;
    }

    public void setGMLevel(int level) {
        this.gmLevel = Math.min(level, 6);
        this.gmLevel = Math.max(level, 0);

        whiteChat = gmLevel >= 4;   // thanks ozanrijen for suggesting default white chat
    }

    public void closePartySearchInteractions() {
        this.getWorldServer().getPartySearchCoordinator().unregisterPartyLeader(this);
        if (canRecvPartySearchInvite) {
            this.getWorldServer().getPartySearchCoordinator().detachPlayer(this);
        }
    }

    public void closePlayerInteractions() {
        closeNpcShop();
        closeTrade();
        closePlayerMessenger();

        client.closePlayerScriptInteractions();
        resetPlayerAggro();
    }

    public void closeNpcShop() {
        setShop(null);
    }

    public void closeTrade() {
        Trade.cancelTrade(this, Trade.TradeResult.PARTNER_CANCEL);
    }

    public void closePlayerMessenger() {
        Messenger m = this.getMessenger();
        if (m == null) {
            return;
        }

        World w = getWorldServer();
        MessengerCharacter messengerplayer = new MessengerCharacter(this, this.getMessengerPosition());

        w.leaveMessenger(m.getId(), messengerplayer);
        this.setMessenger(null);
        this.setMessengerPosition(4);
    }

    public Pet[] getPets() {
        petLock.lock();
        try {
            return Arrays.copyOf(pets, pets.length);
        } finally {
            petLock.unlock();
        }
    }

    public Pet getPet(int index) {
        if (index < 0) {
            return null;
        }

        petLock.lock();
        try {
            return pets[index];
        } finally {
            petLock.unlock();
        }
    }

    public byte getPetIndex(int petId) {
        petLock.lock();
        try {
            for (byte i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    if (pets[i].getUniqueId() == petId) {
                        return i;
                    }
                }
            }
            return -1;
        } finally {
            petLock.unlock();
        }
    }

    public byte getPetIndex(Pet pet) {
        petLock.lock();
        try {
            for (byte i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    if (pets[i].getUniqueId() == pet.getUniqueId()) {
                        return i;
                    }
                }
            }
            return -1;
        } finally {
            petLock.unlock();
        }
    }

    public int getPossibleReports() {
        return possibleReports;
    }

    public final byte getQuestStatus(final int quest) {
        synchronized (quests) {
            QuestStatus mqs = quests.get((short) quest);
            if (mqs != null) {
                return (byte) mqs.getStatus().getId();
            } else {
                return 0;
            }
        }
    }

    public QuestStatus getQuest(final int quest) {
        return getQuest(Quest.getInstance(quest));
    }

    public QuestStatus getQuest(Quest quest) {
        synchronized (quests) {
            short questid = quest.getId();
            QuestStatus qs = quests.get(questid);
            if (qs == null) {
                qs = new QuestStatus(quest, QuestStatus.Status.NOT_STARTED);
                quests.put(questid, qs);
            }
            return qs;
        }
    }

    public final QuestStatus getQuestNAdd(final Quest quest) {
        synchronized (quests) {
            if (!quests.containsKey(quest.getId())) {
                final QuestStatus status = new QuestStatus(quest, QuestStatus.Status.NOT_STARTED);
                quests.put(quest.getId(), status);
                return status;
            }
            return quests.get(quest.getId());
        }
    }

    public final QuestStatus getQuestNoAdd(final Quest quest) {
        synchronized (quests) {
            return quests.get(quest.getId());
        }
    }

    public boolean needQuestItem(int questid, int itemid) {
        if (questid <= 0) { //For non quest items :3
            return true;
        }

        int amountNeeded, questStatus = this.getQuestStatus(questid);
        if (questStatus == 0) {
            amountNeeded = Quest.getInstance(questid).getStartItemAmountNeeded(itemid);
            if (amountNeeded == Integer.MIN_VALUE) {
                return false;
            }
        } else if (questStatus != 1) {
            return false;
        } else {
            amountNeeded = Quest.getInstance(questid).getCompleteItemAmountNeeded(itemid);
            if (amountNeeded == Integer.MAX_VALUE) {
                return true;
            }
        }

        return getInventory(ItemConstants.getInventoryType(itemid)).countById(itemid) < amountNeeded;
    }

    public int getRank() {
        return rank;
    }

    public int getRankMove() {
        return rankMove;
    }

    public void clearSavedLocation(SavedLocationType type) {
        savedLocations[type.ordinal()] = null;
    }

    public int peekSavedLocation(String type) {
        SavedLocation sl = savedLocations[SavedLocationType.fromString(type).ordinal()];
        if (sl == null) {
            return -1;
        }
        return sl.getMapId();
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String find) {
        search = find;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public Map<Skill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public int getSkillLevel(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return 0;
        }
        return ret.skillevel;
    }

    public byte getSkillLevel(Skill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).skillevel;
    }

    public long getSkillExpiration(Skill skill) {
        if (skills.get(skill) == null) {
            return -1;
        }
        return skills.get(skill).expiration;
    }

    public SkinColor getSkinColor() {
        return skinColor;
    }

    public int getSlot() {
        return slots;
    }

    public void setSlot(int slotid) {
        slots = slotid;
    }

    public final List<QuestStatus> getStartedQuests() {
        List<QuestStatus> ret = new LinkedList<>();
        for (QuestStatus qs : getQuests()) {
            if (qs.getStatus().equals(QuestStatus.Status.STARTED)) {
                ret.add(qs);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public StatEffect getStatForBuff(BuffStat effect) {
        effLock.lock();
        chrLock.lock();
        try {
            BuffStatValueHolder mbsvh = effects.get(effect);
            if (mbsvh == null) {
                return null;
            }
            return mbsvh.effect;
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public Storage getStorage() {
        return storage;
    }

    public Collection<Summon> getSummonsValues() {
        return summons.values();
    }

    public void clearSummons() {
        summons.clear();
    }

    public Summon getSummonByKey(int id) {
        return summons.get(id);
    }

    public boolean isSummonsEmpty() {
        return summons.isEmpty();
    }

    public boolean containsSummon(Summon summon) {
        return summons.containsValue(summon);
    }

    public Trade getTrade() {
        return trade;
    }

    public void setTrade(Trade trade) {
        this.trade = trade;
    }

    public MapObject[] getVisibleMapObjects() {
        return visibleMapObjects.toArray(new MapObject[visibleMapObjects.size()]);
    }

    public int getWorld() {
        return world;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public World getWorldServer() {
        return Server.getInstance().getWorld(world);
    }

    public void giveCoolDowns(final int skillid, long starttime, long length) {
        long timeNow = Server.getInstance().getCurrentTime();
        int time = (int) ((length + starttime) - timeNow);
        addCooldown(skillid, timeNow, time);
    }

    public int gmLevel() {
        return gmLevel;
    }

    public boolean hasEntered(String script, int mapId) {
        String e = entered.get(mapId);
        return script.equals(e);
    }

    public void hasGivenFame(Character to) {
        lastfametime = System.currentTimeMillis();
        lastmonthfameids.add(Integer.valueOf(to.getId()));
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)")) {
            ps.setInt(1, getId());
            ps.setInt(2, to.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean haveItem(int itemid) {
        return getItemQuantity(itemid, ItemConstants.isEquipment(itemid)) > 0;
    }

    public boolean isBuffFrom(BuffStat stat, Skill skill) {
        effLock.lock();
        chrLock.lock();
        try {
            BuffStatValueHolder mbsvh = effects.get(stat);
            if (mbsvh == null) {
                return false;
            }
            return mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public boolean isGmJob() {
        int jn = job.getJobNiche();
        return jn >= 8 && jn <= 9;
    }

    public boolean isCygnus() {
        return getJobType() == 1;
    }

    public boolean isAran() {
        return job.getId() >= 2000 && job.getId() <= 2112;
    }

    public boolean isBeginnerJob() {
        return (job.getId() == 0 || job.getId() == 1000 || job.getId() == 2000);
    }

    public boolean isGM() {
        return gmLevel > 1;
    }

    public void setGM(int level) {
        this.gmLevel = level;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isMapObjectVisible(MapObject mo) {
        return visibleMapObjects.contains(mo);
    }

    public boolean isPartyLeader() {
        prtLock.lock();
        try {
            Party party = getParty();
            return party != null && party.getLeaderId() == getId();
        } finally {
            prtLock.unlock();
        }
    }

    public boolean isGuildLeader() {    // true on guild master or jr. master
        return guildid > 0 && guildRank < 3;
    }

    public void leaveMap() {
        releaseControlledMonsters();
        visibleMapObjects.clear();
        setChair(-1);
    }

    private int getUsedSp(Job job) {
        int jobId = job.getId();
        int spUsed = 0;

        for (Entry<Skill, SkillEntry> s : this.getSkills().entrySet()) {
            Skill skill = s.getKey();
            if (GameConstants.isInJobTree(skill.getId(), jobId) && !skill.isBeginnerSkill()) {
                spUsed += s.getValue().skillevel;
            }
        }

        return spUsed;
    }

    private int getJobLevelSp(int level, Job job, int jobBranch) {
        return 3 * level + GameConstants.getChangeJobSpUpgrade(jobBranch);
    }

    private int getJobMaxSp(Job job) {
        int jobBranch = GameConstants.getJobBranch(job);
        int jobRange = GameConstants.getJobUpgradeLevelRange(jobBranch);
        return getJobLevelSp(jobRange, job, jobBranch);
    }

    private int getJobRemainingSp(Job job) {
        int skillBook = GameConstants.getSkillBook(job.getId());

        int ret = 0;
        for (int i = 0; i <= skillBook; i++) {
            ret += this.getRemainingSp(i);
        }

        return ret;
    }

    private int getSpGain(int spGain, Job job) {
        int curSp = getUsedSp(job) + getJobRemainingSp(job);
        return getSpGain(spGain, curSp, job);
    }

    private int getSpGain(int spGain, int curSp, Job job) {
        int maxSp = getJobMaxSp(job);

        spGain = Math.min(spGain, maxSp - curSp);
        int jobBranch = GameConstants.getJobBranch(job);
        return spGain;
    }

    private void levelUpGainSp() {
        if (GameConstants.getJobBranch(job) == 0) {
            return;
        }

        int spGain = 3;
        if (YamlConfig.config.server.USE_ENFORCE_JOB_SP_RANGE) {
            spGain = getSpGain(spGain, job);
        }

        if (spGain > 0) {
            gainSp(spGain, GameConstants.getSkillBook(job.getId()), true);
        }
    }

    public synchronized void levelUp(boolean takeexp) {
        if (YamlConfig.config.server.USE_AUTOASSIGN_STARTERS_AP && level < 11) {
            effLock.lock();
            statWlock.lock();
            try {
                gainAp(5, true);

                int str = 0, dex = 0;
                if (level < 6) {
                    str += 5;
                } else {
                    str += 4;
                    dex += 1;
                }

                assignStrDexIntLuk(str, dex, 0, 0);
            } finally {
                statWlock.unlock();
                effLock.unlock();
            }
        } else {
            int remainingAp = 5;
            gainAp(remainingAp, true);
        }

        int addhp = Randomizer.rand(12, 16);
        int addmp = Randomizer.rand(10, 12);

        addMaxMPMaxHP(addhp, addmp, true);

        if (takeexp) {
            exp.addAndGet(-ExpTable.getExpNeededForLevel(level));
            if (exp.get() < 0) {
                exp.set(0);
            }
        }

        level++;
        if (level >= getMaxClassLevel()) {
            exp.set(0);
            int maxClassLevel = getMaxClassLevel();
            level = maxClassLevel;
        }

        levelUpGainSp();

        effLock.lock();
        statWlock.lock();
        try {
            recalcLocalStats();
            changeHpMp(localmaxhp, localmaxmp, true);

            List<Pair<Stat, Integer>> statup = new ArrayList<>(10);
            statup.add(new Pair<>(Stat.AVAILABLEAP, remainingAp));
            statup.add(new Pair<>(Stat.AVAILABLESP, remainingSp[GameConstants.getSkillBook(job.getId())]));
            statup.add(new Pair<>(Stat.HP, hp));
            statup.add(new Pair<>(Stat.MP, mp));
            statup.add(new Pair<>(Stat.EXP, exp.get()));
            statup.add(new Pair<>(Stat.LEVEL, level));
            statup.add(new Pair<>(Stat.MAXHP, clientmaxhp));
            statup.add(new Pair<>(Stat.MAXMP, clientmaxmp));
            statup.add(new Pair<>(Stat.STR, str));
            statup.add(new Pair<>(Stat.DEX, dex));

            sendPacket(ChannelPacketCreator.getInstance().updatePlayerStats(statup, true, this));
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }

        getMap().broadcastMessage(this, ChannelPacketCreator.getInstance().showForeignEffect(getId(), 0), false);
        setMPC(new PartyCharacter(this));
        silentPartyUpdate();
    }

    public void setPlayerRates() {
        this.expRate *= GameConstants.getPlayerBonusExpRate(this.level / 20);
        this.mesoRate *= GameConstants.getPlayerBonusMesoRate(this.level / 20);
        this.dropRate *= GameConstants.getPlayerBonusDropRate(this.level / 20);
    }

    public void setWorldRates() {
        World worldz = getWorldServer();
        this.expRate *= worldz.getExpRate();
        this.mesoRate *= worldz.getMesoRate();
        this.dropRate *= worldz.getDropRate();
    }

    public void resetPlayerRates() {
        expRate = 1;
        mesoRate = 1;
        dropRate = 1;

        expCoupon = 1;
        mesoCoupon = 1;
        dropCoupon = 1;
    }

    private void loadCharSkillPoints(String[] skillPoints) {
        int[] sps = new int[skillPoints.length];
        for (int i = 0; i < skillPoints.length; i++) {
            sps[i] = Integer.parseInt(skillPoints[i]);
        }

        setRemainingSp(sps);
    }

    public int getRemainingSp() {
        return getRemainingSp(job.getId()); //default
    }

    public void reloadQuestExpirations() {
        for (QuestStatus mqs : getStartedQuests()) {
            if (mqs.getExpirationTime() > 0) {
                questTimeLimit2(mqs.getQuest(), mqs.getExpirationTime());
            }
        }
    }

    public void message(String m) {
        dropMessage(5, m);
    }

    public void yellowMessage(String m) {
        sendPacket(ChannelPacketCreator.getInstance().sendYellowTip(m));
    }

    public void raiseQuestMobCount(int id) {
        // It seems nexon uses monsters that don't exist in the WZ (except string) to merge multiple mobs together for these 3 monsters.
        // We also want to run mobKilled for both since there are some quest that don't use the updated ID...
        if (id == MobId.GREEN_MUSHROOM || id == MobId.DEJECTED_GREEN_MUSHROOM) {
            raiseQuestMobCount(MobId.GREEN_MUSHROOM_QUEST);
        } else if (id == MobId.ZOMBIE_MUSHROOM || id == MobId.ANNOYED_ZOMBIE_MUSHROOM) {
            raiseQuestMobCount(MobId.ZOMBIE_MUSHROOM_QUEST);
        } else if (id == MobId.GHOST_STUMP || id == MobId.SMIRKING_GHOST_STUMP) {
            raiseQuestMobCount(MobId.GHOST_STUMP_QUEST);
        }

        int lastQuestProcessed = 0;
        try {
            synchronized (quests) {
                for (QuestStatus qs : getQuests()) {
                    lastQuestProcessed = qs.getQuest().getId();
                    if (qs.getStatus() == QuestStatus.Status.COMPLETED || qs.getQuest().canComplete(this, null)) {
                        continue;
                    }

                    if (qs.progress(id)) {
                        announceUpdateQuest(DelayedQuestUpdate.UPDATE, qs, false);
                        if (qs.getInfoNumber() > 0) {
                            announceUpdateQuest(DelayedQuestUpdate.UPDATE, qs, true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Character.mobKilled. chrId {}, last quest processed: {}", this.id, lastQuestProcessed, e);
        }
    }

    public Mount mount(int id, int skillid) {
        Mount mount = maplemount;
        mount.setItemId(id);
        mount.setSkillId(skillid);
        return mount;
    }

    private void playerDead() {
        cancelAllBuffs(false);
        dispelDebuffs();
        lastDeathtime = Server.getInstance().getCurrentTime();

        int[] charmID = {ItemId.SAFETY_CHARM, ItemId.EASTER_BASKET, ItemId.EASTER_CHARM};
        int possesed = 0;
        int i;
        for (i = 0; i < charmID.length; i++) {
            int quantity = getItemQuantity(charmID[i], false);
            if (possesed == 0 && quantity > 0) {
                possesed = quantity;
                break;
            }
        }

        if (getBuffedValue(BuffStat.MORPH) != null) {
            cancelEffectFromBuffStat(BuffStat.MORPH);
        }

        if (getBuffedValue(BuffStat.MONSTER_RIDING) != null) {
            cancelEffectFromBuffStat(BuffStat.MONSTER_RIDING);
        }

        unsitChairInternal();
        sendPacket(ChannelPacketCreator.getInstance().enableActions());
    }

    private void unsitChairInternal() {
        int chairid = chair.get();
        if (chairid >= 0) {
            setChair(-1);
            if (unregisterChairBuff()) {
                getMap().broadcastMessage(this, ChannelPacketCreator.getInstance().cancelForeignChairSkillEffect(this.getId()), false);
            }

            getMap().broadcastMessage(this, ChannelPacketCreator.getInstance().showChair(this.getId(), 0), false);
        }

        sendPacket(ChannelPacketCreator.getInstance().cancelChair(-1));
    }

    public void sitChair(int itemId) {
        if (this.isLoggedinWorld()) {
            if (itemId >= 1000000) {    // sit on item chair
                if (chair.get() < 0) {
                    setChair(itemId);
                    getMap().broadcastMessage(this, ChannelPacketCreator.getInstance().showChair(this.getId(), itemId), false);
                }
                sendPacket(ChannelPacketCreator.getInstance().enableActions());
            } else if (itemId >= 0) {    // sit on map chair
                if (chair.get() < 0) {
                    setChair(itemId);
                    if (registerChairBuff()) {
                        getMap().broadcastMessage(this, ChannelPacketCreator.getInstance().giveForeignChairSkillEffect(this.getId()), false);
                    }
                    sendPacket(ChannelPacketCreator.getInstance().cancelChair(itemId));
                }
            } else {    // stand up
                unsitChairInternal();
            }
        }
    }

    public void respawn(int returnMap) {
        changeMap(returnMap);

        cancelAllBuffs(false);  // thanks Oblivium91 for finding out players still could revive in area and take damage before returning to town

        if (usedSafetyCharm) {  // thanks kvmba for noticing safety charm not providing 30% HP/MP
            addMPHP((int) Math.ceil(this.getClientMaxHp() * 0.3), (int) Math.ceil(this.getClientMaxMp() * 0.3));
        } else {
            updateHp(50);
        }

        setStance(0);
    }

    private void recalcEquipStats() {
        if (equipchanged) {
            equipmaxhp = 0;
            equipmaxmp = 0;
            equipdex = 0;
            equipint_ = 0;
            equipstr = 0;
            equipluk = 0;
            equipmagic = 0;
            equipwatk = 0;
            //equipspeed = 0;
            //equipjump = 0;

            for (Item item : getInventory(InventoryType.EQUIPPED)) {
                Equip equip = (Equip) item;
                equipmaxhp += equip.getHp();
                equipmaxmp += equip.getMp();
                equipdex += equip.getDex();
                equipint_ += equip.getInt();
                equipstr += equip.getStr();
                equipluk += equip.getLuk();
                equipmagic += equip.getMatk() + equip.getInt();
                equipwatk += equip.getWatk();
                //equipspeed += equip.getSpeed();
                //equipjump += equip.getJump();
            }

            equipchanged = false;
        }

        localmaxhp += equipmaxhp;
        localmaxmp += equipmaxmp;
        localdex += equipdex;
        localint_ += equipint_;
        localstr += equipstr;
        localluk += equipluk;
        localmagic += equipmagic;
        localwatk += equipwatk;
    }

    private void reapplyLocalStats() {
        effLock.lock();
        chrLock.lock();
        statWlock.lock();
        try {
            localmaxhp = getMaxHp();
            localmaxmp = getMaxMp();
            localdex = getDex();
            localint_ = getInt();
            localstr = getStr();
            localluk = getLuk();
            localmagic = localint_;
            localwatk = 0;
            localchairrate = -1;

            recalcEquipStats();

            localmagic = Math.min(localmagic, 2000);

            Integer hbhp = getBuffedValue(BuffStat.HYPERBODYHP);
            if (hbhp != null) {
                localmaxhp += (hbhp.doubleValue() / 100) * localmaxhp;
            }
            Integer hbmp = getBuffedValue(BuffStat.HYPERBODYMP);
            if (hbmp != null) {
                localmaxmp += (hbmp.doubleValue() / 100) * localmaxmp;
            }

            localmaxhp = Math.min(30000, localmaxhp);
            localmaxmp = Math.min(30000, localmaxmp);

            StatEffect combo = getBuffEffect(BuffStat.ARAN_COMBO);
            if (combo != null) {
                localwatk += combo.getX();
            }

            Integer mwarr = getBuffedValue(BuffStat.MAPLE_WARRIOR);
            if (mwarr != null) {
                localstr += getStr() * mwarr / 100;
                localdex += getDex() * mwarr / 100;
                localint_ += getInt() * mwarr / 100;
                localluk += getLuk() * mwarr / 100;
            }

            Integer watkbuff = getBuffedValue(BuffStat.WATK);
            if (watkbuff != null) {
                localwatk += watkbuff.intValue();
            }
            Integer matkbuff = getBuffedValue(BuffStat.MATK);
            if (matkbuff != null) {
                localmagic += matkbuff.intValue();
            }

            Integer blessing = getSkillLevel(10000000 * getJobType() + 12);
            if (blessing > 0) {
                localwatk += blessing;
                localmagic += blessing * 2;
            }
        } finally {
            statWlock.unlock();
            chrLock.unlock();
            effLock.unlock();
        }
    }

    private List<Pair<Stat, Integer>> recalcLocalStats() {
        effLock.lock();
        chrLock.lock();
        statWlock.lock();
        try {
            List<Pair<Stat, Integer>> hpmpupdate = new ArrayList<>(2);
            int oldlocalmaxhp = localmaxhp;
            int oldlocalmaxmp = localmaxmp;

            reapplyLocalStats();

            if (YamlConfig.config.server.USE_FIXED_RATIO_HPMP_UPDATE) {
                if (localmaxhp != oldlocalmaxhp) {
                    Pair<Stat, Integer> hpUpdate;

                    if (transienthp == Float.NEGATIVE_INFINITY) {
                        hpUpdate = calcHpRatioUpdate(localmaxhp, oldlocalmaxhp);
                    } else {
                        hpUpdate = calcHpRatioTransient();
                    }

                    hpmpupdate.add(hpUpdate);
                }

                if (localmaxmp != oldlocalmaxmp) {
                    Pair<Stat, Integer> mpUpdate;

                    if (transientmp == Float.NEGATIVE_INFINITY) {
                        mpUpdate = calcMpRatioUpdate(localmaxmp, oldlocalmaxmp);
                    } else {
                        mpUpdate = calcMpRatioTransient();
                    }

                    hpmpupdate.add(mpUpdate);
                }
            }

            return hpmpupdate;
        } finally {
            statWlock.unlock();
            chrLock.unlock();
            effLock.unlock();
        }
    }

    private void updateLocalStats() {
        prtLock.lock();
        effLock.lock();
        statWlock.lock();
        try {
            int oldmaxhp = localmaxhp;
            List<Pair<Stat, Integer>> hpmpupdate = recalcLocalStats();
            enforceMaxHpMp();

            if (!hpmpupdate.isEmpty()) {
                sendPacket(ChannelPacketCreator.getInstance().updatePlayerStats(hpmpupdate, true, this));
            }

            if (oldmaxhp != localmaxhp) {   // thanks Wh1SK3Y (Suwaidy) for pointing out a deadlock occuring related to party members HP
                updatePartyMemberHP();
            }
        } finally {
            statWlock.unlock();
            effLock.unlock();
            prtLock.unlock();
        }
    }

    public void receivePartyMemberHP() {
        prtLock.lock();
        try {
            if (party != null) {
                for (Character partychar : this.getPartyMembersOnSameMap()) {
                    sendPacket(ChannelPacketCreator.getInstance().updatePartyMemberHP(partychar.getId(), partychar.getHp(), partychar.getCurrentMaxHp()));
                }
            }
        } finally {
            prtLock.unlock();
        }
    }

    public void removeCooldown(int skillId) {
        effLock.lock();
        chrLock.lock();
        try {
            this.coolDowns.remove(skillId);
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public void removePet(Pet pet, boolean shift_left) {
        petLock.lock();
        try {
            int slot = -1;
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    if (pets[i].getUniqueId() == pet.getUniqueId()) {
                        pets[i] = null;
                        slot = i;
                        break;
                    }
                }
            }
            if (shift_left) {
                if (slot > -1) {
                    for (int i = slot; i < 3; i++) {
                        if (i != 2) {
                            pets[i] = pets[i + 1];
                        } else {
                            pets[i] = null;
                        }
                    }
                }
            }
        } finally {
            petLock.unlock();
        }
    }

    public void removeVisibleMapObject(MapObject mo) {
        visibleMapObjects.remove(mo);
    }

    public synchronized void saveCooldowns() {
        List<PlayerCoolDownValueHolder> listcd = getAllCooldowns();

        if (!listcd.isEmpty()) {
            try (Connection con = DatabaseConnection.getStaticConnection()) {
                deleteWhereCharacterId(con, "DELETE FROM cooldowns WHERE charid = ?");
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, getId());
                    for (PlayerCoolDownValueHolder cooling : listcd) {
                        ps.setInt(2, cooling.skillId);
                        ps.setLong(3, cooling.startTime);
                        ps.setLong(4, cooling.length);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        Map<Disease, Pair<Long, MobSkill>> listds = getAllDiseases();
        if (!listds.isEmpty()) {
            try (Connection con = DatabaseConnection.getStaticConnection()) {
                deleteWhereCharacterId(con, "DELETE FROM playerdiseases WHERE charid = ?");
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO playerdiseases (charid, disease, mobskillid, mobskilllv, length) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setInt(1, getId());

                    for (Entry<Disease, Pair<Long, MobSkill>> e : listds.entrySet()) {
                        ps.setInt(2, e.getKey().ordinal());

                        MobSkill ms = e.getValue().getRight();
                        MobSkillId msId = ms.getId();
                        ps.setInt(3, msId.type().getId());
                        ps.setInt(4, msId.level());
                        ps.setInt(5, e.getValue().getLeft().intValue());
                        ps.addBatch();
                    }

                    ps.executeBatch();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public void saveLocation(String type) {
        Portal closest = map.findClosestPortal(getPosition());
        savedLocations[SavedLocationType.fromString(type).ordinal()] = new SavedLocation(getMapId(), closest != null ? closest.getId() : 0);
    }

    public void saveCharToDB() {
        if (YamlConfig.config.server.USE_AUTOSAVE) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    saveCharToDB(true);
                }
            };

            CharacterSaveService service = (CharacterSaveService) getWorldServer().getServiceAccess(WorldServices.SAVE_CHARACTER);
            service.registerSaveCharacter(this.getId(), r);
        } else {
            saveCharToDB(true);
        }
    }

    public synchronized void saveCharToDB(boolean notAutosave) {
        if (!loggedIn) {
            return;
        }

        Calendar c = Calendar.getInstance();
        log.debug("Attempting to {} chr {}", notAutosave ? "save" : "autosave", name);

        // TODO: come up with better character management
//        Server.getInstance().updateCharacterEntry(this);

        try (Connection con = DatabaseConnection.getStaticConnection()) {
            con.setAutoCommit(false);
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

            try {
                try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, int = ?, exp = ?, gachaexp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpMpUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, mountlevel = ?, mountexp = ?, mounttiredness= ?, equipslots = ?, useslots = ?, setupslots = ?, etcslots = ?,  monsterbookcover = ?, vanquisherStage = ?, dojoPoints = ?, lastDojoStage = ?, finishedDojoTutorial = ?, vanquisherKills = ?, matchcardwins = ?, matchcardlosses = ?, matchcardties = ?, omokwins = ?, omoklosses = ?, omokties = ?, dataString = ?, fquest = ?, jailexpire = ?, partnerId = ?, marriageItemId = ?, lastExpGainTime = ?, ariantPoints = ?, partySearch = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, level);    // thanks CanIGetaPR for noticing an unnecessary "level" limitation when persisting DB data
                    ps.setInt(2, fame);

                    effLock.lock();
                    statWlock.lock();
                    try {
                        ps.setInt(3, str);
                        ps.setInt(4, dex);
                        ps.setInt(5, luk);
                        ps.setInt(6, int_);
                        ps.setInt(7, Math.abs(exp.get()));
                        ps.setInt(8, Math.abs(gachaexp.get()));
                        ps.setInt(9, hp);
                        ps.setInt(10, mp);
                        ps.setInt(11, maxhp);
                        ps.setInt(12, maxmp);

                        StringBuilder sps = new StringBuilder();
                        for (int j : remainingSp) {
                            sps.append(j);
                            sps.append(",");
                        }
                        String sp = sps.toString();
                        ps.setString(13, sp.substring(0, sp.length() - 1));

                        ps.setInt(14, remainingAp);
                    } finally {
                        statWlock.unlock();
                        effLock.unlock();
                    }

                    ps.setInt(15, gmLevel);
                    ps.setInt(16, skinColor.getId());
                    ps.setInt(17, gender);
                    ps.setInt(18, job.getId());
                    ps.setInt(19, hair);
                    ps.setInt(20, face);
                    if (map == null || (cashshop != null && cashshop.isOpened())) {
                        ps.setInt(21, mapid);
                    } else {
                        if (map.getForcedReturnId() != MapId.NONE) {
                            ps.setInt(21, map.getForcedReturnId());
                        } else {
                            ps.setInt(21, getHp() < 1 ? map.getReturnMapId() : map.getId());
                        }
                    }
                    ps.setInt(22, meso.get());
                    ps.setInt(23, hpMpApUsed);
                    if (map == null || map.getId() == MapId.CRIMSONWOOD_VALLEY_1 || map.getId() == MapId.CRIMSONWOOD_VALLEY_2) {  // reset to first spawnpoint on those maps
                        ps.setInt(24, 0);
                    } else {
                        Portal closest = map.findClosestPlayerSpawnpoint(getPosition());
                        if (closest != null) {
                            ps.setInt(24, closest.getId());
                        } else {
                            ps.setInt(24, 0);
                        }
                    }

                    prtLock.lock();
                    try {
                        if (party != null) {
                            ps.setInt(25, party.getId());
                        } else {
                            ps.setInt(25, -1);
                        }
                    } finally {
                        prtLock.unlock();
                    }

                    ps.setInt(26, buddylist.getCapacity());
                    if (messenger != null) {
                        ps.setInt(27, messenger.getId());
                        ps.setInt(28, messengerposition);
                    } else {
                        ps.setInt(27, 0);
                        ps.setInt(28, 4);
                    }
                    if (maplemount != null) {
                        ps.setInt(29, maplemount.getLevel());
                        ps.setInt(30, maplemount.getExp());
                        ps.setInt(31, maplemount.getTiredness());
                    } else {
                        ps.setInt(29, 1);
                        ps.setInt(30, 0);
                        ps.setInt(31, 0);
                    }
                    for (int i = 1; i < 5; i++) {
                        ps.setInt(i + 31, getSlots(i));
                    }

                    monsterbook.saveCards(con, id);

                    ps.setInt(36, bookCover);
                    ps.setInt(37, 0);
                    ps.setInt(38, 0);
                    ps.setInt(39, 0);
                    ps.setInt(40, 0);
                    ps.setInt(41, 0);
                    ps.setInt(42, 0);
                    ps.setInt(43, 0);
                    ps.setInt(44, 0);
                    ps.setInt(45, 0);
                    ps.setInt(46, 0);
                    ps.setInt(47, 0);
                    ps.setString(48, dataString);
                    ps.setInt(49, quest_fame);
                    ps.setLong(50, jailExpiration);
                    ps.setInt(51, -1);
                    ps.setInt(52, -1);
                    ps.setTimestamp(53, new Timestamp(lastExpGainTime));
                    ps.setInt(54, 0);
                    ps.setInt(55, canRecvPartySearchInvite ? 1 : 0);
                    ps.setInt(56, id);

                    int updateRows = ps.executeUpdate();
                    if (updateRows < 1) {
                        throw new RuntimeException("Character not in database (" + id + ")");
                    }
                }

                List<Pet> petList = new LinkedList<>();
                petLock.lock();
                try {
                    for (int i = 0; i < 3; i++) {
                        if (pets[i] != null) {
                            petList.add(pets[i]);
                        }
                    }
                } finally {
                    petLock.unlock();
                }

                for (Pet pet : petList) {
                    pet.saveToDb();
                }

                for (Entry<Integer, Set<Integer>> es : getExcluded().entrySet()) {    // this set is already protected
                    try (PreparedStatement psIgnore = con.prepareStatement("DELETE FROM petignores WHERE petid=?")) {
                        psIgnore.setInt(1, es.getKey());
                        psIgnore.executeUpdate();
                    }

                    try (PreparedStatement psIgnore = con.prepareStatement("INSERT INTO petignores (petid, itemid) VALUES (?, ?)")) {
                        psIgnore.setInt(1, es.getKey());
                        for (Integer x : es.getValue()) {
                            psIgnore.setInt(2, x);
                            psIgnore.addBatch();
                        }
                        psIgnore.executeBatch();
                    }
                }

                // Key config
                deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?");
                try (PreparedStatement psKey = con.prepareStatement("INSERT INTO keymap (characterid, key, type, action) VALUES (?, ?, ?, ?)")) {
                    psKey.setInt(1, id);

                    Set<Entry<Integer, KeyBinding>> keybindingItems = Collections.unmodifiableSet(keymap.entrySet());
                    for (Entry<Integer, KeyBinding> keybinding : keybindingItems) {
                        psKey.setInt(2, keybinding.getKey());
                        psKey.setInt(3, keybinding.getValue().getType());
                        psKey.setInt(4, keybinding.getValue().getAction());
                        psKey.addBatch();
                    }
                    psKey.executeBatch();
                }

                // No quickslots, or no change.
                boolean bQuickslotEquals = this.m_pQuickslotKeyMapped == null || (this.m_aQuickslotLoaded != null && Arrays.equals(this.m_pQuickslotKeyMapped.GetKeybindings(), this.m_aQuickslotLoaded));
                if (!bQuickslotEquals) {
                    long nQuickslotKeymapped = LongTool.BytesToLong(this.m_pQuickslotKeyMapped.GetKeybindings());

                    try (final PreparedStatement psQuick = con.prepareStatement("INSERT INTO quickslotkeymapped (accountid, keymap) VALUES (?, ?) ON CONFLICT (accountid) DO UPDATE SET keymap = ?;")) {
                        psQuick.setInt(1, this.getAccountID());
                        psQuick.setLong(2, nQuickslotKeymapped);
                        psQuick.setLong(3, nQuickslotKeymapped);
                        psQuick.executeUpdate();
                    }
                }

                // Skill macros
                deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");
                try (PreparedStatement psMacro = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    psMacro.setInt(1, getId());
                    for (int i = 0; i < 5; i++) {
                        SkillMacro macro = skillMacros[i];
                        if (macro != null) {
                            psMacro.setInt(2, macro.getSkill1());
                            psMacro.setInt(3, macro.getSkill2());
                            psMacro.setInt(4, macro.getSkill3());
                            psMacro.setString(5, macro.getName());
                            psMacro.setInt(6, macro.getShout());
                            psMacro.setInt(7, i);
                            psMacro.addBatch();
                        }
                    }
                    psMacro.executeBatch();
                }

                List<Pair<Item, InventoryType>> itemsWithType = new ArrayList<>();
                for (Inventory iv : inventory) {
                    for (Item item : iv.list()) {
                        itemsWithType.add(new Pair<>(item, iv.getType()));
                    }
                }

                // Items
                ItemFactory.INVENTORY.saveItems(itemsWithType, id, con);

                final var query = """
                        INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration)
                        VALUES (?, ?, ?, ?, ?)
                        ON CONFLICT (characterid, skillid) DO UPDATE SET
                            skilllevel = EXCLUDED.skilllevel,
                            masterlevel = EXCLUDED.masterlevel,
                            expiration = EXCLUDED.expiration
                        """;

                // Skills
                try (PreparedStatement psSkill = con.prepareStatement(query)) {
                    psSkill.setInt(1, id);
                    for (Entry<Skill, SkillEntry> skill : skills.entrySet()) {
                        psSkill.setInt(2, skill.getKey().getId());
                        psSkill.setInt(3, skill.getValue().skillevel);
                        psSkill.setInt(4, skill.getValue().masterlevel);
                        psSkill.setLong(5, skill.getValue().expiration);
                        psSkill.addBatch();
                    }
                    psSkill.executeBatch();
                }

                // Saved locations
                deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
                try (PreparedStatement psLoc = con.prepareStatement("INSERT INTO savedlocations (characterid, locationtype, map, portal) VALUES (?, ?, ?, ?)")) {
                    psLoc.setInt(1, id);
                    for (SavedLocationType savedLocationType : SavedLocationType.values()) {
                        if (savedLocations[savedLocationType.ordinal()] != null) {
                            psLoc.setString(2, savedLocationType.name());
                            psLoc.setInt(3, savedLocations[savedLocationType.ordinal()].getMapId());
                            psLoc.setInt(4, savedLocations[savedLocationType.ordinal()].getPortal());
                            psLoc.addBatch();
                        }
                    }
                    psLoc.executeBatch();
                }

                deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?");

                // Vip teleport rocks
                try (PreparedStatement psVip = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 0)")) {
                    for (int i = 0; i < getTrockSize(); i++) {
                        if (trockmaps.get(i) != MapId.NONE) {
                            psVip.setInt(1, getId());
                            psVip.setInt(2, trockmaps.get(i));
                            psVip.addBatch();
                        }
                    }
                    psVip.executeBatch();
                }

                // Regular teleport rocks
                try (PreparedStatement psReg = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 1)")) {
                    for (int i = 0; i < getVipTrockSize(); i++) {
                        if (viptrockmaps.get(i) != MapId.NONE) {
                            psReg.setInt(1, getId());
                            psReg.setInt(2, viptrockmaps.get(i));
                            psReg.addBatch();
                        }
                    }
                    psReg.executeBatch();
                }

                // Buddy
                deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ? AND pending = 0");
                try (PreparedStatement psBuddy = con.prepareStatement("INSERT INTO buddies (characterid, buddyid, pending, group) VALUES (?, ?, 0, ?)")) {
                    psBuddy.setInt(1, id);

                    for (BuddylistEntry entry : buddylist.getBuddies()) {
                        if (entry.isVisible()) {
                            psBuddy.setInt(2, entry.getCharacterId());
                            psBuddy.setString(3, entry.getGroup());
                            psBuddy.addBatch();
                        }
                    }
                    psBuddy.executeBatch();
                }

                // Area info
                deleteWhereCharacterId(con, "DELETE FROM area_info WHERE charid = ?");
                try (PreparedStatement psArea = con.prepareStatement("INSERT INTO area_info (id, charid, area, info) VALUES (DEFAULT, ?, ?, ?)")) {
                    psArea.setInt(1, id);

                    for (Entry<Short, String> area : area_info.entrySet()) {
                        psArea.setInt(2, area.getKey());
                        psArea.setString(3, area.getValue());
                        psArea.addBatch();
                    }
                    psArea.executeBatch();
                }

                // Event stats
                deleteWhereCharacterId(con, "DELETE FROM eventstats WHERE characterid = ?");

                deleteQuestProgressWhereCharacterId(con, id);

                // Quests and medals
                try (PreparedStatement psStatus = con.prepareStatement("INSERT INTO queststatus (queststatusid, characterid, quest, status, time, expires, forfeited, completed) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement psProgress = con.prepareStatement("INSERT INTO questprogress VALUES (DEFAULT, ?, ?, ?, ?)");
                     PreparedStatement psMedal = con.prepareStatement("INSERT INTO medalmaps VALUES (DEFAULT, ?, ?, ?)")) {
                    psStatus.setInt(1, id);

                    for (QuestStatus qs : getQuests()) {
                        psStatus.setInt(2, qs.getQuest().getId());
                        psStatus.setInt(3, qs.getStatus().getId());
                        psStatus.setInt(4, (int) (qs.getCompletionTime() / 1000));
                        psStatus.setLong(5, qs.getExpirationTime());
                        psStatus.setInt(6, qs.getForfeited());
                        psStatus.setInt(7, qs.getCompleted());
                        psStatus.executeUpdate();

                        try (ResultSet rs = psStatus.getGeneratedKeys()) {
                            rs.next();
                            for (int mob : qs.getProgress().keySet()) {
                                psProgress.setInt(1, id);
                                psProgress.setInt(2, rs.getInt(1));
                                psProgress.setInt(3, mob);
                                psProgress.setString(4, qs.getProgress(mob));
                                psProgress.addBatch();
                            }
                            psProgress.executeBatch();

                            for (int i = 0; i < qs.getMedalMaps().size(); i++) {
                                psMedal.setInt(1, id);
                                psMedal.setInt(2, rs.getInt(1));
                                psMedal.setInt(3, qs.getMedalMaps().get(i));
                                psMedal.addBatch();
                            }
                            psMedal.executeBatch();
                        }
                    }
                }

                if (cashshop != null) {
                    cashshop.save(con);
                }

                if (storage != null && usedStorage) {
                    storage.saveToDB(con);
                    usedStorage = false;
                }

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            log.error("Error saving chr {}, level: {}, job: {}", name, level, job.getId(), e);
        }
    }

    public void sendPolice(int greason, String reason, int duration) {
        sendPacket(ChannelPacketCreator.getInstance().sendPolice(String.format("You have been blocked by the#b %s Police for %s.#k", "Cosmic", reason)));
        this.isbanned = true;
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                client.disconnect(false, false);
            }
        }, duration);
    }

    public void sendPolice(String text) {
        final String message = getName() + " received this - " + text;
        if (Server.getInstance().isGmOnline(this.getWorld())) { //Alert and log if a GM is online
            Server.getInstance().broadcastGMMessage(this.getWorld(), ChannelPacketCreator.getInstance().sendYellowTip(message));
        } else { //Auto DC and log if no GM is online
            client.disconnect(false, false);
        }
        log.info(message);
    }

    public void sendKeymap() {
        sendPacket(ChannelPacketCreator.getInstance().getKeymap(keymap));
    }

    public void sendQuickmap() {
        // send quickslots to user
        QuickslotBinding pQuickslotKeyMapped = this.m_pQuickslotKeyMapped;

        if (pQuickslotKeyMapped == null) {
            pQuickslotKeyMapped = new QuickslotBinding(QuickslotBinding.DEFAULT_QUICKSLOTS);
        }

        this.sendPacket(ChannelPacketCreator.getInstance().QuickslotMappedInit(pQuickslotKeyMapped));
    }

    public void sendMacros() {
        // Always send the macro packet to fix a client side bug when switching characters.
        sendPacket(ChannelPacketCreator.getInstance().getMacros(skillMacros));
    }

    public SkillMacro[] getMacros() {
        return skillMacros;
    }

    public void setBuddyCapacity(int capacity) {
        buddylist.setCapacity(capacity);
        sendPacket(ChannelPacketCreator.getInstance().updateBuddyCapacity(capacity));
    }

    public void setBuffedValue(BuffStat effect, int value) {
        effLock.lock();
        chrLock.lock();
        try {
            BuffStatValueHolder mbsvh = effects.get(effect);
            if (mbsvh == null) {
                return;
            }
            mbsvh.value = value;
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    private void hpChangeAction(int oldHp) {
        boolean playerDied = false;
        if (hp <= 0) {
            if (oldHp > hp) {
                playerDied = true;
            }
        }

        final boolean chrDied = playerDied;
        Runnable r = () -> {
            updatePartyMemberHP();    // thanks BHB (BHB88) for detecting a deadlock case within player stats.

            if (chrDied) {
                playerDead();
            }
        };
        if (map != null) {
            map.registerCharacterStatUpdate(r);
        }
    }

    private Pair<Stat, Integer> calcHpRatioUpdate(int newHp, int oldHp) {
        int delta = newHp - oldHp;
        this.hp = calcHpRatioUpdate(hp, oldHp, delta);

        hpChangeAction(Short.MIN_VALUE);
        return new Pair<>(Stat.HP, hp);
    }

    private Pair<Stat, Integer> calcMpRatioUpdate(int newMp, int oldMp) {
        int delta = newMp - oldMp;
        this.mp = calcMpRatioUpdate(mp, oldMp, delta);
        return new Pair<>(Stat.MP, mp);
    }

    private Pair<Stat, Integer> calcHpRatioTransient() {
        this.hp = calcTransientRatio(transienthp * localmaxhp);

        hpChangeAction(Short.MIN_VALUE);
        return new Pair<>(Stat.HP, hp);
    }

    private Pair<Stat, Integer> calcMpRatioTransient() {
        this.mp = calcTransientRatio(transientmp * localmaxmp);
        return new Pair<>(Stat.MP, mp);
    }

    private int calcHpRatioUpdate(int curpoint, int maxpoint, int diffpoint) {
        int curMax = maxpoint;
        int nextMax = Math.min(30000, maxpoint + diffpoint);

        float temp = curpoint * nextMax;
        int ret = (int) Math.ceil(temp / curMax);

        transienthp = (maxpoint > nextMax) ? ((float) curpoint) / maxpoint : ((float) ret) / nextMax;
        return ret;
    }

    private int calcMpRatioUpdate(int curpoint, int maxpoint, int diffpoint) {
        int curMax = maxpoint;
        int nextMax = Math.min(30000, maxpoint + diffpoint);

        float temp = curpoint * nextMax;
        int ret = (int) Math.ceil(temp / curMax);

        transientmp = (maxpoint > nextMax) ? ((float) curpoint) / maxpoint : ((float) ret) / nextMax;
        return ret;
    }

    public boolean applyHpMpChange(int hpCon, int hpchange, int mpchange) {
        boolean zombify = hasDisease(Disease.ZOMBIFY);

        effLock.lock();
        statWlock.lock();
        try {
            int nextHp = hp + hpchange, nextMp = mp + mpchange;
            boolean cannotApplyHp = hpchange != 0 && nextHp <= 0 && (!zombify || hpCon > 0);
            boolean cannotApplyMp = mpchange != 0 && nextMp < 0;

            if (cannotApplyHp || cannotApplyMp) {
                if (!isGM()) {
                    return false;
                }

                if (cannotApplyHp) {
                    nextHp = 1;
                }
            }

            updateHpMp(nextHp, nextMp);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }

        // autopot on HPMP deplete... thanks shavit for finding out D. Roar doesn't trigger autopot request
        if (hpchange < 0) {
            KeyBinding autohpPot = this.getKeymap().get(91);
            if (autohpPot != null) {
                int autohpItemid = autohpPot.getAction();
                float autohpAlert = this.getAutopotHpAlert();
                if (((float) this.getHp()) / this.getCurrentMaxHp() <= autohpAlert) { // try within user settings... thanks Lame, Optimist, Stealth2800
                    Item autohpItem = this.getInventory(InventoryType.USE).findById(autohpItemid);
                    if (autohpItem != null) {
                        this.setAutopotHpAlert(0.9f * autohpAlert);
                        PetAutopotProcessor.runAutopotAction(client, autohpItem.getPosition(), autohpItemid);
                    }
                }
            }
        }

        if (mpchange < 0) {
            KeyBinding autompPot = this.getKeymap().get(92);
            if (autompPot != null) {
                int autompItemid = autompPot.getAction();
                float autompAlert = this.getAutopotMpAlert();
                if (((float) this.getMp()) / this.getCurrentMaxMp() <= autompAlert) {
                    Item autompItem = this.getInventory(InventoryType.USE).findById(autompItemid);
                    if (autompItem != null) {
                        this.setAutopotMpAlert(0.9f * autompAlert); // autoMP would stick to using pots at every depletion in some cases... thanks Rohenn
                        PetAutopotProcessor.runAutopotAction(client, autompItem.getPosition(), autompItemid);
                    }
                }
            }
        }

        return true;
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public int getDoorSlot() {
        if (doorSlot != -1) {
            return doorSlot;
        }
        return fetchDoorSlot();
    }

    public int fetchDoorSlot() {
        prtLock.lock();
        try {
            doorSlot = (party == null) ? 0 : party.getPartyDoor(this.getId());
            return doorSlot;
        } finally {
            prtLock.unlock();
        }
    }

    public byte getSlots(int type) {
        return type == InventoryType.CASH.getType() ? 96 : inventory[type].getSlotLimit();
    }

    public boolean canGainSlots(int type, int slots) {
        slots += inventory[type].getSlotLimit();
        return slots <= 96;
    }

    public boolean gainSlots(int type, int slots) {
        return gainSlots(type, slots, true);
    }

    public boolean gainSlots(int type, int slots, boolean update) {
        int newLimit = gainSlotsInternal(type, slots);
        if (newLimit != -1) {
            this.saveCharToDB();
            if (update) {
                sendPacket(ChannelPacketCreator.getInstance().updateInventorySlotLimit(type, newLimit));
            }
            return true;
        } else {
            return false;
        }
    }

    private int gainSlotsInternal(int type, int slots) {
        inventory[type].lockInventory();
        try {
            if (canGainSlots(type, slots)) {
                int newLimit = inventory[type].getSlotLimit() + slots;
                inventory[type].setSlotLimit(newLimit);
                return newLimit;
            } else {
                return -1;
            }
        } finally {
            inventory[type].unlockInventory();
        }
    }

    public void shiftPetsRight() {
        petLock.lock();
        try {
            if (pets[2] == null) {
                pets[2] = pets[1];
                pets[1] = pets[0];
                pets[0] = null;
            }
        } finally {
            petLock.unlock();
        }
    }

    public void showUnderleveledInfo(Monster mob) {
        long curTime = Server.getInstance().getCurrentTime();
        if (nextWarningTime < curTime) {
            nextWarningTime = curTime + MINUTES.toMillis(1);   // show underlevel info again after 1 minute

            showHint("You have gained #rno experience#k from defeating #e#b" + mob.getName() + "#k#n (lv. #b" + mob.getLevel() + "#k)! Take note you must have around the same level as the mob to start earning EXP from it.");
        }
    }

    public void showHint(String msg) {
        showHint(msg, 500);
    }

    public void showHint(String msg, int length) {
        client.announceHint(msg, length);
    }

    public void silentGiveBuffs(List<Pair<Long, PlayerBuffValueHolder>> buffs) {
        for (Pair<Long, PlayerBuffValueHolder> mbsv : buffs) {
            PlayerBuffValueHolder mbsvh = mbsv.getRight();
            mbsvh.effect.silentApplyBuff(this, mbsv.getLeft());
        }
    }

    public void silentPartyUpdate() {
        silentPartyUpdateInternal(getParty());
    }

    private void silentPartyUpdateInternal(Party chrParty) {
        if (chrParty != null) {
            getWorldServer().updateParty(chrParty.getId(), PartyOperation.SILENT_UPDATE, getMPC());
        }
    }

    public boolean skillIsCooling(int skillId) {
        effLock.lock();
        chrLock.lock();
        try {
            return coolDowns.containsKey(Integer.valueOf(skillId));
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public void runFullnessSchedule(int petSlot) {
        Pet pet = getPet(petSlot);
        if (pet == null) {
            return;
        }

        int newFullness = pet.getFullness() - PetDataFactory.getHunger(pet.getItemId());
        if (newFullness <= 5) {
            pet.setFullness(15);
            pet.saveToDb();
            unequipPet(pet, true);
            dropMessage(6, "Your pet grew hungry! Treat it some pet food to keep it healthy!");
        } else {
            pet.setFullness(newFullness);
            pet.saveToDb();
            Item petz = getInventory(InventoryType.CASH).getItem(pet.getPosition());
            if (petz != null) {
                forceUpdateItem(petz);
            }
        }
    }

    public boolean runTirednessSchedule() {
        if (maplemount != null) {
            int tiredness = maplemount.incrementAndGetTiredness();

            this.getMap().broadcastMessage(ChannelPacketCreator.getInstance().updateMount(this.getId(), maplemount, false));
            if (tiredness > 99) {
                maplemount.setTiredness(99);
                this.dispelSkill(this.getJobType() * 10000000 + 1004);
                this.dropMessage(6, "Your mount grew tired! Treat it some revitalizer before riding it again!");
                return false;
            }
        }

        return true;
    }

    public void startMapEffect(String msg, int itemId, int duration) {
        final MapEffect mapEffect = new MapEffect(msg, itemId);
        sendPacket(mapEffect.makeStartData());
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                sendPacket(mapEffect.makeDestroyData());
            }
        }, duration);
    }

    public void unequipPet(Pet pet, boolean shift_left) {
        unequipPet(pet, shift_left, false);
    }

    public void unequipPet(Pet pet, boolean shift_left, boolean hunger) {
        byte petIdx = this.getPetIndex(pet);
        Pet chrPet = this.getPet(petIdx);

        if (chrPet != null) {
            chrPet.setSummoned(false);
            chrPet.saveToDb();
        }

        this.getClient().getWorldServer().unregisterPetHunger(this, petIdx);
        getMap().broadcastMessage(this, ChannelPacketCreator.getInstance().showPet(this, pet, true, hunger), true);

        removePet(pet, shift_left);
        commitExcludedItems();

        sendPacket(ChannelPacketCreator.getInstance().petStatUpdate(this));
        sendPacket(ChannelPacketCreator.getInstance().enableActions());
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
    }

    public void updatePartyMemberHP() {
        prtLock.lock();
        try {
            updatePartyMemberHPInternal();
        } finally {
            prtLock.unlock();
        }
    }

    private void updatePartyMemberHPInternal() {
        if (party != null) {
            int curmaxhp = getCurrentMaxHp();
            int curhp = getHp();
            for (Character partychar : this.getPartyMembersOnSameMap()) {
                partychar.sendPacket(ChannelPacketCreator.getInstance().updatePartyMemberHP(getId(), curhp, curmaxhp));
            }
        }
    }

    public void setQuestProgress(int id, int infoNumber, String progress) {
        Quest q = Quest.getInstance(id);
        QuestStatus qs = getQuest(q);

        if (qs.getInfoNumber() == infoNumber && infoNumber > 0) {
            Quest iq = Quest.getInstance(infoNumber);
            QuestStatus iqs = getQuest(iq);
            iqs.setProgress(0, progress);
        } else {
            qs.setProgress(infoNumber, progress);   // quest progress is thoroughly a string match, infoNumber is actually another questid
        }

        announceUpdateQuest(DelayedQuestUpdate.UPDATE, qs, false);
        if (qs.getInfoNumber() > 0) {
            announceUpdateQuest(DelayedQuestUpdate.UPDATE, qs, true);
        }
    }

    public void awardQuestPoint(int awardedPoints) {
        if (YamlConfig.config.server.QUEST_POINT_REQUIREMENT < 1 || awardedPoints < 1) {
            return;
        }

        int delta;
        synchronized (quests) {
            quest_fame += awardedPoints;

            delta = quest_fame / YamlConfig.config.server.QUEST_POINT_REQUIREMENT;
            quest_fame %= YamlConfig.config.server.QUEST_POINT_REQUIREMENT;
        }

        if (delta > 0) {
            gainFame(delta);
        }
    }

    private void announceUpdateQuestInternal(Character chr, Pair<DelayedQuestUpdate, Object[]> questUpdate) {
        Object[] objs = questUpdate.getRight();

        switch (questUpdate.getLeft()) {
            case UPDATE:
                sendPacket(ChannelPacketCreator.getInstance().updateQuest(chr, (QuestStatus) objs[0], (Boolean) objs[1]));
                break;

            case FORFEIT:
                sendPacket(ChannelPacketCreator.getInstance().forfeitQuest((Short) objs[0]));
                break;

            case COMPLETE:
                sendPacket(ChannelPacketCreator.getInstance().completeQuest((Short) objs[0], (Long) objs[1]));
                break;

            case INFO:
                QuestStatus qs = (QuestStatus) objs[0];
                sendPacket(ChannelPacketCreator.getInstance().updateQuestInfo(qs.getQuest().getId(), qs.getNpc()));
                break;
        }
    }

    public void announceUpdateQuest(DelayedQuestUpdate questUpdateType, Object... params) {
        Pair<DelayedQuestUpdate, Object[]> p = new Pair<>(questUpdateType, params);
        Client c = this.getClient();
        if (c.getQM() != null || c.getCM() != null) {
            synchronized (npcUpdateQuests) {
                npcUpdateQuests.add(p);
            }
        } else {
            announceUpdateQuestInternal(this, p);
        }
    }

    public void flushDelayedUpdateQuests() {
        List<Pair<DelayedQuestUpdate, Object[]>> qmQuestUpdateList;

        synchronized (npcUpdateQuests) {
            qmQuestUpdateList = new ArrayList<>(npcUpdateQuests);
            npcUpdateQuests.clear();
        }

        for (Pair<DelayedQuestUpdate, Object[]> q : qmQuestUpdateList) {
            announceUpdateQuestInternal(this, q);
        }
    }

    public void updateQuestStatus(QuestStatus qs) {
        synchronized (quests) {
            quests.put(qs.getQuestID(), qs);
        }
        if (qs.getStatus().equals(QuestStatus.Status.STARTED)) {
            announceUpdateQuest(DelayedQuestUpdate.UPDATE, qs, false);
            if (qs.getInfoNumber() > 0) {
                announceUpdateQuest(DelayedQuestUpdate.UPDATE, qs, true);
            }
            announceUpdateQuest(DelayedQuestUpdate.INFO, qs);
        } else if (qs.getStatus().equals(QuestStatus.Status.COMPLETED)) {
            Quest mquest = qs.getQuest();
            short questid = mquest.getId();
            if (!mquest.isSameDayRepeatable() && !Quest.isExploitableQuest(questid)) {
                awardQuestPoint(YamlConfig.config.server.QUEST_POINT_PER_QUEST_COMPLETE);
            }
            qs.setCompleted(qs.getCompleted() + 1);   // Jayd's idea - count quest completed

            announceUpdateQuest(DelayedQuestUpdate.COMPLETE, questid, qs.getCompletionTime());
            //announceUpdateQuest(DelayedQuestUpdate.INFO, qs); // happens after giving rewards, for non-next quests only
        } else if (qs.getStatus().equals(QuestStatus.Status.NOT_STARTED)) {
            announceUpdateQuest(DelayedQuestUpdate.UPDATE, qs, false);
            if (qs.getInfoNumber() > 0) {
                announceUpdateQuest(DelayedQuestUpdate.UPDATE, qs, true);
            }
            // reminder: do not reset quest progress of infoNumbers, some quests cannot backtrack
        }
    }

    private void expireQuest(Quest quest) {
        if (quest.forfeit(this)) {
            sendPacket(ChannelPacketCreator.getInstance().questExpire(quest.getId()));
        }
    }

    public void cancelQuestExpirationTask() {
        evtLock.lock();
        try {
            if (questExpireTask != null) {
                questExpireTask.cancel(false);
                questExpireTask = null;
            }
        } finally {
            evtLock.unlock();
        }
    }

    public void forfeitExpirableQuests() {
        evtLock.lock();
        try {
            for (Quest quest : questExpirations.keySet()) {
                quest.forfeit(this);
            }

            questExpirations.clear();
        } finally {
            evtLock.unlock();
        }
    }

    public void questExpirationTask() {
        evtLock.lock();
        try {
            if (!questExpirations.isEmpty()) {
                if (questExpireTask == null) {
                    questExpireTask = TimerManager.getInstance().register(new Runnable() {
                        @Override
                        public void run() {
                            runQuestExpireTask();
                        }
                    }, SECONDS.toMillis(10));
                }
            }
        } finally {
            evtLock.unlock();
        }
    }

    private void runQuestExpireTask() {
        evtLock.lock();
        try {
            long timeNow = Server.getInstance().getCurrentTime();
            List<Quest> expireList = new LinkedList<>();

            for (Entry<Quest, Long> qe : questExpirations.entrySet()) {
                if (qe.getValue() <= timeNow) {
                    expireList.add(qe.getKey());
                }
            }

            if (!expireList.isEmpty()) {
                for (Quest quest : expireList) {
                    expireQuest(quest);
                    questExpirations.remove(quest);
                }

                if (questExpirations.isEmpty()) {
                    questExpireTask.cancel(false);
                    questExpireTask = null;
                }
            }
        } finally {
            evtLock.unlock();
        }
    }

    private void registerQuestExpire(Quest quest, long time) {
        evtLock.lock();
        try {
            if (questExpireTask == null) {
                questExpireTask = TimerManager.getInstance().register(new Runnable() {
                    @Override
                    public void run() {
                        runQuestExpireTask();
                    }
                }, SECONDS.toMillis(10));
            }

            questExpirations.put(quest, Server.getInstance().getCurrentTime() + time);
        } finally {
            evtLock.unlock();
        }
    }

    public void questTimeLimit(final Quest quest, int seconds) {
        registerQuestExpire(quest, SECONDS.toMillis(seconds));
        sendPacket(ChannelPacketCreator.getInstance().addQuestTimeLimit(quest.getId(), (int) SECONDS.toMillis(seconds)));
    }

    public void questTimeLimit2(final Quest quest, long expires) {
        long timeLeft = expires - System.currentTimeMillis();

        if (timeLeft <= 0) {
            expireQuest(quest);
        } else {
            registerQuestExpire(quest, timeLeft);
        }
    }

    public void updateSingleStat(Stat stat, int newval) {
        updateSingleStat(stat, newval, false);
    }

    private void updateSingleStat(Stat stat, int newval, boolean itemReaction) {
        sendPacket(ChannelPacketCreator.getInstance().updatePlayerStats(Collections.singletonList(new Pair<>(stat, Integer.valueOf(newval))), itemReaction, this));
    }

    public void sendPacket(Packet packet) {
        client.sendPacket(packet);
    }

    @Override
    public int getObjectId() {
        return getId();
    }

    @Override
    public void setObjectId(int id) {
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.PLAYER;
    }

    @Override
    public void sendDestroyData(Client client) {
        client.sendPacket(ChannelPacketCreator.getInstance().removePlayerFromMap(this.getObjectId()));
    }

    @Override
    public void sendSpawnData(Client client) {
        if (!this.isHidden() || client.getPlayer().gmLevel() > 1) {
            client.sendPacket(ChannelPacketCreator.getInstance().spawnPlayerMapObject(client, this, false));

            if (buffEffects.containsKey(getJobMapChair())) { // mustn't effLock, chrLock sendSpawnData
                client.sendPacket(ChannelPacketCreator.getInstance().giveForeignChairSkillEffect(id));
            }
        }

        if (this.isHidden()) {
            List<Pair<BuffStat, Integer>> dsstat = Collections.singletonList(new Pair<>(BuffStat.DARKSIGHT, 0));
            getMap().broadcastGMMessage(this, ChannelPacketCreator.getInstance().giveForeignBuff(getId(), dsstat), false);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public int getLinkedLevel() {
        return linkedLevel;
    }

    public String getLinkedName() {
        return linkedName;
    }

    public CashShop getCashShop() {
        return cashshop;
    }

    public void portalDelay(long delay) {
        this.portaldelay = System.currentTimeMillis() + delay;
    }

    public long portalDelay() {
        return portaldelay;
    }

    public void blockPortal(String scriptName) {
        if (!blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.add(scriptName);
            sendPacket(ChannelPacketCreator.getInstance().enableActions());
        }
    }

    public void unblockPortal(String scriptName) {
        if (blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.remove(scriptName);
        }
    }

    public List<String> getBlockedPortals() {
        return blockedPortals;
    }

    public boolean containsAreaInfo(int area, String info) {
        Short area_ = Short.valueOf((short) area);
        if (area_info.containsKey(area_)) {
            return area_info.get(area_).contains(info);
        }
        return false;
    }

    public void updateAreaInfo(int area, String info) {
        area_info.put(Short.valueOf((short) area), info);
        sendPacket(ChannelPacketCreator.getInstance().updateAreaInfo(area, info));
    }

    public String getAreaInfo(int area) {
        return area_info.get(Short.valueOf((short) area));
    }

    public Map<Short, String> getAreaInfos() {
        return area_info;
    }

    public void autoban(String reason) {
        if (this.isGM() || this.isBanned()) {  // thanks RedHat for noticing GM's being able to get banned
            return;
        }

        this.ban(reason);
        sendPacket(ChannelPacketCreator.getInstance().sendPolice(String.format("You have been blocked by the#b %s Police for HACK reason.#k", "Cosmic")));
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                client.disconnect(false, false);
            }
        }, 5000);

        Server.getInstance().broadcastGMMessage(this.getWorld(), ChannelPacketCreator.getInstance().serverNotice(6, Character.makeMapleReadable(this.name) + " was autobanned for " + reason));
    }

    public void block(int reason, int days, String desc) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        final Timestamp TS = new Timestamp(cal.getTimeInMillis());

        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banreason = ?, tempban = ?, greason = ? WHERE id = ?")) {
            ps.setString(1, desc);
            ps.setTimestamp(2, TS);
            ps.setInt(3, reason);
            ps.setInt(4, accountid);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isBanned() {
        return isbanned;
    }

    public List<Integer> getTrockMaps() {
        return trockmaps;
    }

    public List<Integer> getVipTrockMaps() {
        return viptrockmaps;
    }

    public int getTrockSize() {
        int ret = trockmaps.indexOf(MapId.NONE);
        if (ret == -1) {
            ret = 5;
        }

        return ret;
    }

    public void deleteFromTrocks(int map) {
        trockmaps.remove(Integer.valueOf(map));
        while (trockmaps.size() < 10) {
            trockmaps.add(MapId.NONE);
        }
    }

    public void addTrockMap() {
        int index = trockmaps.indexOf(MapId.NONE);
        if (index != -1) {
            trockmaps.set(index, getMapId());
        }
    }

    public boolean isTrockMap(int id) {
        int index = trockmaps.indexOf(id);
        return index != -1;
    }

    public int getVipTrockSize() {
        int ret = viptrockmaps.indexOf(MapId.NONE);

        if (ret == -1) {
            ret = 10;
        }

        return ret;
    }

    public void deleteFromVipTrocks(int map) {
        viptrockmaps.remove(Integer.valueOf(map));
        while (viptrockmaps.size() < 10) {
            viptrockmaps.add(MapId.NONE);
        }
    }

    public void addVipTrockMap() {
        int index = viptrockmaps.indexOf(MapId.NONE);
        if (index != -1) {
            viptrockmaps.set(index, getMapId());
        }
    }

    public boolean isVipTrockMap(int id) {
        int index = viptrockmaps.indexOf(id);
        return index != -1;
    }

    public AutobanManager getAutobanManager() {
        return autoban;
    }

    public void equippedItem(Equip equip) {
        int itemid = equip.getItemId();

        if (itemid == ItemId.PENDANT_OF_THE_SPIRIT) {
            this.equipPendantOfSpirit();
        } else if (itemid == ItemId.MESO_MAGNET) {
            equippedMesoMagnet = true;
        } else if (itemid == ItemId.ITEM_POUCH) {
            equippedItemPouch = true;
        } else if (itemid == ItemId.ITEM_IGNORE) {
            equippedPetItemIgnore = true;
        }
    }

    public void unequippedItem(Equip equip) {
        int itemid = equip.getItemId();

        if (itemid == ItemId.PENDANT_OF_THE_SPIRIT) {
            this.unequipPendantOfSpirit();
        } else if (itemid == ItemId.MESO_MAGNET) {
            equippedMesoMagnet = false;
        } else if (itemid == ItemId.ITEM_POUCH) {
            equippedItemPouch = false;
        } else if (itemid == ItemId.ITEM_IGNORE) {
            equippedPetItemIgnore = false;
        }
    }

    public boolean isEquippedMesoMagnet() {
        return equippedMesoMagnet;
    }

    public boolean isEquippedItemPouch() {
        return equippedItemPouch;
    }

    public boolean isEquippedPetItemIgnore() {
        return equippedPetItemIgnore;
    }

    private void equipPendantOfSpirit() {
        if (pendantOfSpirit == null) {
            pendantOfSpirit = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    if (pendantExp < 3) {
                        pendantExp++;
                        message("Pendant of the Spirit has been equipped for " + pendantExp + " hour(s), you will now receive " + pendantExp + "0% bonus exp.");
                    } else {
                        pendantOfSpirit.cancel(false);
                    }
                }
            }, 3600000); //1 hour
        }
    }

    private void unequipPendantOfSpirit() {
        if (pendantOfSpirit != null) {
            pendantOfSpirit.cancel(false);
            pendantOfSpirit = null;
        }
        pendantExp = 0;
    }

    private Collection<Item> getUpgradeableEquipList() {
        Collection<Item> fullList = getInventory(InventoryType.EQUIPPED).list();
        if (YamlConfig.config.server.USE_EQUIPMNT_LVLUP_CASH) {
            return fullList;
        }

        Collection<Item> eqpList = new LinkedHashSet<>();
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        for (Item it : fullList) {
            if (!ii.isCash(it.getItemId())) {
                eqpList.add(it);
            }
        }

        return eqpList;
    }

    public void increaseEquipExp(int expGain) {
        if (allowExpGain) {     // thanks Vcoc for suggesting equip EXP gain conditionally
            if (expGain < 0) {
                expGain = Integer.MAX_VALUE;
            }

            ItemInformationProvider ii = ItemInformationProvider.getInstance();
            for (Item item : getUpgradeableEquipList()) {
                Equip nEquip = (Equip) item;
                String itemName = ii.getName(nEquip.getItemId());
                if (itemName == null) {
                    continue;
                }

                nEquip.gainItemExp(client, expGain);
            }
        }
    }

    public void showAllEquipFeatures() {
        String showMsg = "";

        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        for (Item item : getInventory(InventoryType.EQUIPPED).list()) {
            Equip nEquip = (Equip) item;
            String itemName = ii.getName(nEquip.getItemId());
            if (itemName == null) {
                continue;
            }

            showMsg += nEquip.showEquipFeatures(client);
        }

        if (!showMsg.isEmpty()) {
            this.showHint("#ePLAYER EQUIPMENTS:#n\r\n\r\n" + showMsg, 400);
        }
    }

    public void logOff() {
        this.loggedIn = false;

        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE characters SET lastLogoutTime=? WHERE id=?")) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long time) {
        this.loginTime = time;
    }

    public long getLoggedInTime() {
        return System.currentTimeMillis() - loginTime;
    }

    public boolean isLoggedin() {
        return loggedIn;
    }

    public boolean getWhiteChat() {
        return isGM() && whiteChat;
    }

    public void toggleWhiteChat() {
        whiteChat = !whiteChat;
    }

    public float getAutopotHpAlert() {
        return autopotHpAlert;
    }

    public void setAutopotHpAlert(float hpPortion) {
        autopotHpAlert = hpPortion;
    }

    public float getAutopotMpAlert() {
        return autopotMpAlert;
    }

    public void setAutopotMpAlert(float mpPortion) {
        autopotMpAlert = mpPortion;
    }

    public long getJailExpirationTimeLeft() {
        return jailExpiration - System.currentTimeMillis();
    }

    private void setFutureJailExpiration(long time) {
        jailExpiration = System.currentTimeMillis() + time;
    }

    public void addJailExpirationTime(long time) {
        long timeLeft = getJailExpirationTimeLeft();

        if (timeLeft <= 0) {
            setFutureJailExpiration(time);
        } else {
            setFutureJailExpiration(timeLeft + time);
        }
    }

    public void removeJailExpirationTime() {
        jailExpiration = 0;
    }

    public boolean registerNameChange(String newName) {
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            //check for pending name change
            long currentTimeMillis = System.currentTimeMillis();
            try (PreparedStatement ps = con.prepareStatement("SELECT completionTime FROM namechanges WHERE characterid=?")) { //double check, just in case
                ps.setInt(1, getId());

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Timestamp completedTimestamp = rs.getTimestamp("completionTime");
                        if (completedTimestamp == null) {
                            return false; //pending
                        } else if (completedTimestamp.getTime() + YamlConfig.config.server.NAME_CHANGE_COOLDOWN > currentTimeMillis) {
                            return false;
                        }
                    }
                }
            } catch (SQLException e) {
                log.error("Failed to register name change for chr {}", getName(), e);
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement("INSERT INTO namechanges (characterid, old, new) VALUES (?, ?, ?)")) {
                ps.setInt(1, getId());
                ps.setString(2, getName());
                ps.setString(3, newName);
                ps.executeUpdate();
                this.pendingNameChange = true;
                return true;
            } catch (SQLException e) {
                log.error("Failed to register name change for chr {}", getName(), e);
            }
        } catch (SQLException e) {
            log.error("Failed to get DB connection while registering name change", e);
        }
        return false;
    }

    public boolean cancelPendingNameChange() {
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM namechanges WHERE characterid=? AND completionTime IS NULL")) {
            ps.setInt(1, getId());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                pendingNameChange = false;
            }
            return affectedRows > 0; //rows affected
        } catch (SQLException e) {
            log.error("Failed to cancel name change for chr {}", getName(), e);
            return false;
        }
    }

    public void doPendingNameChange() { //called on logout
        if (!pendingNameChange) {
            return;
        }

        try (Connection con = DatabaseConnection.getStaticConnection()) {
            int nameChangeId = -1;
            String newName = null;
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM namechanges WHERE characterid = ? AND completionTime IS NULL")) {
                ps.setInt(1, getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return;
                    }
                    nameChangeId = rs.getInt("id");
                    newName = rs.getString("new");
                }
            } catch (SQLException e) {
                log.error("Failed to retrieve pending name changes for chr {}", this.name, e);
            }

            con.setAutoCommit(false);
            boolean success = doNameChange(con, getId(), getName(), newName, nameChangeId);
            if (!success) {
                con.rollback();
            } else {
                log.info("Name change applied: from {} to {}", this.name, newName);
            }
            con.setAutoCommit(true);
        } catch (SQLException e) {
            log.error("Failed to get DB connection for pending chr name change", e);
        }
    }

    public int checkWorldTransferEligibility() {
        if (getLevel() < 20) {
            return 2;
        } else if (getClient().getTempBanCalendar() != null && getClient().getTempBanCalendar().getTimeInMillis() + (int) DAYS.toMillis(30) < Calendar.getInstance().getTimeInMillis()) {
            return 3;
        } else {
            return 0;
        }
    }

    public boolean registerWorldTransfer(int newWorld) {
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            //check for pending world transfer
            long currentTimeMillis = System.currentTimeMillis();
            try (PreparedStatement ps = con.prepareStatement("SELECT completionTime FROM worldtransfers WHERE characterid=?")) { //double check, just in case
                ps.setInt(1, getId());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Timestamp completedTimestamp = rs.getTimestamp("completionTime");
                    if (completedTimestamp == null) {
                        return false; //pending
                    } else if (completedTimestamp.getTime() + YamlConfig.config.server.WORLD_TRANSFER_COOLDOWN > currentTimeMillis) {
                        return false;
                    }
                }
            } catch (SQLException e) {
                log.error("Failed to register world transfer for chr {}", getName(), e);
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement("INSERT INTO worldtransfers (characterid, from, to) VALUES (?, ?, ?)")) {
                ps.setInt(1, getId());
                ps.setInt(2, getWorld());
                ps.setInt(3, newWorld);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                log.error("Failed to register world transfer for chr {}", getName(), e);
            }
        } catch (SQLException e) {
            log.error("Failed to get DB connection while registering world transfer", e);
        }
        return false;
    }

    public boolean cancelPendingWorldTranfer() {
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM worldtransfers WHERE characterid=? AND completionTime IS NULL")) {
            ps.setInt(1, getId());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0; //rows affected
        } catch (SQLException e) {
            log.error("Failed to cancel pending world transfer for chr {}", getName(), e);
            return false;
        }
    }

    public String getLastCommandMessage() {
        return this.commandtext;
    }

    public void setLastCommandMessage(String text) {
        this.commandtext = text;
    }

    public int getRewardPoints() {
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("SELECT rewardpoints FROM accounts WHERE id=?;")) {
            ps.setInt(1, accountid);
            ResultSet resultSet = ps.executeQuery();
            int point = -1;
            if (resultSet.next()) {
                point = resultSet.getInt(1);
            }
            return point;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void setRewardPoints(int value) {
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET rewardpoints=? WHERE id=?;")) {
            ps.setInt(1, value);
            ps.setInt(2, accountid);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public byte getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = (byte) team;
    }

    public boolean isChasing() {
        return chasing;
    }

    public void setChasing(boolean chasing) {
        this.chasing = chasing;
    }

    public enum FameStatus {

        OK, NOT_TODAY, NOT_THIS_MONTH
    }

    public enum DelayedQuestUpdate {    // quest updates allow player actions during NPC talk...
        UPDATE, FORFEIT, COMPLETE, INFO
    }

    private static class BuffStatValueHolder {

        public StatEffect effect;
        public long startTime;
        public int value;
        public boolean bestApplied;

        public BuffStatValueHolder(StatEffect effect, long startTime, int value) {
            super();
            this.effect = effect;
            this.startTime = startTime;
            this.value = value;
            this.bestApplied = false;
        }
    }

    public static class CooldownValueHolder {

        public int skillId;
        public long startTime, length;

        public CooldownValueHolder(int skillId, long startTime, long length) {
            super();
            this.skillId = skillId;
            this.startTime = startTime;
            this.length = length;
        }
    }

    public static class SkillEntry {

        public int masterlevel;
        public byte skillevel;
        public long expiration;

        public SkillEntry(byte skillevel, int masterlevel, long expiration) {
            this.skillevel = skillevel;
            this.masterlevel = masterlevel;
            this.expiration = expiration;
        }

        @Override
        public String toString() {
            return skillevel + ":" + masterlevel;
        }
    }
}
