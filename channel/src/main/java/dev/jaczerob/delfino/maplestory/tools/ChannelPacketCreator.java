package dev.jaczerob.delfino.maplestory.tools;

import dev.jaczerob.delfino.maplestory.client.BuddylistEntry;
import dev.jaczerob.delfino.maplestory.client.BuffStat;
import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Character.SkillEntry;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.Disease;
import dev.jaczerob.delfino.maplestory.client.FamilyEntitlement;
import dev.jaczerob.delfino.maplestory.client.FamilyEntry;
import dev.jaczerob.delfino.maplestory.client.MonsterBook;
import dev.jaczerob.delfino.maplestory.client.Mount;
import dev.jaczerob.delfino.maplestory.client.QuestStatus;
import dev.jaczerob.delfino.maplestory.client.Ring;
import dev.jaczerob.delfino.maplestory.client.Skill;
import dev.jaczerob.delfino.maplestory.client.SkillMacro;
import dev.jaczerob.delfino.maplestory.client.Stat;
import dev.jaczerob.delfino.maplestory.client.inventory.Equip;
import dev.jaczerob.delfino.maplestory.client.inventory.Equip.ScrollResult;
import dev.jaczerob.delfino.maplestory.client.inventory.Inventory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.ItemFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.ModifyInventory;
import dev.jaczerob.delfino.maplestory.client.inventory.Pet;
import dev.jaczerob.delfino.maplestory.client.keybind.KeyBinding;
import dev.jaczerob.delfino.maplestory.client.keybind.QuickslotBinding;
import dev.jaczerob.delfino.maplestory.client.newyear.NewYearCardRecord;
import dev.jaczerob.delfino.maplestory.client.status.MonsterStatus;
import dev.jaczerob.delfino.maplestory.client.status.MonsterStatusEffect;
import dev.jaczerob.delfino.maplestory.constants.game.ExpTable;
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.constants.id.MapId;
import dev.jaczerob.delfino.maplestory.constants.id.NpcId;
import dev.jaczerob.delfino.maplestory.constants.inventory.ItemConstants;
import dev.jaczerob.delfino.maplestory.constants.skills.Buccaneer;
import dev.jaczerob.delfino.maplestory.constants.skills.ChiefBandit;
import dev.jaczerob.delfino.maplestory.constants.skills.Corsair;
import dev.jaczerob.delfino.maplestory.constants.skills.ThunderBreaker;
import dev.jaczerob.delfino.maplestory.net.server.PlayerCoolDownValueHolder;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.net.server.guild.Alliance;
import dev.jaczerob.delfino.maplestory.net.server.guild.Guild;
import dev.jaczerob.delfino.maplestory.net.server.guild.GuildSummary;
import dev.jaczerob.delfino.maplestory.net.server.world.Party;
import dev.jaczerob.delfino.maplestory.net.server.world.PartyCharacter;
import dev.jaczerob.delfino.maplestory.net.server.world.PartyOperation;
import dev.jaczerob.delfino.maplestory.net.server.world.World;
import dev.jaczerob.delfino.maplestory.packets.handlers.AbstractDealDamageHandler.AttackTarget;
import dev.jaczerob.delfino.maplestory.packets.handlers.PlayerInteractionHandler;
import dev.jaczerob.delfino.maplestory.packets.handlers.SummonDamageHandler.SummonAttackTarget;
import dev.jaczerob.delfino.maplestory.packets.handlers.WhisperHandler;
import dev.jaczerob.delfino.maplestory.server.CashShop;
import dev.jaczerob.delfino.maplestory.server.CashShop.CashItem;
import dev.jaczerob.delfino.maplestory.server.CashShop.CashItemFactory;
import dev.jaczerob.delfino.maplestory.server.CashShop.SpecialCashItem;
import dev.jaczerob.delfino.maplestory.server.DueyPackage;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.server.MTSItemInfo;
import dev.jaczerob.delfino.maplestory.server.ShopItem;
import dev.jaczerob.delfino.maplestory.server.Trade;
import dev.jaczerob.delfino.maplestory.server.events.gm.Snowball;
import dev.jaczerob.delfino.maplestory.server.life.MobSkill;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillId;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.server.life.NPC;
import dev.jaczerob.delfino.maplestory.server.life.PlayerNPC;
import dev.jaczerob.delfino.maplestory.server.maps.AbstractMapObject;
import dev.jaczerob.delfino.maplestory.server.maps.Door;
import dev.jaczerob.delfino.maplestory.server.maps.DoorObject;
import dev.jaczerob.delfino.maplestory.server.maps.Dragon;
import dev.jaczerob.delfino.maplestory.server.maps.HiredMerchant;
import dev.jaczerob.delfino.maplestory.server.maps.MapItem;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.server.maps.MiniGame;
import dev.jaczerob.delfino.maplestory.server.maps.MiniGame.MiniGameResult;
import dev.jaczerob.delfino.maplestory.server.maps.Mist;
import dev.jaczerob.delfino.maplestory.server.maps.PlayerShop;
import dev.jaczerob.delfino.maplestory.server.maps.PlayerShopItem;
import dev.jaczerob.delfino.maplestory.server.maps.Reactor;
import dev.jaczerob.delfino.maplestory.server.maps.Summon;
import dev.jaczerob.delfino.maplestory.server.movement.LifeMovementFragment;
import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.ByteBufOutPacket;
import dev.jaczerob.delfino.network.packets.InPacket;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Component
public class ChannelPacketCreator {
    public static final List<Pair<Stat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();
    private static final long FT_UT_OFFSET = 116444736010800000L + (10000L * TimeZone.getDefault().getOffset(System.currentTimeMillis()));
    private static final long DEFAULT_TIME = 150842304000000000L;
    private static final long ZERO_TIME = 94354848000000000L;
    private static final long PERMANENT = 150841440000000000L;

    private static ChannelPacketCreator INSTANCE;

    public ChannelPacketCreator() {
        super();
        INSTANCE = this;
    }

    public static ChannelPacketCreator getInstance() {
        return INSTANCE;
    }

    public long getTime(long utcTimestamp) {
        if (utcTimestamp < 0 && utcTimestamp >= -3) {
            if (utcTimestamp == -1) {
                return DEFAULT_TIME;
            } else if (utcTimestamp == -2) {
                return ZERO_TIME;
            } else {
                return PERMANENT;
            }
        }

        return utcTimestamp * 10000 + FT_UT_OFFSET;
    }

    private void writeMobSkillId(OutPacket packet, MobSkillId msId) {
        packet.writeShort(msId.type().getId());
        packet.writeShort(msId.level());
    }

    public Packet showHpHealed(int cid, int amount) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_FOREIGN_EFFECT);
        p.writeInt(cid);
        p.writeByte(0x0A);
        p.writeByte(amount);
        return p;
    }

    private void addRemainingSkillInfo(final OutPacket p, Character chr) {
        int[] remainingSp = chr.getRemainingSps();
        int effectiveLength = 0;
        for (int j : remainingSp) {
            if (j > 0) {
                effectiveLength++;
            }
        }

        p.writeByte(effectiveLength);
        for (int i = 0; i < remainingSp.length; i++) {
            if (remainingSp[i] > 0) {
                p.writeByte(i + 1);
                p.writeByte(remainingSp[i]);
            }
        }
    }

    private void addCharStats(OutPacket p, Character chr) {
        p.writeInt(chr.getId());
        p.writeFixedString(StringUtil.getRightPaddedStr(chr.getName(), '\0', 13));
        p.writeByte(chr.getGender());
        p.writeByte(chr.getSkinColor().getId());
        p.writeInt(chr.getFace());
        p.writeInt(chr.getHair());

        for (int i = 0; i < 3; i++) {
            Pet pet = chr.getPet(i);
            if (pet != null) {
                p.writeLong(pet.getUniqueId());
            } else {
                p.writeLong(0);
            }
        }

        p.writeByte(chr.getLevel());
        p.writeShort(chr.getJob().getId());
        p.writeShort(chr.getStr());
        p.writeShort(chr.getDex());
        p.writeShort(chr.getInt());
        p.writeShort(chr.getLuk());
        p.writeShort(chr.getHp());
        p.writeShort(chr.getClientMaxHp());
        p.writeShort(chr.getMp());
        p.writeShort(chr.getClientMaxMp());
        p.writeShort(chr.getRemainingAp());
        if (GameConstants.hasSPTable(chr.getJob())) {
            addRemainingSkillInfo(p, chr);
        } else {
            p.writeShort(chr.getRemainingSp());
        }
        p.writeInt(chr.getExp());
        p.writeShort(chr.getFame());
        p.writeInt(chr.getGachaExp());
        p.writeInt(chr.getMapId());
        p.writeByte(chr.getInitialSpawnpoint());
        p.writeInt(0);
    }

    protected void addCharLook(final OutPacket p, Character chr, boolean mega) {
        p.writeByte(chr.getGender());
        p.writeByte(chr.getSkinColor().getId());
        p.writeInt(chr.getFace());
        p.writeBool(!mega);
        p.writeInt(chr.getHair());
        addCharEquips(p, chr);
    }

    private void addCharacterInfo(OutPacket p, Character chr) {
        p.writeLong(-1);
        p.writeByte(0);
        addCharStats(p, chr);
        p.writeByte(chr.getBuddylist().getCapacity());

        if (chr.getLinkedName() == null) {
            p.writeByte(0);
        } else {
            p.writeByte(1);
            p.writeString(chr.getLinkedName());
        }

        p.writeInt(chr.getMeso());
        addInventoryInfo(p, chr);
        addSkillInfo(p, chr);
        addQuestInfo(p, chr);
        addMiniGameInfo(p, chr);
        addRingInfo(p, chr);
        addTeleportInfo(p, chr);
        addMonsterBookInfo(p, chr);
        addNewYearInfo(p, chr);
        addAreaInfo(p, chr);
        p.writeShort(0);
    }

    private void addNewYearInfo(OutPacket p, Character chr) {
        Set<NewYearCardRecord> received = chr.getReceivedNewYearRecords();

        p.writeShort(received.size());
        for (NewYearCardRecord nyc : received) {
            encodeNewYearCard(nyc, p);
        }
    }

    private void addTeleportInfo(OutPacket p, Character chr) {
        final List<Integer> tele = chr.getTrockMaps();
        final List<Integer> viptele = chr.getVipTrockMaps();
        for (int i = 0; i < 5; i++) {
            p.writeInt(tele.get(i));
        }
        for (int i = 0; i < 10; i++) {
            p.writeInt(viptele.get(i));
        }
    }

    private void addMiniGameInfo(OutPacket p, Character chr) {
        p.writeShort(0);
    }

    private void addAreaInfo(OutPacket p, Character chr) {
        Map<Short, String> areaInfos = chr.getAreaInfos();
        p.writeShort(areaInfos.size());
        for (Short area : areaInfos.keySet()) {
            p.writeShort(area);
            p.writeString(areaInfos.get(area));
        }
    }

    private void addCharEquips(final OutPacket p, Character chr) {
        Inventory equip = chr.getInventory(InventoryType.EQUIPPED);
        Collection<Item> ii = ItemInformationProvider.getInstance().canWearEquipment(chr, equip.list());
        Map<Short, Integer> myEquip = new LinkedHashMap<>();
        Map<Short, Integer> maskedEquip = new LinkedHashMap<>();
        for (Item item : ii) {
            short pos = (byte) (item.getPosition() * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, item.getItemId());
            } else if (pos > 100 && pos != 111) {
                pos -= 100;
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, item.getItemId());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, item.getItemId());
            }
        }
        for (Entry<Short, Integer> entry : myEquip.entrySet()) {
            p.writeByte(entry.getKey());
            p.writeInt(entry.getValue());
        }
        p.writeByte(0xFF);
        for (Entry<Short, Integer> entry : maskedEquip.entrySet()) {
            p.writeByte(entry.getKey());
            p.writeInt(entry.getValue());
        }
        p.writeByte(0xFF);
        Item cWeapon = equip.getItem((short) -111);
        p.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
        for (int i = 0; i < 3; i++) {
            if (chr.getPet(i) != null) {
                p.writeInt(chr.getPet(i).getItemId());
            } else {
                p.writeInt(0);
            }
        }
    }

    private void addCharEntry(OutPacket p, Character chr, boolean viewall) {
        addCharStats(p, chr);
        addCharLook(p, chr, false);
        if (!viewall) {
            p.writeByte(0);
        }
        if (chr.isGM() || chr.isGmJob()) {
            p.writeByte(0);
            return;
        }
        p.writeByte(1);
        p.writeInt(chr.getRank());
        p.writeInt(chr.getRankMove());
        p.writeInt(chr.getJobRank());
        p.writeInt(chr.getJobRankMove());
    }

    private void addQuestInfo(OutPacket p, Character chr) {
        List<QuestStatus> started = chr.getStartedQuests();
        int startedSize = 0;
        for (QuestStatus qs : started) {
            if (qs.getInfoNumber() > 0) {
                startedSize++;
            }
            startedSize++;
        }
        p.writeShort(startedSize);
        for (QuestStatus qs : started) {
            p.writeShort(qs.getQuest().getId());
            p.writeString(qs.getProgressData());

            short infoNumber = qs.getInfoNumber();
            if (infoNumber > 0) {
                QuestStatus iqs = chr.getQuest(infoNumber);
                p.writeShort(infoNumber);
                p.writeString(iqs.getProgressData());
            }
        }
        List<QuestStatus> completed = chr.getCompletedQuests();
        p.writeShort(completed.size());
        for (QuestStatus qs : completed) {
            p.writeShort(qs.getQuest().getId());
            p.writeLong(getTime(qs.getCompletionTime()));
        }
    }

    private void addExpirationTime(final OutPacket p, long time) {
        p.writeLong(getTime(time));
    }

    private void addItemInfo(OutPacket p, Item item) {
        addItemInfo(p, item, false);
    }

    protected void addItemInfo(final OutPacket p, Item item, boolean zeroPosition) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        boolean isCash = ii.isCash(item.getItemId());
        boolean isPet = item.getPetId() > -1;
        boolean isRing = false;
        Equip equip = null;
        short pos = item.getPosition();
        byte itemType = item.getItemType();
        if (itemType == 1) {
            equip = (Equip) item;
            isRing = equip.getRingId() > -1;
        }
        if (!zeroPosition) {
            if (equip != null) {
                if (pos < 0) {
                    pos *= -1;
                }
                p.writeShort(pos > 100 ? pos - 100 : pos);
            } else {
                p.writeByte(pos);
            }
        }
        p.writeByte(itemType);
        p.writeInt(item.getItemId());
        p.writeBool(isCash);
        if (isCash) {
            p.writeLong(isPet ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
        }
        addExpirationTime(p, item.getExpiration());
        if (isPet) {
            Pet pet = item.getPet();
            p.writeFixedString(StringUtil.getRightPaddedStr(pet.getName(), '\0', 13));
            p.writeByte(pet.getLevel());
            p.writeShort(pet.getTameness());
            p.writeByte(pet.getFullness());
            addExpirationTime(p, item.getExpiration());
            p.writeShort(pet.getPetAttribute());
            p.writeShort(0);
            p.writeInt(18000);
            p.writeShort(0);
            return;
        }
        if (equip == null) {
            p.writeShort(item.getQuantity());
            p.writeString(item.getOwner());
            p.writeShort(item.getFlag());

            if (ItemConstants.isRechargeable(item.getItemId())) {
                p.writeInt(2);
                p.writeBytes(new byte[]{(byte) 0x54, 0, 0, (byte) 0x34});
            }
            return;
        }
        p.writeByte(equip.getUpgradeSlots());
        p.writeByte(equip.getLevel());
        p.writeShort(equip.getStr());
        p.writeShort(equip.getDex());
        p.writeShort(equip.getInt());
        p.writeShort(equip.getLuk());
        p.writeShort(equip.getHp());
        p.writeShort(equip.getMp());
        p.writeShort(equip.getWatk());
        p.writeShort(equip.getMatk());
        p.writeShort(equip.getWdef());
        p.writeShort(equip.getMdef());
        p.writeShort(equip.getAcc());
        p.writeShort(equip.getAvoid());
        p.writeShort(equip.getHands());
        p.writeShort(equip.getSpeed());
        p.writeShort(equip.getJump());
        p.writeString(equip.getOwner());
        p.writeShort(equip.getFlag());

        if (isCash) {
            for (int i = 0; i < 10; i++) {
                p.writeByte(0x40);
            }
        } else {
            int itemLevel = equip.getItemLevel();

            long expNibble = ((long) ExpTable.getExpNeededForLevel(ii.getEquipLevelReq(item.getItemId())) * equip.getItemExp());
            expNibble /= ExpTable.getEquipExpNeededForLevel(itemLevel);

            p.writeByte(0);
            p.writeByte(itemLevel);
            p.writeInt((int) expNibble);
            p.writeInt(equip.getVicious());
            p.writeLong(0);
        }
        p.writeLong(getTime(-2));
        p.writeInt(-1);

    }

    private void addInventoryInfo(OutPacket p, Character chr) {
        for (byte i = 1; i <= 5; i++) {
            p.writeByte(chr.getInventory(InventoryType.getByType(i)).getSlotLimit());
        }
        p.writeLong(getTime(-2));
        Inventory iv = chr.getInventory(InventoryType.EQUIPPED);
        Collection<Item> equippedC = iv.list();
        List<Item> equipped = new ArrayList<>(equippedC.size());
        List<Item> equippedCash = new ArrayList<>(equippedC.size());
        for (Item item : equippedC) {
            if (item.getPosition() <= -100) {
                equippedCash.add(item);
            } else {
                equipped.add(item);
            }
        }
        for (Item item : equipped) {
            addItemInfo(p, item);
        }
        p.writeShort(0);
        for (Item item : equippedCash) {
            addItemInfo(p, item);
        }
        p.writeShort(0);
        for (Item item : chr.getInventory(InventoryType.EQUIP).list()) {
            addItemInfo(p, item);
        }
        p.writeInt(0);
        for (Item item : chr.getInventory(InventoryType.USE).list()) {
            addItemInfo(p, item);
        }
        p.writeByte(0);
        for (Item item : chr.getInventory(InventoryType.SETUP).list()) {
            addItemInfo(p, item);
        }
        p.writeByte(0);
        for (Item item : chr.getInventory(InventoryType.ETC).list()) {
            addItemInfo(p, item);
        }
        p.writeByte(0);
        for (Item item : chr.getInventory(InventoryType.CASH).list()) {
            addItemInfo(p, item);
        }
    }

    private void addSkillInfo(OutPacket p, Character chr) {
        p.writeByte(0);
        Map<Skill, SkillEntry> skills = chr.getSkills();
        int skillsSize = skills.size();

        for (Entry<Skill, SkillEntry> skill : skills.entrySet()) {
            if (GameConstants.isHiddenSkills(skill.getKey().getId())) {
                skillsSize--;
            }
        }
        p.writeShort(skillsSize);
        for (Entry<Skill, SkillEntry> skill : skills.entrySet()) {
            if (GameConstants.isHiddenSkills(skill.getKey().getId())) {
                continue;
            }
            p.writeInt(skill.getKey().getId());
            p.writeInt(skill.getValue().skillevel);
            addExpirationTime(p, skill.getValue().expiration);
            if (skill.getKey().isFourthJob()) {
                p.writeInt(skill.getValue().masterlevel);
            }
        }
        p.writeShort(chr.getAllCooldowns().size());
        for (PlayerCoolDownValueHolder cooling : chr.getAllCooldowns()) {
            p.writeInt(cooling.skillId);
            int timeLeft = (int) (cooling.length + cooling.startTime - System.currentTimeMillis());
            p.writeShort(timeLeft / 1000);
        }
    }

    private void addMonsterBookInfo(OutPacket p, Character chr) {
        p.writeInt(chr.getMonsterBookCover());
        p.writeByte(0);
        Map<Integer, Integer> cards = chr.getMonsterBook().getCards();
        p.writeShort(cards.size());
        for (Entry<Integer, Integer> all : cards.entrySet()) {
            p.writeShort(all.getKey() % 10000);
            p.writeByte(all.getValue());
        }
    }

    public Packet getPing() {
        return OutPacket.create(SendOpcode.PING);
    }

    public Packet getAfterLoginError(int reason) {
        OutPacket p = OutPacket.create(SendOpcode.SELECT_CHARACTER_BY_VAC);
        p.writeShort(reason);
        return p;
    }

    public Packet sendPolice(String text) {
        final OutPacket p = OutPacket.create(SendOpcode.DATA_CRC_CHECK_FAILED);
        p.writeString(text);
        return p;
    }

    public Packet getChannelChange(InetAddress inetAddr, int port) {
        final OutPacket p = OutPacket.create(SendOpcode.CHANGE_CHANNEL);
        p.writeByte(1);
        byte[] addr = inetAddr.getAddress();
        p.writeBytes(addr);
        p.writeShort(port);
        return p;
    }

    public Packet enableTV() {
        OutPacket p = OutPacket.create(SendOpcode.ENABLE_TV);
        p.writeInt(0);
        p.writeByte(0);
        return p;
    }

    public Packet removeTV() {
        return OutPacket.create(SendOpcode.REMOVE_TV);
    }

    public Packet sendTV(Character chr, List<String> messages, int type, Character partner) {
        final OutPacket p = OutPacket.create(SendOpcode.SEND_TV);
        p.writeByte(partner != null ? 3 : 1);
        p.writeByte(type);
        addCharLook(p, chr, false);
        p.writeString(chr.getName());
        if (partner != null) {
            p.writeString(partner.getName());
        } else {
            p.writeShort(0);
        }
        for (int i = 0; i < messages.size(); i++) {
            if (i == 4 && messages.get(4).length() > 15) {
                p.writeString(messages.get(4).substring(0, 15));
            } else {
                p.writeString(messages.get(i));
            }
        }
        p.writeInt(1337);
        if (partner != null) {
            addCharLook(p, partner, false);
        }
        return p;
    }

    public Packet getCharInfo(Character chr) {
        final OutPacket p = OutPacket.create(SendOpcode.SET_FIELD);
        p.writeInt(chr.getClient().getChannel() - 1);
        p.writeByte(1);
        p.writeByte(1);
        p.writeShort(0);
        for (int i = 0; i < 3; i++) {
            p.writeInt(Randomizer.nextInt());
        }
        addCharacterInfo(p, chr);
        p.writeLong(getTime(System.currentTimeMillis()));
        return p;
    }

    public Packet enableActions() {
        return updatePlayerStats(EMPTY_STATUPDATE, true, null);
    }

    public Packet updatePlayerStats(List<Pair<Stat, Integer>> stats, boolean enableActions, Character chr) {
        OutPacket p = OutPacket.create(SendOpcode.STAT_CHANGED);
        p.writeBool(enableActions);
        int updateMask = 0;
        for (Pair<Stat, Integer> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }
        List<Pair<Stat, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            mystats.sort((o1, o2) -> {
                int val1 = o1.getLeft().getValue();
                int val2 = o2.getLeft().getValue();
                return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
            });
        }
        p.writeInt(updateMask);
        for (Pair<Stat, Integer> statupdate : mystats) {
            if (statupdate.getLeft().getValue() >= 1) {
                if (statupdate.getLeft().getValue() == 0x1) {
                    p.writeByte(statupdate.getRight().byteValue());
                } else if (statupdate.getLeft().getValue() <= 0x4) {
                    p.writeInt(statupdate.getRight());
                } else if (statupdate.getLeft().getValue() < 0x20) {
                    p.writeByte(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() == 0x8000) {
                    if (GameConstants.hasSPTable(chr.getJob())) {
                        addRemainingSkillInfo(p, chr);
                    } else {
                        p.writeShort(statupdate.getRight().shortValue());
                    }
                } else if (statupdate.getLeft().getValue() < 0xFFFF) {
                    p.writeShort(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() == 0x20000) {
                    p.writeShort(statupdate.getRight().shortValue());
                } else {
                    p.writeInt(statupdate.getRight());
                }
            }
        }
        return p;
    }

    public Packet getWarpToMap(MapleMap to, int spawnPoint, Character chr) {
        final OutPacket p = OutPacket.create(SendOpcode.SET_FIELD);
        p.writeInt(chr.getClient().getChannel() - 1);
        p.writeInt(0);
        p.writeByte(0);
        p.writeInt(to.getId());
        p.writeByte(spawnPoint);
        p.writeShort(chr.getHp());
        p.writeBool(chr.isChasing());
        if (chr.isChasing()) {
            chr.setChasing(false);
            p.writeInt(chr.getPosition().x);
            p.writeInt(chr.getPosition().y);
        }
        p.writeLong(getTime(Server.getInstance().getCurrentTime()));
        return p;
    }

    public Packet getWarpToMap(MapleMap to, int spawnPoint, Point spawnPosition, Character chr) {
        final OutPacket p = OutPacket.create(SendOpcode.SET_FIELD);
        p.writeInt(chr.getClient().getChannel() - 1);
        p.writeInt(0);
        p.writeByte(0);
        p.writeInt(to.getId());
        p.writeByte(spawnPoint);
        p.writeShort(chr.getHp());
        p.writeBool(true);
        p.writeInt(spawnPosition.x);
        p.writeInt(spawnPosition.y);
        p.writeLong(getTime(Server.getInstance().getCurrentTime()));
        return p;
    }

    public Packet spawnPortal(int townId, int targetId, Point pos) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_PORTAL);
        p.writeInt(townId);
        p.writeInt(targetId);
        p.writePos(pos);
        return p;
    }

    public Packet spawnDoor(int ownerid, Point pos, boolean launched) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_DOOR);
        p.writeBool(launched);
        p.writeInt(ownerid);
        p.writePos(pos);
        return p;
    }

    public Packet removeDoor(int ownerId, boolean town) {
        final OutPacket p;
        if (town) {
            p = OutPacket.create(SendOpcode.SPAWN_PORTAL);
            p.writeInt(MapId.NONE);
            p.writeInt(MapId.NONE);
        } else {
            p = OutPacket.create(SendOpcode.REMOVE_DOOR);
            p.writeByte(0);
            p.writeInt(ownerId);
        }
        return p;
    }

    public Packet spawnSummon(Summon summon, boolean animated) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_SPECIAL_MAPOBJECT);
        p.writeInt(summon.getOwner().getId());
        p.writeInt(summon.getObjectId());
        p.writeInt(summon.getSkill());
        p.writeByte(0x0A);
        p.writeByte(summon.getSkillLevel());
        p.writePos(summon.getPosition());
        p.writeByte(summon.getStance());
        p.writeShort(0);
        p.writeByte(summon.getMovementType().getValue());
        p.writeBool(!summon.isPuppet());
        p.writeBool(!animated);
        return p;
    }

    public Packet removeSummon(Summon summon, boolean animated) {
        OutPacket p = OutPacket.create(SendOpcode.REMOVE_SPECIAL_MAPOBJECT);
        p.writeInt(summon.getOwner().getId());
        p.writeInt(summon.getObjectId());
        p.writeByte(animated ? 4 : 1);
        return p;
    }

    public Packet spawnKite(int objId, int itemId, String name, String msg, Point pos, int ft) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_KITE);
        p.writeInt(objId);
        p.writeInt(itemId);
        p.writeString(msg);
        p.writeString(name);
        p.writeShort(pos.x);
        p.writeShort(ft);
        return p;
    }

    public Packet removeKite(int objId, int animationType) {
        OutPacket p = OutPacket.create(SendOpcode.REMOVE_KITE);
        p.writeByte(animationType);
        p.writeInt(objId);
        return p;
    }

    public Packet sendCannotSpawnKite() {
        return OutPacket.create(SendOpcode.CANNOT_SPAWN_KITE);
    }

    public Packet getRelogResponse() {
        OutPacket p = OutPacket.create(SendOpcode.RELOG_RESPONSE);
        p.writeByte(1);
        return p;
    }

    public Packet serverMessage(String message) {
        return serverMessage(4, (byte) 0, message, true, false, 0);
    }

    public Packet serverNotice(int type, String message) {
        return serverMessage(type, (byte) 0, message, false, false, 0);
    }

    public Packet serverNotice(int type, String message, int npc) {
        return serverMessage(type, 0, message, false, false, npc);
    }

    public Packet serverNotice(int type, int channel, String message) {
        return serverMessage(type, channel, message, false, false, 0);
    }

    public Packet serverNotice(int type, int channel, String message, boolean smegaEar) {
        return serverMessage(type, channel, message, false, smegaEar, 0);
    }

    private Packet serverMessage(int type, int channel, String message, boolean servermessage, boolean megaEar, int npc) {
        OutPacket p = OutPacket.create(SendOpcode.SERVERMESSAGE);
        p.writeByte(type);
        if (servermessage) {
            p.writeByte(1);
        }
        p.writeString(message);
        if (type == 3) {
            p.writeByte(channel - 1);
            p.writeBool(megaEar);
        } else if (type == 6) {
            p.writeInt(0);
        } else if (type == 7) {
            p.writeInt(npc);
        }
        return p;
    }

    public Packet getAvatarMega(Character chr, String medal, int channel, int itemId, List<String> message, boolean ear) {
        final OutPacket p = OutPacket.create(SendOpcode.SET_AVATAR_MEGAPHONE);
        p.writeInt(itemId);
        p.writeString(medal + chr.getName());
        for (String s : message) {
            p.writeString(s);
        }
        p.writeInt(channel - 1);
        p.writeBool(ear);
        addCharLook(p, chr, true);
        return p;
    }

    public Packet byeAvatarMega() {
        final OutPacket p = OutPacket.create(SendOpcode.CLEAR_AVATAR_MEGAPHONE);
        p.writeByte(1);
        return p;
    }

    public Packet gachaponMessage(Item item, String town, Character player) {
        final OutPacket p = OutPacket.create(SendOpcode.SERVERMESSAGE);
        p.writeByte(0x0B);
        p.writeString(player.getName() + " : got a(n)");
        p.writeInt(0);
        p.writeString(town);
        addItemInfo(p, item, true);
        return p;
    }

    public Packet spawnNPC(NPC life) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_NPC);
        p.writeInt(life.getObjectId());
        p.writeInt(life.getId());
        p.writeShort(life.getPosition().x);
        p.writeShort(life.getCy());
        p.writeBool(life.getF() != 1);
        p.writeShort(life.getFh());
        p.writeShort(life.getRx0());
        p.writeShort(life.getRx1());
        p.writeByte(1);
        return p;
    }

    public Packet spawnNPCRequestController(NPC life, boolean miniMap) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER);
        p.writeByte(1);
        p.writeInt(life.getObjectId());
        p.writeInt(life.getId());
        p.writeShort(life.getPosition().x);
        p.writeShort(life.getCy());
        p.writeBool(life.getF() != 1);
        p.writeShort(life.getFh());
        p.writeShort(life.getRx0());
        p.writeShort(life.getRx1());
        p.writeBool(miniMap);
        return p;
    }

    public Packet spawnMonster(Monster life, boolean newSpawn) {
        return spawnMonsterInternal(life, false, newSpawn, false, 0, false);
    }

    public Packet spawnMonster(Monster life, boolean newSpawn, int effect) {
        return spawnMonsterInternal(life, false, newSpawn, false, effect, false);
    }

    public Packet controlMonster(Monster life, boolean newSpawn, boolean aggro) {
        return spawnMonsterInternal(life, true, newSpawn, aggro, 0, false);
    }

    private void encodeParentlessMobSpawnEffect(OutPacket p, boolean newSpawn, int effect) {
        if (effect > 0) {
            p.writeByte(effect);
            p.writeByte(0);
            p.writeShort(0);
            if (effect == 15) {
                p.writeByte(0);
            }
        }
        p.writeByte(newSpawn ? -2 : -1);
    }

    private void encodeTemporary(OutPacket p, Map<MonsterStatus, MonsterStatusEffect> stati) {
        int pCounter = -1;
        int mCounter = -1;

        stati = stati.entrySet()
                .stream()
                .filter(e -> !(e.getKey().equals(MonsterStatus.WATK) || e.getKey().equals(MonsterStatus.WDEF)))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        writeLongEncodeTemporaryMask(p, stati.keySet());

        for (Entry<MonsterStatus, MonsterStatusEffect> s : stati.entrySet()) {
            MonsterStatusEffect mse = s.getValue();
            p.writeShort(mse.getStati().get(s.getKey()));

            MobSkill mobSkill = mse.getMobSkill();
            if (mobSkill != null) {
                writeMobSkillId(p, mobSkill.getId());

                switch (s.getKey()) {
                    case WEAPON_REFLECT -> pCounter = mobSkill.getX();
                    case MAGIC_REFLECT -> mCounter = mobSkill.getY();
                }
            } else {
                Skill skill = mse.getSkill();
                p.writeInt(skill != null ? skill.getId() : 0);
            }

            p.writeShort(-1);
        }


        if (pCounter != -1) {
            p.writeInt(pCounter);
        }
        if (mCounter != -1) {
            p.writeInt(mCounter);
        }
        if (pCounter != -1 || mCounter != -1) {
            p.writeInt(100);
        }
    }

    private Packet spawnMonsterInternal(Monster life, boolean requestController, boolean newSpawn, boolean aggro, int effect, boolean makeInvis) {
        if (makeInvis) {
            OutPacket p = OutPacket.create(SendOpcode.SPAWN_MONSTER_CONTROL);
            p.writeByte(0);
            p.writeInt(life.getObjectId());
            return p;
        }

        final OutPacket p;
        if (requestController) {
            p = OutPacket.create(SendOpcode.SPAWN_MONSTER_CONTROL);
            p.writeByte(aggro ? 2 : 1);
        } else {
            p = OutPacket.create(SendOpcode.SPAWN_MONSTER);
        }

        p.writeInt(life.getObjectId());
        p.writeByte(life.getController() == null ? 5 : 1);
        p.writeInt(life.getId());

        if (requestController) {
            encodeTemporary(p, life.getStati());
        } else {
            p.skip(16);
        }

        p.writePos(life.getPosition());
        p.writeByte(life.getStance());
        p.writeShort(0);
        p.writeShort(life.getFh());


        /**
         * -4: Fake -3: Appear after linked mob is dead -2: Fade in 1: Smoke 3:
         * King Slime spawn 4: Summoning rock thing, used for 3rd job? 6:
         * Magical shit 7: Smoke shit 8: 'The Boss' 9/10: Grim phantom shit?
         * 11/12: Nothing? 13: Frankenstein 14: Angry ^ 15: Orb animation thing,
         * ?? 16: ?? 19: Mushroom castle boss thing
         */

        if (life.getParentMobOid() != 0) {
            Monster parentMob = life.getMap().getMonsterByOid(life.getParentMobOid());
            if (parentMob != null && parentMob.isAlive()) {
                p.writeByte(effect != 0 ? effect : -3);
                p.writeInt(life.getParentMobOid());
            } else {
                encodeParentlessMobSpawnEffect(p, newSpawn, effect);
            }
        } else {
            encodeParentlessMobSpawnEffect(p, newSpawn, effect);
        }

        p.writeByte(life.getTeam());
        p.writeInt(0);
        return p;
    }

    public Packet spawnFakeMonster(Monster life, int effect) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_MONSTER_CONTROL);
        p.writeByte(1);
        p.writeInt(life.getObjectId());
        p.writeByte(5);
        p.writeInt(life.getId());
        encodeTemporary(p, life.getStati());
        p.writePos(life.getPosition());
        p.writeByte(life.getStance());
        p.writeShort(0);
        p.writeShort(life.getFh());
        if (effect > 0) {
            p.writeByte(effect);
            p.writeByte(0);
            p.writeShort(0);
        }
        p.writeShort(-2);
        p.writeByte(life.getTeam());
        p.writeInt(0);
        return p;
    }

    public Packet makeMonsterReal(Monster life) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_MONSTER);
        p.writeInt(life.getObjectId());
        p.writeByte(5);
        p.writeInt(life.getId());
        encodeTemporary(p, life.getStati());
        p.writePos(life.getPosition());
        p.writeByte(life.getStance());
        p.writeShort(0);
        p.writeShort(life.getFh());
        p.writeShort(-1);
        p.writeInt(0);
        return p;
    }

    public Packet stopControllingMonster(int oid) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_MONSTER_CONTROL);
        p.writeByte(0);
        p.writeInt(oid);
        return p;
    }

    public Packet moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills) {
        return moveMonsterResponse(objectid, moveid, currentMp, useSkills, 0, 0);
    }

    public Packet moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
        OutPacket p = OutPacket.create(SendOpcode.MOVE_MONSTER_RESPONSE);
        p.writeInt(objectid);
        p.writeShort(moveid);
        p.writeBool(useSkills);
        p.writeShort(currentMp);
        p.writeByte(skillId);
        p.writeByte(skillLevel);
        return p;
    }

    public Packet getChatText(int cidfrom, String text, boolean gm, int show) {
        final OutPacket p = OutPacket.create(SendOpcode.CHATTEXT);
        p.writeInt(cidfrom);
        p.writeBool(gm);
        p.writeString(text);
        p.writeByte(show);
        return p;
    }

    public Packet getShowExpGain(int gain, int equip, int party, boolean inChat, boolean white) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_STATUS_INFO);
        p.writeByte(3);
        p.writeBool(white);
        p.writeInt(gain);
        p.writeBool(inChat);
        p.writeInt(0);
        p.writeByte(0);
        p.writeByte(0);
        p.writeInt(0);
        if (inChat) {
            p.writeByte(0);
        }

        p.writeByte(0);
        p.writeInt(party);
        p.writeInt(equip);
        p.writeInt(0);
        p.writeInt(0);
        return p;
    }

    public Packet getShowFameGain(int gain) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_STATUS_INFO);
        p.writeByte(4);
        p.writeInt(gain);
        return p;
    }

    public Packet getShowMesoGain(int gain, boolean inChat) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_STATUS_INFO);
        if (!inChat) {
            p.writeByte(0);
            p.writeShort(1);
        } else {
            p.writeByte(5);
        }
        p.writeInt(gain);
        p.writeShort(0);
        return p;
    }

    public Packet getShowItemGain(int itemId, short quantity) {
        return getShowItemGain(itemId, quantity, false);
    }

    public Packet getShowItemGain(int itemId, short quantity, boolean inChat) {
        final OutPacket p;
        if (inChat) {
            p = OutPacket.create(SendOpcode.SHOW_ITEM_GAIN_INCHAT);
            p.writeByte(3);
            p.writeByte(1);
            p.writeInt(itemId);
            p.writeInt(quantity);
        } else {
            p = OutPacket.create(SendOpcode.SHOW_STATUS_INFO);
            p.writeShort(0);
            p.writeInt(itemId);
            p.writeInt(quantity);
            p.writeInt(0);
            p.writeInt(0);
        }
        return p;
    }

    public Packet killMonster(int objId, boolean animation) {
        return killMonster(objId, animation ? 1 : 0);
    }

    public Packet killMonster(int objId, int animation) {
        OutPacket p = OutPacket.create(SendOpcode.KILL_MONSTER);
        p.writeInt(objId);
        p.writeByte(animation);
        p.writeByte(animation);
        return p;
    }

    public Packet updateMapItemObject(MapItem drop, boolean giveOwnership) {
        OutPacket p = OutPacket.create(SendOpcode.DROP_ITEM_FROM_MAPOBJECT);
        p.writeByte(2);
        p.writeInt(drop.getObjectId());
        p.writeBool(drop.getMeso() > 0);
        p.writeInt(drop.getItemId());
        p.writeInt(giveOwnership ? 0 : -1);
        p.writeByte(drop.hasExpiredOwnershipTime() ? 2 : drop.getDropType());
        p.writePos(drop.getPosition());
        p.writeInt(giveOwnership ? 0 : -1);

        if (drop.getMeso() == 0) {
            addExpirationTime(p, drop.getItem().getExpiration());
        }
        p.writeBool(!drop.isPlayerDrop());
        return p;
    }

    public Packet dropItemFromMapObject(Character player, MapItem drop, Point dropfrom, Point dropto, byte mod,
                                        short delay) {
        int dropType = drop.getDropType();
        if (drop.hasClientsideOwnership(player) && dropType < 3) {
            dropType = 2;
        }

        OutPacket p = OutPacket.create(SendOpcode.DROP_ITEM_FROM_MAPOBJECT);
        p.writeByte(mod);
        p.writeInt(drop.getObjectId());
        p.writeBool(drop.getMeso() > 0);
        p.writeInt(drop.getItemId());
        p.writeInt(drop.getClientsideOwnerId());
        p.writeByte(dropType);
        p.writePos(dropto);
        p.writeInt(drop.getDropper().getObjectId());

        if (mod != 2) {
            p.writePos(dropfrom);
            p.writeShort(delay);
        }
        if (drop.getMeso() == 0) {
            addExpirationTime(p, drop.getItem().getExpiration());
        }
        p.writeByte(drop.isPlayerDrop() ? 0 : 1);
        return p;
    }

    private void writeForeignBuffs(OutPacket p, Character chr) {
        p.writeInt(0);
        p.writeShort(0);
        p.writeByte(0xFC);
        p.writeByte(1);
        if (chr.getBuffedValue(BuffStat.MORPH) != null) {
            p.writeInt(2);
        } else {
            p.writeInt(0);
        }
        long buffmask = 0;
        Integer buffvalue = null;
        if ((chr.getBuffedValue(BuffStat.DARKSIGHT) != null || chr.getBuffedValue(BuffStat.WIND_WALK) != null) && !chr.isHidden()) {
            buffmask |= BuffStat.DARKSIGHT.getValue();
        }
        if (chr.getBuffedValue(BuffStat.COMBO) != null) {
            buffmask |= BuffStat.COMBO.getValue();
            buffvalue = chr.getBuffedValue(BuffStat.COMBO);
        }
        if (chr.getBuffedValue(BuffStat.SHADOWPARTNER) != null) {
            buffmask |= BuffStat.SHADOWPARTNER.getValue();
        }
        if (chr.getBuffedValue(BuffStat.SOULARROW) != null) {
            buffmask |= BuffStat.SOULARROW.getValue();
        }
        if (chr.getBuffedValue(BuffStat.MORPH) != null) {
            buffvalue = chr.getBuffedValue(BuffStat.MORPH);
        }
        p.writeInt((int) ((buffmask >> 32) & 0xffffffffL));
        if (buffvalue != null) {
            if (chr.getBuffedValue(BuffStat.MORPH) != null) {
                p.writeShort(buffvalue);
            } else {
                p.writeByte(buffvalue.byteValue());
            }
        }
        p.writeInt((int) (buffmask & 0xffffffffL));


        p.writeInt(chr.getEnergyBar() == 15000 ? 1 : 0);
        p.writeShort(0);
        p.skip(4);

        boolean dashBuff = chr.getBuffedValue(BuffStat.DASH) != null;

        p.writeInt(dashBuff ? 1 << 24 : 0);
        p.skip(11);
        p.writeShort(0);

        p.skip(9);
        p.writeInt(dashBuff ? 1 << 24 : 0);
        p.writeShort(0);
        p.writeByte(0);


        Integer bv = chr.getBuffedValue(BuffStat.MONSTER_RIDING);
        if (bv != null) {
            Mount mount = chr.getMount();
            if (mount != null) {
                p.writeInt(mount.getItemId());
                p.writeInt(mount.getSkillId());
            } else {
                p.writeLong(0);
            }
        } else {
            p.writeLong(0);
        }

        int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
        p.writeInt(CHAR_MAGIC_SPAWN);

        p.skip(8);
        p.writeInt(CHAR_MAGIC_SPAWN);
        p.writeByte(0);
        p.writeInt(CHAR_MAGIC_SPAWN);
        p.writeShort(0);

        p.skip(9);
        p.writeInt(CHAR_MAGIC_SPAWN);
        p.writeInt(0);

        p.skip(9);
        p.writeInt(CHAR_MAGIC_SPAWN);
        p.writeShort(0);
        p.writeShort(0);
    }

    public Packet spawnPlayerMapObject(Client target, Character chr, boolean enteringField) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_PLAYER);
        p.writeInt(chr.getId());
        p.writeByte(chr.getLevel());
        p.writeString(chr.getName());
        if (chr.getGuildId() < 1) {
            p.writeString("");
            p.writeBytes(new byte[6]);
        } else {
            GuildSummary gs = chr.getClient().getWorldServer().getGuildSummary(chr.getGuildId(), chr.getWorld());
            if (gs != null) {
                p.writeString(gs.getName());
                p.writeShort(gs.getLogoBG());
                p.writeByte(gs.getLogoBGColor());
                p.writeShort(gs.getLogo());
                p.writeByte(gs.getLogoColor());
            } else {
                p.writeString("");
                p.writeBytes(new byte[6]);
            }
        }

        writeForeignBuffs(p, chr);

        p.writeShort(chr.getJob().getId());

                /* replace "p.writeShort(chr.getJob().getId())" with this snippet for 3rd person FJ animation on all classes
                if (chr.getJob().isA(Job.HERMIT) || chr.getJob().isA(Job.DAWNWARRIOR2) || chr.getJob().isA(Job.NIGHTWALKER2)) {
			p.writeShort(chr.getJob().getId());
                } else {
			p.writeShort(412);
                }*/

        addCharLook(p, chr, false);
        p.writeInt(chr.getInventory(InventoryType.CASH).countById(ItemId.HEART_SHAPED_CHOCOLATE));
        p.writeInt(chr.getItemEffect());
        p.writeInt(ItemConstants.getInventoryType(chr.getChair()) == InventoryType.SETUP ? chr.getChair() : 0);

        if (enteringField) {
            Point spawnPos = new Point(chr.getPosition());
            spawnPos.y -= 42;
            p.writePos(spawnPos);
            p.writeByte(6);
        } else {
            p.writePos(chr.getPosition());
            p.writeByte(chr.getStance());
        }

        p.writeShort(0);
        p.writeByte(0);
        Pet[] pet = chr.getPets();
        for (int i = 0; i < 3; i++) {
            if (pet[i] != null) {
                addPetInfo(p, pet[i], false);
            }
        }
        p.writeByte(0);
        if (chr.getMount() == null) {
            p.writeInt(1);
            p.writeLong(0);
        } else {
            p.writeInt(chr.getMount().getLevel());
            p.writeInt(chr.getMount().getExp());
            p.writeInt(chr.getMount().getTiredness());
        }

        PlayerShop mps = chr.getPlayerShop();
        if (mps != null && mps.isOwner(chr)) {
            if (mps.hasFreeSlot()) {
                addAnnounceBox(p, mps, mps.getVisitors().length);
            } else {
                addAnnounceBox(p, mps, 1);
            }
        } else {
            MiniGame miniGame = chr.getMiniGame();
            if (miniGame != null && miniGame.isOwner(chr)) {
                if (miniGame.hasFreeSlot()) {
                    addAnnounceBox(p, miniGame, 1, 0);
                } else {
                    addAnnounceBox(p, miniGame, 2, miniGame.isMatchInProgress() ? 1 : 0);
                }
            } else {
                p.writeByte(0);
            }
        }

        if (chr.getChalkboard() != null) {
            p.writeByte(1);
            p.writeString(chr.getChalkboard());
        } else {
            p.writeByte(0);
        }
        addRingLook(p, chr, true);
        addRingLook(p, chr, false);
        addMarriageRingLook(target, p, chr);
        encodeNewYearCardInfo(p, chr);
        p.writeByte(0);
        p.writeByte(0);
        p.writeByte(chr.getTeam());
        return p;
    }

    private void encodeNewYearCardInfo(OutPacket p, Character chr) {
        Set<NewYearCardRecord> newyears = chr.getReceivedNewYearRecords();
        if (!newyears.isEmpty()) {
            p.writeByte(1);

            p.writeInt(newyears.size());
            for (NewYearCardRecord nyc : newyears) {
                p.writeInt(nyc.getId());
            }
        } else {
            p.writeByte(0);
        }
    }

    public Packet onNewYearCardRes(Character user, int cardId, int mode, int msg) {
        NewYearCardRecord newyear = user.getNewYearRecord(cardId);
        return onNewYearCardRes(user, newyear, mode, msg);
    }

    public Packet onNewYearCardRes(Character user, NewYearCardRecord newyear, int mode, int msg) {
        OutPacket p = OutPacket.create(SendOpcode.NEW_YEAR_CARD_RES);
        p.writeByte(mode);
        switch (mode) {
            case 4:
            case 6:
                encodeNewYearCard(newyear, p);
                break;

            case 8:
                p.writeInt(newyear.getId());
                break;

            case 5:
            case 7:
            case 9:
            case 0xB:


                p.writeByte(msg);
                break;

            case 0xA:
                int nSN = 1;
                p.writeInt(nSN);
                if ((nSN - 1) <= 98 && nSN > 0) {
                    for (int i = 0; i < nSN; i++) {
                        p.writeInt(newyear.getId());
                        p.writeInt(newyear.getSenderId());
                        p.writeString(newyear.getSenderName());
                    }
                }
                break;

            case 0xC:
                p.writeInt(newyear.getId());
                p.writeString(newyear.getSenderName());
                break;

            case 0xD:
                p.writeInt(newyear.getId());
                p.writeInt(user.getId());
                break;

            case 0xE:
                p.writeInt(newyear.getId());
                break;
        }
        return p;
    }

    private void encodeNewYearCard(NewYearCardRecord newyear, OutPacket p) {
        p.writeInt(newyear.getId());
        p.writeInt(newyear.getSenderId());
        p.writeString(newyear.getSenderName());
        p.writeBool(newyear.isSenderCardDiscarded());
        p.writeLong(newyear.getDateSent());
        p.writeInt(newyear.getReceiverId());
        p.writeString(newyear.getReceiverName());
        p.writeBool(newyear.isReceiverCardDiscarded());
        p.writeBool(newyear.isReceiverCardReceived());
        p.writeLong(newyear.getDateReceived());
        p.writeString(newyear.getMessage());
    }

    private void addRingLook(final OutPacket p, Character chr, boolean crush) {
        List<Ring> rings;
        if (crush) {
            rings = chr.getCrushRings();
        } else {
            rings = chr.getFriendshipRings();
        }
        boolean yes = false;
        for (Ring ring : rings) {
            if (ring.equipped()) {
                if (!yes) {
                    yes = true;
                    p.writeByte(1);
                }
                p.writeInt(ring.getRingId());
                p.writeInt(0);
                p.writeInt(ring.getPartnerRingId());
                p.writeInt(0);
                p.writeInt(ring.getItemId());
            }
        }
        if (!yes) {
            p.writeByte(0);
        }
    }

    private void addMarriageRingLook(Client target, final OutPacket p, Character chr) {
        p.writeByte(0);
    }

    private void addAnnounceBox(final OutPacket p, PlayerShop shop, int availability) {
        p.writeByte(4);
        p.writeInt(shop.getObjectId());
        p.writeString(shop.getDescription());
        p.writeByte(0);
        p.writeByte(0);
        p.writeByte(1);
        p.writeByte(availability);
        p.writeByte(0);
    }

    private void addAnnounceBox(final OutPacket p, MiniGame game, int ammount, int joinable) {
        p.writeByte(game.getGameType().getValue());
        p.writeInt(game.getObjectId());
        p.writeString(game.getDescription());
        p.writeBool(!game.getPassword().isEmpty());
        p.writeByte(game.getPieceType());
        p.writeByte(ammount);
        p.writeByte(2);
        p.writeByte(joinable);
    }

    private void updateHiredMerchantBoxInfo(OutPacket p, HiredMerchant hm) {
        byte[] roomInfo = hm.getShopRoomInfo();

        p.writeByte(5);
        p.writeInt(hm.getObjectId());
        p.writeString(hm.getDescription());
        p.writeByte(hm.getItemId() % 100);
        p.writeBytes(roomInfo);
    }

    public Packet updateHiredMerchantBox(HiredMerchant hm) {
        final OutPacket p = OutPacket.create(SendOpcode.UPDATE_HIRED_MERCHANT);
        p.writeInt(hm.getOwnerId());
        updateHiredMerchantBoxInfo(p, hm);
        return p;
    }

    private void updatePlayerShopBoxInfo(OutPacket p, PlayerShop shop) {
        byte[] roomInfo = shop.getShopRoomInfo();

        p.writeByte(4);
        p.writeInt(shop.getObjectId());
        p.writeString(shop.getDescription());
        p.writeByte(0);
        p.writeByte(shop.getItemId() % 100);
        p.writeByte(roomInfo[0]);
        p.writeByte(roomInfo[1]);
        p.writeByte(0);
    }

    public Packet updatePlayerShopBox(PlayerShop shop) {
        final OutPacket p = OutPacket.create(SendOpcode.UPDATE_CHAR_BOX);
        p.writeInt(shop.getOwner().getId());
        updatePlayerShopBoxInfo(p, shop);
        return p;
    }

    public Packet removePlayerShopBox(PlayerShop shop) {
        OutPacket p = OutPacket.create(SendOpcode.UPDATE_CHAR_BOX);
        p.writeInt(shop.getOwner().getId());
        p.writeByte(0);
        return p;
    }

    public Packet facialExpression(Character from, int expression) {
        OutPacket p = OutPacket.create(SendOpcode.FACIAL_EXPRESSION);
        p.writeInt(from.getId());
        p.writeInt(expression);
        return p;
    }

    private void rebroadcastMovementList(OutPacket op, InPacket ip, long movementDataLength) {


        for (long i = 0; i < movementDataLength; i++) {
            op.writeByte(ip.readByte());
        }
    }

    private void serializeMovementList(OutPacket p, List<LifeMovementFragment> moves) {
        p.writeByte(moves.size());
        for (LifeMovementFragment move : moves) {
            move.serialize(p);
        }
    }

    public Packet movePlayer(int chrId, InPacket movementPacket, long movementDataLength) {
        OutPacket p = OutPacket.create(SendOpcode.MOVE_PLAYER);
        p.writeInt(chrId);
        p.writeInt(0);
        rebroadcastMovementList(p, movementPacket, movementDataLength);
        return p;
    }

    public Packet moveSummon(int cid, int oid, Point startPos, InPacket movementPacket, long movementDataLength) {
        final OutPacket p = OutPacket.create(SendOpcode.MOVE_SUMMON);
        p.writeInt(cid);
        p.writeInt(oid);
        p.writePos(startPos);
        rebroadcastMovementList(p, movementPacket, movementDataLength);
        return p;
    }

    public Packet moveMonster(int oid, boolean skillPossible, int skill, int skillId, int skillLevel, int pOption,
                              Point startPos, InPacket movementPacket, long movementDataLength) {
        final OutPacket p = OutPacket.create(SendOpcode.MOVE_MONSTER);
        p.writeInt(oid);
        p.writeByte(0);
        p.writeBool(skillPossible);
        p.writeByte(skill);
        p.writeByte(skillId);
        p.writeByte(skillLevel);
        p.writeShort(pOption);
        p.writePos(startPos);
        rebroadcastMovementList(p, movementPacket, movementDataLength);
        return p;
    }

    public Packet summonAttack(int cid, int summonOid, byte direction, List<SummonAttackTarget> targets) {
        OutPacket p = OutPacket.create(SendOpcode.SUMMON_ATTACK);

        p.writeInt(cid);
        p.writeInt(summonOid);
        p.writeByte(0);
        p.writeByte(direction);
        p.writeByte(targets.size());
        for (SummonAttackTarget target : targets) {
            p.writeInt(target.monsterOid());
            p.writeByte(6);
            p.writeInt(target.damage());
        }

        return p;
    }

    public Packet closeRangeAttack(Character chr, int skill, int skilllevel, int stance,
                                   int numAttackedAndDamage, Map<Integer, AttackTarget> targets, int speed,
                                   int direction, int display) {
        final OutPacket p = OutPacket.create(SendOpcode.CLOSE_RANGE_ATTACK);
        addAttackBody(p, chr, skill, skilllevel, stance, numAttackedAndDamage, 0, targets, speed, direction,
                display);
        return p;
    }

    public Packet rangedAttack(Character chr, int skill, int skilllevel, int stance, int numAttackedAndDamage,
                               int projectile, Map<Integer, AttackTarget> targets, int speed, int direction,
                               int display) {
        final OutPacket p = OutPacket.create(SendOpcode.RANGED_ATTACK);
        addAttackBody(p, chr, skill, skilllevel, stance, numAttackedAndDamage, projectile, targets, speed, direction,
                display);
        p.writeInt(0);
        return p;
    }

    public Packet magicAttack(Character chr, int skill, int skilllevel, int stance, int numAttackedAndDamage,
                              Map<Integer, AttackTarget> targets, int charge, int speed, int direction,
                              int display) {
        final OutPacket p = OutPacket.create(SendOpcode.MAGIC_ATTACK);
        addAttackBody(p, chr, skill, skilllevel, stance, numAttackedAndDamage, 0, targets, speed, direction,
                display);
        if (charge != -1) {
            p.writeInt(charge);
        }
        return p;
    }

    private void addAttackBody(OutPacket p, Character chr, int skill, int skilllevel, int stance,
                               int numAttackedAndDamage, int projectile, Map<Integer, AttackTarget> targets,
                               int speed, int direction, int display) {
        p.writeInt(chr.getId());
        p.writeByte(numAttackedAndDamage);
        p.writeByte(0x5B);
        p.writeByte(skilllevel);
        if (skilllevel > 0) {
            p.writeInt(skill);
        }
        p.writeByte(display);
        p.writeByte(direction);
        p.writeByte(stance);
        p.writeByte(speed);
        p.writeByte(0x0A);
        p.writeInt(projectile);
        for (Entry<Integer, AttackTarget> target : targets.entrySet()) {
            AttackTarget value = target.getValue();
            if (value != null) {
                p.writeInt(target.getKey());
                p.writeByte(0x0);
                if (skill == ChiefBandit.MESO_EXPLOSION) {
                    p.writeByte(value.damageLines().size());
                }
                for (Integer damageLine : value.damageLines()) {
                    p.writeInt(damageLine);
                }
            }
        }
    }

    public Packet throwGrenade(int cid, Point pos, int keyDown, int skillId, int skillLevel) {
        OutPacket p = OutPacket.create(SendOpcode.THROW_GRENADE);
        p.writeInt(cid);
        p.writeInt(pos.x);
        p.writeInt(pos.y);
        p.writeInt(keyDown);
        p.writeInt(skillId);
        p.writeInt(skillLevel);
        return p;
    }

    private int doubleToShortBits(double d) {
        return (int) (Double.doubleToLongBits(d) >> 48);
    }

    public Packet getNPCShop(Client c, int sid, List<ShopItem> items) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        final OutPacket p = OutPacket.create(SendOpcode.OPEN_NPC_SHOP);
        p.writeInt(sid);
        p.writeShort(items.size());
        for (ShopItem item : items) {
            p.writeInt(item.getItemId());
            p.writeInt(item.getPrice());
            p.writeInt(item.getPrice() == 0 ? item.getPitch() : 0);
            p.writeInt(0);
            p.writeInt(0);
            if (!ItemConstants.isRechargeable(item.getItemId())) {
                p.writeShort(1);
                p.writeShort(item.getBuyable());
            } else {
                p.writeShort(0);
                p.writeInt(0);
                p.writeShort(doubleToShortBits(ii.getUnitPrice(item.getItemId())));
                p.writeShort(ii.getSlotMax(c, item.getItemId()));
            }
        }
        return p;
    }

    public Packet shopTransaction(byte code) {
        OutPacket p = OutPacket.create(SendOpcode.CONFIRM_SHOP_TRANSACTION);
        p.writeByte(code);
        return p;
    }

    public Packet updateInventorySlotLimit(int type, int newLimit) {
        final OutPacket p = OutPacket.create(SendOpcode.INVENTORY_GROW);
        p.writeByte(type);
        p.writeByte(newLimit);
        return p;
    }

    public Packet modifyInventory(boolean updateTick, final List<ModifyInventory> mods) {
        OutPacket p = OutPacket.create(SendOpcode.INVENTORY_OPERATION);
        p.writeBool(updateTick);
        p.writeByte(mods.size());

        int addMovement = -1;
        for (ModifyInventory mod : mods) {
            p.writeByte(mod.getMode());
            p.writeByte(mod.getInventoryType());
            p.writeShort(mod.getMode() == 2 ? mod.getOldPosition() : mod.getPosition());
            switch (mod.getMode()) {
                case 0: {
                    addItemInfo(p, mod.getItem(), true);
                    break;
                }
                case 1: {
                    p.writeShort(mod.getQuantity());
                    break;
                }
                case 2: {
                    p.writeShort(mod.getPosition());
                    if (mod.getPosition() < 0 || mod.getOldPosition() < 0) {
                        addMovement = mod.getOldPosition() < 0 ? 1 : 2;
                    }
                    break;
                }
                case 3: {
                    if (mod.getPosition() < 0) {
                        addMovement = 2;
                    }
                    break;
                }
            }
            mod.clear();
        }
        if (addMovement > -1) {
            p.writeByte(addMovement);
        }
        return p;
    }

    public Packet getScrollEffect(int chr, ScrollResult scrollSuccess, boolean legendarySpirit, boolean whiteScroll) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_SCROLL_EFFECT);
        p.writeInt(chr);
        p.writeBool(scrollSuccess == ScrollResult.SUCCESS);
        p.writeBool(scrollSuccess == ScrollResult.CURSE);
        p.writeBool(legendarySpirit);
        p.writeBool(whiteScroll);
        return p;
    }

    public Packet removePlayerFromMap(int chrId) {
        OutPacket p = OutPacket.create(SendOpcode.REMOVE_PLAYER_FROM_MAP);
        p.writeInt(chrId);
        return p;
    }

    public Packet catchMessage(int message) {
        final OutPacket p = OutPacket.create(SendOpcode.BRIDLE_MOB_CATCH_FAIL);
        p.writeByte(message);
        p.writeInt(0);
        p.writeInt(0);
        return p;
    }

    public Packet showAllCharacter(int totalWorlds, int totalChrs) {
        OutPacket p = OutPacket.create(SendOpcode.VIEW_ALL_CHAR);
        p.writeByte(totalChrs > 0 ? 1 : 5);
        p.writeInt(totalWorlds);
        p.writeInt(totalChrs);
        return p;
    }

    public Packet showAriantScoreBoard() {
        return OutPacket.create(SendOpcode.ARIANT_ARENA_SHOW_RESULT);
    }

    public Packet updateAriantPQRanking(Map<Character, Integer> playerScore) {
        OutPacket p = OutPacket.create(SendOpcode.ARIANT_ARENA_USER_SCORE);
        p.writeByte(playerScore.size());
        for (Entry<Character, Integer> e : playerScore.entrySet()) {
            p.writeString(e.getKey().getName());
            p.writeInt(e.getValue());
        }
        return p;
    }

    public Packet silentRemoveItemFromMap(int objId) {
        return removeItemFromMap(objId, 1, 0);
    }

    public Packet removeItemFromMap(int objId, int animation, int chrId) {
        return removeItemFromMap(objId, animation, chrId, false, 0);
    }

    public Packet removeItemFromMap(int objId, int animation, int chrId, boolean pet, int slot) {
        OutPacket p = OutPacket.create(SendOpcode.REMOVE_ITEM_FROM_MAP);
        p.writeByte(animation);
        p.writeInt(objId);
        if (animation >= 2) {
            p.writeInt(chrId);
            if (pet) {
                p.writeByte(slot);
            }
        }
        return p;
    }

    public Packet removeExplodedMesoFromMap(int mapObjectId, short delay) {
        OutPacket p = OutPacket.create(SendOpcode.REMOVE_ITEM_FROM_MAP);
        p.writeByte(4);
        p.writeInt(mapObjectId);
        p.writeShort(delay);
        return p;
    }

    public Packet updateCharLook(Client target, Character chr) {
        OutPacket p = OutPacket.create(SendOpcode.UPDATE_CHAR_LOOK);
        p.writeInt(chr.getId());
        p.writeByte(1);
        addCharLook(p, chr, false);
        addRingLook(p, chr, true);
        addRingLook(p, chr, false);
        addMarriageRingLook(target, p, chr);
        p.writeInt(0);
        return p;
    }

    public Packet damagePlayer(int skill, int monsteridfrom, int cid, int damage, int fake, int direction, boolean pgmr, int pgmr_1, boolean is_pg, int oid, int pos_x, int pos_y) {
        final OutPacket p = OutPacket.create(SendOpcode.DAMAGE_PLAYER);
        p.writeInt(cid);
        p.writeByte(skill);
        if (skill == -3) {
            p.writeInt(0);
        }
        p.writeInt(damage);
        if (skill != -4) {
            p.writeInt(monsteridfrom);
            p.writeByte(direction);
            if (pgmr) {
                p.writeByte(pgmr_1);
                p.writeByte(is_pg ? 1 : 0);
                p.writeInt(oid);
                p.writeByte(6);
                p.writeShort(pos_x);
                p.writeShort(pos_y);
                p.writeByte(0);
            } else {
                p.writeShort(0);
            }
            p.writeInt(damage);
            if (fake > 0) {
                p.writeInt(fake);
            }
        } else {
            p.writeInt(damage);
        }

        return p;
    }

    public Packet sendMapleLifeCharacterInfo() {
        final OutPacket p = OutPacket.create(SendOpcode.MAPLELIFE_RESULT);
        p.writeInt(0);
        return p;
    }

    public Packet sendMapleLifeNameError() {
        OutPacket p = OutPacket.create(SendOpcode.MAPLELIFE_RESULT);
        p.writeInt(2);
        p.writeInt(3);
        p.writeByte(0);
        return p;
    }

    public Packet sendMapleLifeError(int code) {
        OutPacket p = OutPacket.create(SendOpcode.MAPLELIFE_ERROR);
        p.writeByte(0);
        p.writeInt(code);
        return p;
    }

    public Packet charNameResponse(String charname, boolean nameUsed) {
        final OutPacket p = OutPacket.create(SendOpcode.CHAR_NAME_RESPONSE);
        p.writeString(charname);
        p.writeByte(nameUsed ? 1 : 0);
        return p;
    }

    public Packet addNewCharEntry(Character chr) {
        final OutPacket p = OutPacket.create(SendOpcode.ADD_NEW_CHAR_ENTRY);
        p.writeByte(0);
        addCharEntry(p, chr, false);
        return p;
    }

    public Packet deleteCharResponse(int cid, int state) {
        final OutPacket p = OutPacket.create(SendOpcode.DELETE_CHAR_RESPONSE);
        p.writeInt(cid);
        p.writeByte(state);
        return p;
    }

    public Packet selectWorld(int world) {
        final OutPacket p = OutPacket.create(SendOpcode.LAST_CONNECTED_WORLD);
        p.writeInt(world);
        return p;
    }

    public Packet sendRecommended(List<Pair<Integer, String>> worlds) {
        final OutPacket p = OutPacket.create(SendOpcode.RECOMMENDED_WORLD_MESSAGE);
        p.writeByte(worlds.size());
        for (Pair<Integer, String> world : worlds) {
            p.writeInt(world.getLeft());
            p.writeString(world.getRight());
        }
        return p;
    }

    public Packet charInfo(Character chr) {

        final OutPacket p = OutPacket.create(SendOpcode.CHAR_INFO);
        p.writeInt(chr.getId());
        p.writeByte(chr.getLevel());
        p.writeShort(chr.getJob().getId());
        p.writeShort(chr.getFame());
        p.writeByte(0);
        String guildName = "";
        String allianceName = "";
        if (chr.getGuildId() > 0) {
            Guild mg = Server.getInstance().getGuild(chr.getGuildId());
            guildName = mg.getName();

            Alliance alliance = Server.getInstance().getAlliance(chr.getGuild().getAllianceId());
            if (alliance != null) {
                allianceName = alliance.getName();
            }
        }
        p.writeString(guildName);
        p.writeString(allianceName);
        p.writeByte(0);

        Pet[] pets = chr.getPets();
        Item inv = chr.getInventory(InventoryType.EQUIPPED).getItem((short) -114);
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                p.writeByte(pets[i].getUniqueId());
                p.writeInt(pets[i].getItemId());
                p.writeString(pets[i].getName());
                p.writeByte(pets[i].getLevel());
                p.writeShort(pets[i].getTameness());
                p.writeByte(pets[i].getFullness());
                p.writeShort(0);
                p.writeInt(inv != null ? inv.getItemId() : 0);
            }
        }
        p.writeByte(0);

        Item mount;
        if (chr.getMount() != null && (mount = chr.getInventory(InventoryType.EQUIPPED).getItem((short) -18)) != null && ItemInformationProvider.getInstance().getEquipLevelReq(mount.getItemId()) <= chr.getLevel()) {
            Mount mmount = chr.getMount();
            p.writeByte(mmount.getId());
            p.writeInt(mmount.getLevel());
            p.writeInt(mmount.getExp());
            p.writeInt(mmount.getTiredness());
        } else {
            p.writeByte(0);
        }
        p.writeByte(chr.getCashShop().getWishList().size());
        for (int sn : chr.getCashShop().getWishList()) {
            p.writeInt(sn);
        }

        MonsterBook book = chr.getMonsterBook();
        p.writeInt(book.getBookLevel());
        p.writeInt(book.getNormalCard());
        p.writeInt(book.getSpecialCard());
        p.writeInt(book.getTotalCards());
        p.writeInt(chr.getMonsterBookCover() > 0 ? ItemInformationProvider.getInstance().getCardMobId(chr.getMonsterBookCover()) : 0);
        Item medal = chr.getInventory(InventoryType.EQUIPPED).getItem((short) -49);
        if (medal != null) {
            p.writeInt(medal.getItemId());
        } else {
            p.writeInt(0);
        }
        ArrayList<Short> medalQuests = new ArrayList<>();
        List<QuestStatus> completed = chr.getCompletedQuests();
        for (QuestStatus qs : completed) {
            if (qs.getQuest().getId() >= 29000) {
                medalQuests.add(qs.getQuest().getId());
            }
        }

        Collections.sort(medalQuests);
        p.writeShort(medalQuests.size());
        for (Short s : medalQuests) {
            p.writeShort(s);
        }
        return p;
    }

    public Packet giveBuff(int buffid, int bufflength, List<Pair<BuffStat, Integer>> statups) {
        final OutPacket p = OutPacket.create(SendOpcode.GIVE_BUFF);
        boolean special = false;
        writeLongMask(p, statups);
        for (Pair<BuffStat, Integer> statup : statups) {
            if (statup.getLeft().equals(BuffStat.MONSTER_RIDING) || statup.getLeft().equals(BuffStat.HOMING_BEACON)) {
                special = true;
            }
            p.writeShort(statup.getRight().shortValue());
            p.writeInt(buffid);
            p.writeInt(bufflength);
        }
        p.writeInt(0);
        p.writeByte(0);
        p.writeInt(statups.get(0).getRight());

        if (special) {
            p.skip(3);
        }
        return p;
    }

    public Packet showMonsterRiding(int cid, Mount mount) {
        final OutPacket p = OutPacket.create(SendOpcode.GIVE_FOREIGN_BUFF);
        p.writeInt(cid);
        p.writeLong(BuffStat.MONSTER_RIDING.getValue());
        p.writeLong(0);
        p.writeShort(0);
        p.writeInt(mount.getItemId());
        p.writeInt(mount.getSkillId());
        p.writeInt(0);
        p.writeShort(0);
        p.writeByte(0);
        return p;
    }

    public Packet forfeitQuest(short quest) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_STATUS_INFO);
        p.writeByte(1);
        p.writeShort(quest);
        p.writeByte(0);
        return p;
    }

    public Packet completeQuest(short quest, long time) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_STATUS_INFO);
        p.writeByte(1);
        p.writeShort(quest);
        p.writeByte(2);
        p.writeLong(getTime(time));
        return p;
    }

    public Packet updateQuestInfo(short quest, int npc) {
        final OutPacket p = OutPacket.create(SendOpcode.UPDATE_QUEST_INFO);
        p.writeByte(8);
        p.writeShort(quest);
        p.writeInt(npc);
        p.writeInt(0);
        return p;
    }

    public Packet addQuestTimeLimit(final short quest, final int time) {
        final OutPacket p = OutPacket.create(SendOpcode.UPDATE_QUEST_INFO);
        p.writeByte(6);
        p.writeShort(1);
        p.writeShort(quest);
        p.writeInt(time);
        return p;
    }

    public Packet removeQuestTimeLimit(final short quest) {
        final OutPacket p = OutPacket.create(SendOpcode.UPDATE_QUEST_INFO);
        p.writeByte(7);
        p.writeShort(1);
        p.writeShort(quest);
        return p;
    }

    public Packet updateQuest(Character chr, QuestStatus qs, boolean infoUpdate) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_STATUS_INFO);
        p.writeByte(1);
        if (infoUpdate) {
            QuestStatus iqs = chr.getQuest(qs.getInfoNumber());
            p.writeShort(iqs.getQuestID());
            p.writeByte(1);
            p.writeString(iqs.getProgressData());
        } else {
            p.writeShort(qs.getQuest().getId());
            p.writeByte(qs.getStatus().getId());
            p.writeString(qs.getProgressData());
        }
        p.skip(5);
        return p;
    }

    private void writeLongMaskD(final OutPacket p, List<Pair<Disease, Integer>> statups) {
        long firstmask = 0;
        long secondmask = 0;
        for (Pair<Disease, Integer> statup : statups) {
            if (statup.getLeft().isFirst()) {
                firstmask |= statup.getLeft().getValue();
            } else {
                secondmask |= statup.getLeft().getValue();
            }
        }
        p.writeLong(firstmask);
        p.writeLong(secondmask);
    }

    public Packet giveDebuff(List<Pair<Disease, Integer>> statups, MobSkill skill) {
        final OutPacket p = OutPacket.create(SendOpcode.GIVE_BUFF);
        writeLongMaskD(p, statups);
        for (Pair<Disease, Integer> statup : statups) {
            p.writeShort(statup.getRight().shortValue());
            writeMobSkillId(p, skill.getId());
            p.writeInt((int) skill.getDuration());
        }
        p.writeShort(0);
        p.writeShort(900);
        p.writeByte(1);
        return p;
    }

    public Packet giveForeignDebuff(int chrId, List<Pair<Disease, Integer>> statups, MobSkill skill) {

        OutPacket p = OutPacket.create(SendOpcode.GIVE_FOREIGN_BUFF);
        p.writeInt(chrId);
        writeLongMaskD(p, statups);
        for (Pair<Disease, Integer> statup : statups) {
            if (statup.getLeft() == Disease.POISON) {
                p.writeShort(statup.getRight().shortValue());
            }
            writeMobSkillId(p, skill.getId());
        }
        p.writeShort(0);
        p.writeShort(900);
        return p;
    }

    public Packet cancelForeignFirstDebuff(int cid, long mask) {
        final OutPacket p = OutPacket.create(SendOpcode.CANCEL_FOREIGN_BUFF);
        p.writeInt(cid);
        p.writeLong(mask);
        p.writeLong(0);
        return p;
    }

    public Packet cancelForeignDebuff(int cid, long mask) {
        final OutPacket p = OutPacket.create(SendOpcode.CANCEL_FOREIGN_BUFF);
        p.writeInt(cid);
        p.writeLong(0);
        p.writeLong(mask);
        return p;
    }

    public Packet giveForeignBuff(int chrId, List<Pair<BuffStat, Integer>> statups) {
        OutPacket p = OutPacket.create(SendOpcode.GIVE_FOREIGN_BUFF);
        p.writeInt(chrId);
        writeLongMask(p, statups);
        for (Pair<BuffStat, Integer> statup : statups) {
            p.writeShort(statup.getRight().shortValue());
        }
        p.writeInt(0);
        p.writeShort(0);
        return p;
    }

    public Packet cancelForeignBuff(int chrId, List<BuffStat> statups) {
        OutPacket p = OutPacket.create(SendOpcode.CANCEL_FOREIGN_BUFF);
        p.writeInt(chrId);
        writeLongMaskFromList(p, statups);
        return p;
    }

    public Packet cancelBuff(List<BuffStat> statups) {
        OutPacket p = OutPacket.create(SendOpcode.CANCEL_BUFF);
        writeLongMaskFromList(p, statups);
        p.writeByte(1);
        return p;
    }

    private void writeLongMask(final OutPacket p, List<Pair<BuffStat, Integer>> statups) {
        long firstmask = 0;
        long secondmask = 0;
        for (Pair<BuffStat, Integer> statup : statups) {
            if (statup.getLeft().isFirst()) {
                firstmask |= statup.getLeft().getValue();
            } else {
                secondmask |= statup.getLeft().getValue();
            }
        }
        p.writeLong(firstmask);
        p.writeLong(secondmask);
    }

    private void writeLongMaskFromList(OutPacket p, List<BuffStat> statups) {
        long firstmask = 0;
        long secondmask = 0;
        for (BuffStat statup : statups) {
            if (statup.isFirst()) {
                firstmask |= statup.getValue();
            } else {
                secondmask |= statup.getValue();
            }
        }
        p.writeLong(firstmask);
        p.writeLong(secondmask);
    }

    private void writeLongEncodeTemporaryMask(final OutPacket p, Collection<MonsterStatus> stati) {
        int[] masks = new int[4];

        for (MonsterStatus statup : stati) {
            int pos = statup.isFirst() ? 0 : 2;
            for (int i = 0; i < 2; i++) {
                masks[pos + i] |= statup.getValue() >> 32 * i;
            }
        }

        for (int mask : masks) {
            p.writeInt(mask);
        }
    }

    public Packet cancelDebuff(long mask) {
        OutPacket p = OutPacket.create(SendOpcode.CANCEL_BUFF);
        p.writeLong(0);
        p.writeLong(mask);
        p.writeByte(0);
        return p;
    }

    private void writeLongMaskSlowD(final OutPacket p) {
        p.writeInt(0);
        p.writeInt(2048);
        p.writeLong(0);
    }

    public Packet giveForeignSlowDebuff(int chrId, List<Pair<Disease, Integer>> statups, MobSkill skill) {
        OutPacket p = OutPacket.create(SendOpcode.GIVE_FOREIGN_BUFF);
        p.writeInt(chrId);
        writeLongMaskSlowD(p);
        for (Pair<Disease, Integer> statup : statups) {
            if (statup.getLeft() == Disease.POISON) {
                p.writeShort(statup.getRight().shortValue());
            }
            writeMobSkillId(p, skill.getId());
        }
        p.writeShort(0);
        p.writeShort(900);
        return p;
    }

    public Packet cancelForeignSlowDebuff(int chrId) {
        final OutPacket p = OutPacket.create(SendOpcode.CANCEL_FOREIGN_BUFF);
        p.writeInt(chrId);
        writeLongMaskSlowD(p);
        return p;
    }

    private void writeLongMaskChair(OutPacket p) {
        p.writeInt(0);
        p.writeInt(262144);
        p.writeLong(0);
    }

    public Packet giveForeignChairSkillEffect(int cid) {
        final OutPacket p = OutPacket.create(SendOpcode.GIVE_FOREIGN_BUFF);
        p.writeInt(cid);
        writeLongMaskChair(p);

        p.writeShort(0);
        p.writeShort(0);
        p.writeShort(100);
        p.writeShort(1);

        p.writeShort(0);
        p.writeShort(900);

        p.skip(7);

        return p;
    }

    public Packet giveForeignWKChargeEffect(int cid, int buffid, List<Pair<BuffStat, Integer>> statups) {
        OutPacket p = OutPacket.create(SendOpcode.GIVE_FOREIGN_BUFF);
        p.writeInt(cid);
        writeLongMask(p, statups);
        p.writeInt(buffid);
        p.writeShort(600);
        p.writeShort(1000);
        p.writeByte(1);
        return p;
    }

    public Packet cancelForeignChairSkillEffect(int chrId) {
        OutPacket p = OutPacket.create(SendOpcode.CANCEL_FOREIGN_BUFF);
        p.writeInt(chrId);
        writeLongMaskChair(p);
        return p;
    }

    public Packet getPlayerShopChat(Character chr, String chat, boolean owner) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.CHAT.getCode());
        p.writeByte(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        p.writeBool(!owner);
        p.writeString(chr.getName() + " : " + chat);
        return p;
    }

    public Packet getPlayerShopNewVisitor(Character chr, int slot) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.VISIT.getCode());
        p.writeByte(slot);
        addCharLook(p, chr, false);
        p.writeString(chr.getName());
        return p;
    }

    public Packet getPlayerShopRemoveVisitor(int slot) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.EXIT.getCode());
        if (slot != 0) {
            p.writeShort(slot);
        }
        return p;
    }

    public Packet getTradePartnerAdd(Character chr) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.VISIT.getCode());
        p.writeByte(1);
        addCharLook(p, chr, false);
        p.writeString(chr.getName());
        return p;
    }

    public Packet tradeInvite(Character chr) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.INVITE.getCode());
        p.writeByte(3);
        p.writeString(chr.getName());
        p.writeBytes(new byte[]{(byte) 0xB7, (byte) 0x50, 0, 0});
        return p;
    }

    public Packet getTradeMesoSet(byte number, int meso) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.SET_MESO.getCode());
        p.writeByte(number);
        p.writeInt(meso);
        return p;
    }

    public Packet getTradeItemAdd(byte number, Item item) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.SET_ITEMS.getCode());
        p.writeByte(number);
        p.writeByte(item.getPosition());
        addItemInfo(p, item, true);
        return p;
    }

    public Packet getPlayerShopItemUpdate(PlayerShop shop) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
        p.writeByte(shop.getItems().size());
        for (PlayerShopItem item : shop.getItems()) {
            p.writeShort(item.getBundles());
            p.writeShort(item.getItem().getQuantity());
            p.writeInt(item.getPrice());
            addItemInfo(p, item.getItem(), true);
        }
        return p;
    }

    public Packet getPlayerShopOwnerUpdate(PlayerShop.SoldItem item, int position) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.UPDATE_PLAYERSHOP.getCode());
        p.writeByte(position);
        p.writeShort(item.getQuantity());
        p.writeString(item.getBuyer());

        return p;
    }

    public Packet getPlayerShop(PlayerShop shop, boolean owner) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.ROOM.getCode());
        p.writeByte(4);
        p.writeByte(4);
        p.writeByte(owner ? 0 : 1);

        if (owner) {
            List<PlayerShop.SoldItem> sold = shop.getSold();
            p.writeByte(sold.size());
            for (PlayerShop.SoldItem s : sold) {
                p.writeInt(s.getItemId());
                p.writeShort(s.getQuantity());
                p.writeInt(s.getMesos());
                p.writeString(s.getBuyer());
            }
        } else {
            p.writeByte(0);
        }

        addCharLook(p, shop.getOwner(), false);
        p.writeString(shop.getOwner().getName());

        Character[] visitors = shop.getVisitors();
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null) {
                p.writeByte(i + 1);
                addCharLook(p, visitors[i], false);
                p.writeString(visitors[i].getName());
            }
        }

        p.writeByte(0xFF);
        p.writeString(shop.getDescription());
        List<PlayerShopItem> items = shop.getItems();
        p.writeByte(0x10);
        p.writeByte(items.size());
        for (PlayerShopItem item : items) {
            p.writeShort(item.getBundles());
            p.writeShort(item.getItem().getQuantity());
            p.writeInt(item.getPrice());
            addItemInfo(p, item.getItem(), true);
        }
        return p;
    }

    public Packet getTradeStart(Client c, Trade trade, byte number) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.ROOM.getCode());
        p.writeByte(3);
        p.writeByte(2);
        p.writeByte(number);
        if (number == 1) {
            p.writeByte(0);
            addCharLook(p, trade.getPartner().getChr(), false);
            p.writeString(trade.getPartner().getChr().getName());
        }
        p.writeByte(number);
        addCharLook(p, c.getPlayer(), false);
        p.writeString(c.getPlayer().getName());
        p.writeByte(0xFF);
        return p;
    }

    public Packet getTradeConfirmation() {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.CONFIRM.getCode());
        return p;
    }

    public Packet getTradeResult(byte number, byte operation) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.EXIT.getCode());
        p.writeByte(number);
        p.writeByte(operation);
        return p;
    }

    public Packet getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte speaker) {
        final OutPacket p = OutPacket.create(SendOpcode.NPC_TALK);
        p.writeByte(4);
        p.writeInt(npc);
        p.writeByte(msgType);
        p.writeByte(speaker);
        p.writeString(talk);
        p.writeBytes(HexTool.toBytes(endBytes));
        return p;
    }

    public Packet getDimensionalMirror(String talk) {
        final OutPacket p = OutPacket.create(SendOpcode.NPC_TALK);
        p.writeByte(4);
        p.writeInt(NpcId.DIMENSIONAL_MIRROR);
        p.writeByte(0x0E);
        p.writeByte(0);
        p.writeInt(0);
        p.writeString(talk);
        return p;
    }

    public Packet getNPCTalkStyle(int npc, String talk, int[] styles) {
        final OutPacket p = OutPacket.create(SendOpcode.NPC_TALK);
        p.writeByte(4);
        p.writeInt(npc);
        p.writeByte(7);
        p.writeByte(0);
        p.writeString(talk);
        p.writeByte(styles.length);
        for (int style : styles) {
            p.writeInt(style);
        }
        return p;
    }

    public Packet getNPCTalkNum(int npc, String talk, int def, int min, int max) {
        final OutPacket p = OutPacket.create(SendOpcode.NPC_TALK);
        p.writeByte(4);
        p.writeInt(npc);
        p.writeByte(3);
        p.writeByte(0);
        p.writeString(talk);
        p.writeInt(def);
        p.writeInt(min);
        p.writeInt(max);
        p.writeInt(0);
        return p;
    }

    public Packet getNPCTalkText(int npc, String talk, String def) {
        final OutPacket p = OutPacket.create(SendOpcode.NPC_TALK);
        p.writeByte(4);
        p.writeInt(npc);
        p.writeByte(2);
        p.writeByte(0);
        p.writeString(talk);
        p.writeString(def);
        p.writeInt(0);
        return p;
    }

    public Packet showBuffEffect(int chrId, int skillId, int effectId) {
        return showBuffEffect(chrId, skillId, effectId, (byte) 3);
    }

    public Packet showBuffEffect(int chrId, int skillId, int effectId, byte direction) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_FOREIGN_EFFECT);
        p.writeInt(chrId);
        p.writeByte(effectId);
        p.writeInt(skillId);
        p.writeByte(direction);
        p.writeByte(1);
        p.writeLong(0);
        return p;
    }

    public Packet showBuffEffect(int chrId, int skillId, int skillLv, int effectId, byte direction) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_FOREIGN_EFFECT);
        p.writeInt(chrId);
        p.writeByte(effectId);
        p.writeInt(skillId);
        p.writeByte(0);
        p.writeByte(skillLv);
        p.writeByte(direction);
        return p;
    }

    public Packet showOwnBuffEffect(int skillId, int effectId) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_ITEM_GAIN_INCHAT);
        p.writeByte(effectId);
        p.writeInt(skillId);
        p.writeByte(0xA9);
        p.writeByte(1);
        return p;
    }

    public Packet showOwnBerserk(int skilllevel, boolean Berserk) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_ITEM_GAIN_INCHAT);
        p.writeByte(1);
        p.writeInt(1320006);
        p.writeByte(0xA9);
        p.writeByte(skilllevel);
        p.writeByte(Berserk ? 1 : 0);
        return p;
    }

    public Packet showBerserk(int chrId, int skillLv, boolean berserk) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_FOREIGN_EFFECT);
        p.writeInt(chrId);
        p.writeByte(1);
        p.writeInt(1320006);
        p.writeByte(0xA9);
        p.writeByte(skillLv);
        p.writeBool(berserk);
        return p;
    }

    public Packet updateSkill(int skillId, int level, int masterlevel, long expiration) {
        OutPacket p = OutPacket.create(SendOpcode.UPDATE_SKILLS);
        p.writeByte(1);
        p.writeShort(1);
        p.writeInt(skillId);
        p.writeInt(level);
        p.writeInt(masterlevel);
        addExpirationTime(p, expiration);
        p.writeByte(4);
        return p;
    }

    public Packet getShowQuestCompletion(int id) {
        final OutPacket p = OutPacket.create(SendOpcode.QUEST_CLEAR);
        p.writeShort(id);
        return p;
    }

    public Packet getKeymap(Map<Integer, KeyBinding> keybindings) {
        final OutPacket p = OutPacket.create(SendOpcode.KEYMAP);
        p.writeByte(0);
        for (int x = 0; x < 90; x++) {
            KeyBinding binding = keybindings.get(x);
            if (binding != null) {
                p.writeByte(binding.getType());
                p.writeInt(binding.getAction());
            } else {
                p.writeByte(0);
                p.writeInt(0);
            }
        }
        return p;
    }

    public Packet QuickslotMappedInit(QuickslotBinding pQuickslot) {
        OutPacket p = OutPacket.create(SendOpcode.QUICKSLOT_INIT);
        pQuickslot.encode(p);
        return p;
    }

    public Packet getInventoryFull() {
        return modifyInventory(true, Collections.emptyList());
    }

    public Packet getShowInventoryFull() {
        return getShowInventoryStatus(0xff);
    }

    public Packet showItemUnavailable() {
        return getShowInventoryStatus(0xfe);
    }

    public Packet getShowInventoryStatus(int mode) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_STATUS_INFO);
        p.writeByte(0);
        p.writeByte(mode);
        p.writeInt(0);
        p.writeInt(0);
        return p;
    }

    public Packet getStorage(int npcId, byte slots, Collection<Item> items, int meso) {
        final OutPacket p = OutPacket.create(SendOpcode.STORAGE);
        p.writeByte(0x16);
        p.writeInt(npcId);
        p.writeByte(slots);
        p.writeShort(0x7E);
        p.writeShort(0);
        p.writeInt(0);
        p.writeInt(meso);
        p.writeShort(0);
        p.writeByte((byte) items.size());
        for (Item item : items) {
            addItemInfo(p, item, true);
        }
        p.writeShort(0);
        p.writeByte(0);
        return p;
    }

    public Packet getStorageError(byte i) {
        final OutPacket p = OutPacket.create(SendOpcode.STORAGE);
        p.writeByte(i);
        return p;
    }

    public Packet mesoStorage(byte slots, int meso) {
        final OutPacket p = OutPacket.create(SendOpcode.STORAGE);
        p.writeByte(0x13);
        p.writeByte(slots);
        p.writeShort(2);
        p.writeShort(0);
        p.writeInt(0);
        p.writeInt(meso);
        return p;
    }

    public Packet storeStorage(byte slots, InventoryType type, Collection<Item> items) {
        final OutPacket p = OutPacket.create(SendOpcode.STORAGE);
        p.writeByte(0xD);
        p.writeByte(slots);
        p.writeShort(type.getBitfieldEncoding());
        p.writeShort(0);
        p.writeInt(0);
        p.writeByte(items.size());
        for (Item item : items) {
            addItemInfo(p, item, true);
        }
        return p;
    }

    public Packet takeOutStorage(byte slots, InventoryType type, Collection<Item> items) {
        final OutPacket p = OutPacket.create(SendOpcode.STORAGE);
        p.writeByte(0x9);
        p.writeByte(slots);
        p.writeShort(type.getBitfieldEncoding());
        p.writeShort(0);
        p.writeInt(0);
        p.writeByte(items.size());
        for (Item item : items) {
            addItemInfo(p, item, true);
        }
        return p;
    }

    public Packet arrangeStorage(byte slots, Collection<Item> items) {
        OutPacket p = OutPacket.create(SendOpcode.STORAGE);
        p.writeByte(0xF);
        p.writeByte(slots);
        p.writeByte(124);
        p.skip(10);
        p.writeByte(items.size());
        for (Item item : items) {
            addItemInfo(p, item, true);
        }
        p.writeByte(0);
        return p;
    }

    public Packet showMonsterHP(int oid, int remhppercentage) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_MONSTER_HP);
        p.writeInt(oid);
        p.writeByte(remhppercentage);
        return p;
    }

    public Packet showBossHP(int oid, int currHP, int maxHP, byte tagColor, byte tagBgColor) {
        final OutPacket p = OutPacket.create(SendOpcode.FIELD_EFFECT);
        p.writeByte(5);
        p.writeInt(oid);
        p.writeInt(currHP);
        p.writeInt(maxHP);
        p.writeByte(tagColor);
        p.writeByte(tagBgColor);
        return p;
    }

    private Pair<Integer, Integer> normalizedCustomMaxHP(long currHP, long maxHP) {
        int sendHP, sendMaxHP;

        if (maxHP <= Integer.MAX_VALUE) {
            sendHP = (int) currHP;
            sendMaxHP = (int) maxHP;
        } else {
            float f = ((float) currHP) / maxHP;

            sendHP = (int) (Integer.MAX_VALUE * f);
            sendMaxHP = Integer.MAX_VALUE;
        }

        return new Pair<>(sendHP, sendMaxHP);
    }

    public Packet giveFameResponse(int mode, String charname, int newfame) {
        final OutPacket p = OutPacket.create(SendOpcode.FAME_RESPONSE);
        p.writeByte(0);
        p.writeString(charname);
        p.writeByte(mode);
        p.writeShort(newfame);
        p.writeShort(0);
        return p;
    }

    public Packet giveFameErrorResponse(int status) {
        final OutPacket p = OutPacket.create(SendOpcode.FAME_RESPONSE);
        p.writeByte(status);
        return p;
    }

    public Packet receiveFame(int mode, String charnameFrom) {
        final OutPacket p = OutPacket.create(SendOpcode.FAME_RESPONSE);
        p.writeByte(5);
        p.writeString(charnameFrom);
        p.writeByte(mode);
        return p;
    }

    public Packet partyCreated(Party party, int partycharid) {
        final OutPacket p = OutPacket.create(SendOpcode.PARTY_OPERATION);
        p.writeByte(8);
        p.writeInt(party.getId());

        Map<Integer, Door> partyDoors = party.getDoors();
        if (partyDoors.size() > 0) {
            Door door = partyDoors.get(partycharid);

            if (door != null) {
                DoorObject mdo = door.getAreaDoor();
                p.writeInt(mdo.getTo().getId());
                p.writeInt(mdo.getFrom().getId());
                p.writeInt(mdo.getPosition().x);
                p.writeInt(mdo.getPosition().y);
            } else {
                p.writeInt(MapId.NONE);
                p.writeInt(MapId.NONE);
                p.writeInt(0);
                p.writeInt(0);
            }
        } else {
            p.writeInt(MapId.NONE);
            p.writeInt(MapId.NONE);
            p.writeInt(0);
            p.writeInt(0);
        }
        return p;
    }

    public Packet partyInvite(Character from) {
        final OutPacket p = OutPacket.create(SendOpcode.PARTY_OPERATION);
        p.writeByte(4);
        p.writeInt(from.getParty().getId());
        p.writeString(from.getName());
        p.writeByte(0);
        return p;
    }

    public Packet partySearchInvite(Character from) {
        final OutPacket p = OutPacket.create(SendOpcode.PARTY_OPERATION);
        p.writeByte(4);
        p.writeInt(from.getParty().getId());
        p.writeString("PS: " + from.getName());
        p.writeByte(0);
        return p;
    }

    public Packet partyStatusMessage(int message) {
        final OutPacket p = OutPacket.create(SendOpcode.PARTY_OPERATION);
        p.writeByte(message);
        return p;
    }

    public Packet partyStatusMessage(int message, String charname) {
        final OutPacket p = OutPacket.create(SendOpcode.PARTY_OPERATION);
        p.writeByte(message);
        p.writeString(charname);
        return p;
    }

    private void addPartyStatus(int forchannel, Party party, OutPacket p, boolean leaving) {
        List<PartyCharacter> partymembers = new ArrayList<>(party.getMembers());
        while (partymembers.size() < 6) {
            partymembers.add(new PartyCharacter());
        }
        for (PartyCharacter partychar : partymembers) {
            p.writeInt(partychar.getId());
        }
        for (PartyCharacter partychar : partymembers) {
            p.writeFixedString(getRightPaddedStr(partychar.getName(), '\0', 13));
        }
        for (PartyCharacter partychar : partymembers) {
            p.writeInt(partychar.getJobId());
        }
        for (PartyCharacter partychar : partymembers) {
            p.writeInt(partychar.getLevel());
        }
        for (PartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                p.writeInt(partychar.getChannel() - 1);
            } else {
                p.writeInt(-2);
            }
        }
        p.writeInt(party.getLeader().getId());
        for (PartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel) {
                p.writeInt(partychar.getMapId());
            } else {
                p.writeInt(0);
            }
        }

        Map<Integer, Door> partyDoors = party.getDoors();
        for (PartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel && !leaving) {
                if (partyDoors.size() > 0) {
                    Door door = partyDoors.get(partychar.getId());
                    if (door != null) {
                        DoorObject mdo = door.getTownDoor();
                        p.writeInt(mdo.getTown().getId());
                        p.writeInt(mdo.getArea().getId());
                        p.writeInt(mdo.getPosition().x);
                        p.writeInt(mdo.getPosition().y);
                    } else {
                        p.writeInt(MapId.NONE);
                        p.writeInt(MapId.NONE);
                        p.writeInt(0);
                        p.writeInt(0);
                    }
                } else {
                    p.writeInt(MapId.NONE);
                    p.writeInt(MapId.NONE);
                    p.writeInt(0);
                    p.writeInt(0);
                }
            } else {
                p.writeInt(MapId.NONE);
                p.writeInt(MapId.NONE);
                p.writeInt(0);
                p.writeInt(0);
            }
        }
    }

    public Packet updateParty(int forChannel, Party party, PartyOperation op, PartyCharacter target) {
        final OutPacket p = OutPacket.create(SendOpcode.PARTY_OPERATION);
        switch (op) {
            case DISBAND:
            case EXPEL:
            case LEAVE:
                p.writeByte(0x0C);
                p.writeInt(party.getId());
                p.writeInt(target.getId());
                if (op == PartyOperation.DISBAND) {
                    p.writeByte(0);
                    p.writeInt(party.getId());
                } else {
                    p.writeByte(1);
                    if (op == PartyOperation.EXPEL) {
                        p.writeByte(1);
                    } else {
                        p.writeByte(0);
                    }
                    p.writeString(target.getName());
                    addPartyStatus(forChannel, party, p, false);
                }
                break;
            case JOIN:
                p.writeByte(0xF);
                p.writeInt(party.getId());
                p.writeString(target.getName());
                addPartyStatus(forChannel, party, p, false);
                break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
                p.writeByte(0x7);
                p.writeInt(party.getId());
                addPartyStatus(forChannel, party, p, false);
                break;
            case CHANGE_LEADER:
                p.writeByte(0x1B);
                p.writeInt(target.getId());
                p.writeByte(0);
                break;
        }
        return p;
    }

    public Packet partyPortal(int townId, int targetId, Point position) {
        final OutPacket p = OutPacket.create(SendOpcode.PARTY_OPERATION);
        p.writeShort(0x23);
        p.writeInt(townId);
        p.writeInt(targetId);
        p.writePos(position);
        return p;
    }

    public Packet updatePartyMemberHP(int cid, int curhp, int maxhp) {
        final OutPacket p = OutPacket.create(SendOpcode.UPDATE_PARTYMEMBER_HP);
        p.writeInt(cid);
        p.writeInt(curhp);
        p.writeInt(maxhp);
        return p;
    }

    public Packet multiChat(String name, String chattext, int mode) {
        OutPacket p = OutPacket.create(SendOpcode.MULTICHAT);
        p.writeByte(mode);
        p.writeString(name);
        p.writeString(chattext);
        return p;
    }

    private void writeIntMask(OutPacket p, Map<MonsterStatus, Integer> stats) {
        int firstmask = 0;
        int secondmask = 0;
        for (MonsterStatus stat : stats.keySet()) {
            if (stat.isFirst()) {
                firstmask |= stat.getValue();
            } else {
                secondmask |= stat.getValue();
            }
        }
        p.writeInt(firstmask);
        p.writeInt(secondmask);
    }

    public Packet applyMonsterStatus(final int oid, final MonsterStatusEffect mse, final List<Integer> reflection) {
        Map<MonsterStatus, Integer> stati = mse.getStati();
        final OutPacket p = OutPacket.create(SendOpcode.APPLY_MONSTER_STATUS);
        p.writeInt(oid);
        p.writeLong(0);
        writeIntMask(p, stati);
        for (Entry<MonsterStatus, Integer> stat : stati.entrySet()) {
            p.writeShort(stat.getValue());
            if (mse.isMonsterSkill()) {
                writeMobSkillId(p, mse.getMobSkill().getId());
            } else {
                p.writeInt(mse.getSkill().getId());
            }
            p.writeShort(-1);
        }
        int size = stati.size();
        if (reflection != null) {
            for (Integer ref : reflection) {
                p.writeInt(ref);
            }
            if (reflection.size() > 0) {
                size /= 2;
            }
        }
        p.writeByte(size);
        p.writeInt(0);
        return p;
    }

    public Packet cancelMonsterStatus(int oid, Map<MonsterStatus, Integer> stats) {
        final OutPacket p = OutPacket.create(SendOpcode.CANCEL_MONSTER_STATUS);
        p.writeInt(oid);
        p.writeLong(0);
        writeIntMask(p, stats);
        p.writeInt(0);
        return p;
    }

    public Packet getClock(int time) {
        OutPacket p = OutPacket.create(SendOpcode.CLOCK);
        p.writeByte(2);
        p.writeInt(time);
        return p;
    }

    public Packet getClockTime(int hour, int min, int sec) {
        OutPacket p = OutPacket.create(SendOpcode.CLOCK);
        p.writeByte(1);
        p.writeByte(hour);
        p.writeByte(min);
        p.writeByte(sec);
        return p;
    }

    public Packet removeClock() {
        final OutPacket p = OutPacket.create(SendOpcode.STOP_CLOCK);
        p.writeByte(0);
        return p;
    }

    public Packet spawnMobMist(int objId, int ownerMobId, MobSkillId msId, Mist mist) {
        return spawnMist(objId, ownerMobId, msId.type().getId(), msId.level(), mist);
    }

    public Packet spawnMist(int objId, int ownerId, int skill, int level, Mist mist) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_MIST);
        p.writeInt(objId);
        p.writeInt(mist.isMobMist() ? 0 : mist.isPoisonMist() ? 1 : mist.isRecoveryMist() ? 4 : 2);
        p.writeInt(ownerId);
        p.writeInt(skill);
        p.writeByte(level);
        p.writeShort(mist.getSkillDelay());
        p.writeInt(mist.getBox().x);
        p.writeInt(mist.getBox().y);
        p.writeInt(mist.getBox().x + mist.getBox().width);
        p.writeInt(mist.getBox().y + mist.getBox().height);
        p.writeInt(0);
        return p;
    }

    public Packet removeMist(int objId) {
        OutPacket p = OutPacket.create(SendOpcode.REMOVE_MIST);
        p.writeInt(objId);
        return p;
    }

    public Packet damageSummon(int cid, int oid, int damage, int monsterIdFrom) {
        final OutPacket p = OutPacket.create(SendOpcode.DAMAGE_SUMMON);
        p.writeInt(cid);
        p.writeInt(oid);
        p.writeByte(12);
        p.writeInt(damage);
        p.writeInt(monsterIdFrom);
        p.writeByte(0);
        return p;
    }

    public Packet damageMonster(int oid, int damage) {
        return damageMonster(oid, damage, 0, 0);
    }

    public Packet healMonster(int oid, int heal, int curhp, int maxhp) {
        return damageMonster(oid, -heal, curhp, maxhp);
    }

    private Packet damageMonster(int oid, int damage, int curhp, int maxhp) {
        final OutPacket p = OutPacket.create(SendOpcode.DAMAGE_MONSTER);
        p.writeInt(oid);
        p.writeByte(0);
        p.writeInt(damage);
        p.writeInt(curhp);
        p.writeInt(maxhp);
        return p;
    }

    public Packet updateBuddylist(Collection<BuddylistEntry> buddylist) {
        OutPacket p = OutPacket.create(SendOpcode.BUDDYLIST);
        p.writeByte(7);
        p.writeByte(buddylist.size());
        for (BuddylistEntry buddy : buddylist) {
            if (buddy.isVisible()) {
                p.writeInt(buddy.getCharacterId());
                p.writeFixedString(getRightPaddedStr(buddy.getName(), '\0', 13));
                p.writeByte(0);
                p.writeInt(buddy.getChannel() - 1);
                p.writeFixedString(getRightPaddedStr(buddy.getGroup(), '\0', 13));
                p.writeInt(0);
            }
        }
        for (int x = 0; x < buddylist.size(); x++) {
            p.writeInt(0);
        }
        return p;
    }

    public Packet requestBuddylistAdd(int chrIdFrom, int chrId, String nameFrom) {
        OutPacket p = OutPacket.create(SendOpcode.BUDDYLIST);
        p.writeByte(9);
        p.writeInt(chrIdFrom);
        p.writeString(nameFrom);
        p.writeInt(chrIdFrom);
        p.writeFixedString(getRightPaddedStr(nameFrom, '\0', 11));
        p.writeByte(0x09);
        p.writeByte(0xf0);
        p.writeByte(0x01);
        p.writeInt(0x0f);
        p.writeFixedString("Default Group");
        p.writeByte(0);
        p.writeInt(chrId);
        return p;
    }

    public Packet updateBuddyChannel(int characterid, int channel) {
        final OutPacket p = OutPacket.create(SendOpcode.BUDDYLIST);
        p.writeByte(0x14);
        p.writeInt(characterid);
        p.writeByte(0);
        p.writeInt(channel);
        return p;
    }

    public Packet itemEffect(int characterid, int itemid) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_ITEM_EFFECT);
        p.writeInt(characterid);
        p.writeInt(itemid);
        return p;
    }

    public Packet updateBuddyCapacity(int capacity) {
        final OutPacket p = OutPacket.create(SendOpcode.BUDDYLIST);
        p.writeByte(0x15);
        p.writeByte(capacity);
        return p;
    }

    public Packet showChair(int characterid, int itemid) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_CHAIR);
        p.writeInt(characterid);
        p.writeInt(itemid);
        return p;
    }

    public Packet cancelChair(int id) {
        final OutPacket p = OutPacket.create(SendOpcode.CANCEL_CHAIR);
        if (id < 0) {
            p.writeByte(0);
        } else {
            p.writeByte(1);
            p.writeShort(id);
        }
        return p;
    }

    public Packet spawnReactor(Reactor reactor) {
        OutPacket p = OutPacket.create(SendOpcode.REACTOR_SPAWN);
        p.writeInt(reactor.getObjectId());
        p.writeInt(reactor.getId());
        p.writeByte(reactor.getState());
        p.writePos(reactor.getPosition());
        p.writeByte(0);
        p.writeShort(0);
        return p;
    }

    public Packet triggerReactor(Reactor reactor, int stance) {
        OutPacket p = OutPacket.create(SendOpcode.REACTOR_HIT);
        p.writeInt(reactor.getObjectId());
        p.writeByte(reactor.getState());
        p.writePos(reactor.getPosition());
        p.writeByte(stance);
        p.writeShort(0);
        p.writeByte(5);
        return p;
    }

    public Packet destroyReactor(Reactor reactor) {
        OutPacket p = OutPacket.create(SendOpcode.REACTOR_DESTROY);
        p.writeInt(reactor.getObjectId());
        p.writeByte(reactor.getState());
        p.writePos(reactor.getPosition());
        return p;
    }

    public Packet musicChange(String song) {
        return environmentChange(song, 6);
    }

    public Packet showEffect(String effect) {
        return environmentChange(effect, 3);
    }

    public Packet playSound(String sound) {
        return environmentChange(sound, 4);
    }

    public Packet environmentChange(String env, int mode) {
        OutPacket p = OutPacket.create(SendOpcode.FIELD_EFFECT);
        p.writeByte(mode);
        p.writeString(env);
        return p;
    }

    public Packet environmentMove(String env, int mode) {
        OutPacket p = OutPacket.create(SendOpcode.FIELD_OBSTACLE_ONOFF);
        p.writeString(env);
        p.writeInt(mode);
        return p;
    }

    public Packet environmentMoveList(Set<Entry<String, Integer>> envList) {
        OutPacket p = OutPacket.create(SendOpcode.FIELD_OBSTACLE_ONOFF_LIST);
        p.writeInt(envList.size());

        for (Entry<String, Integer> envMove : envList) {
            p.writeString(envMove.getKey());
            p.writeInt(envMove.getValue());
        }

        return p;
    }

    public Packet environmentMoveReset() {
        return OutPacket.create(SendOpcode.FIELD_OBSTACLE_ALL_RESET);
    }

    public Packet startMapEffect(String msg, int itemId, boolean active) {
        OutPacket p = OutPacket.create(SendOpcode.BLOW_WEATHER);
        p.writeBool(!active);
        p.writeInt(itemId);
        if (active) {
            p.writeString(msg);
        }
        return p;
    }

    public Packet removeMapEffect() {
        OutPacket p = OutPacket.create(SendOpcode.BLOW_WEATHER);
        p.writeByte(0);
        p.writeInt(0);
        return p;
    }

    public Packet mapEffect(String path) {
        final OutPacket p = OutPacket.create(SendOpcode.FIELD_EFFECT);
        p.writeByte(3);
        p.writeString(path);
        return p;
    }

    public Packet mapSound(String path) {
        final OutPacket p = OutPacket.create(SendOpcode.FIELD_EFFECT);
        p.writeByte(4);
        p.writeString(path);
        return p;
    }

    public Packet skillEffect(Character from, int skillId, int level, byte flags, int speed, byte direction) {
        final OutPacket p = OutPacket.create(SendOpcode.SKILL_EFFECT);
        p.writeInt(from.getId());
        p.writeInt(skillId);
        p.writeByte(level);
        p.writeByte(flags);
        p.writeByte(speed);
        p.writeByte(direction);
        return p;
    }

    public Packet skillCancel(Character from, int skillId) {
        final OutPacket p = OutPacket.create(SendOpcode.CANCEL_SKILL_EFFECT);
        p.writeInt(from.getId());
        p.writeInt(skillId);
        return p;
    }

    public Packet catchMonster(int mobOid, byte success) {
        final OutPacket p = OutPacket.create(SendOpcode.CATCH_MONSTER);
        p.writeInt(mobOid);
        p.writeByte(success);
        return p;
    }

    public Packet catchMonster(int mobOid, int itemid, byte success) {
        final OutPacket p = OutPacket.create(SendOpcode.CATCH_MONSTER_WITH_ITEM);
        p.writeInt(mobOid);
        p.writeInt(itemid);
        p.writeByte(success);
        return p;
    }


    public Packet sendHint(String hint, int width, int height) {
        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (height < 5) {
            height = 5;
        }
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_HINT);
        p.writeString(hint);
        p.writeShort(width);
        p.writeShort(height);
        p.writeByte(1);
        return p;
    }

    public Packet messengerInvite(String from, int messengerid) {
        final OutPacket p = OutPacket.create(SendOpcode.MESSENGER);
        p.writeByte(0x03);
        p.writeString(from);
        p.writeByte(0);
        p.writeInt(messengerid);
        p.writeByte(0);
        return p;
    }

    public Packet OnCoupleMessage(String fiance, String text, boolean spouse) {
        OutPacket p = OutPacket.create(SendOpcode.SPOUSE_CHAT);
        p.writeByte(spouse ? 5 : 4);
        if (spouse) {
            p.writeString(fiance);
        }
        p.writeByte(spouse ? 5 : 1);
        p.writeString(text);
        return p;
    }

    public Packet addMessengerPlayer(String from, Character chr, int position, int channel) {
        final OutPacket p = OutPacket.create(SendOpcode.MESSENGER);
        p.writeByte(0x00);
        p.writeByte(position);
        addCharLook(p, chr, true);
        p.writeString(from);
        p.writeByte(channel);
        p.writeByte(0x00);
        return p;
    }

    public Packet removeMessengerPlayer(int position) {
        final OutPacket p = OutPacket.create(SendOpcode.MESSENGER);
        p.writeByte(0x02);
        p.writeByte(position);
        return p;
    }

    public Packet updateMessengerPlayer(String from, Character chr, int position, int channel) {
        final OutPacket p = OutPacket.create(SendOpcode.MESSENGER);
        p.writeByte(0x07);
        p.writeByte(position);
        addCharLook(p, chr, true);
        p.writeString(from);
        p.writeByte(channel);
        p.writeByte(0x00);
        return p;
    }

    public Packet joinMessenger(int position) {
        final OutPacket p = OutPacket.create(SendOpcode.MESSENGER);
        p.writeByte(0x01);
        p.writeByte(position);
        return p;
    }

    public Packet messengerChat(String text) {
        final OutPacket p = OutPacket.create(SendOpcode.MESSENGER);
        p.writeByte(0x06);
        p.writeString(text);
        return p;
    }

    public Packet messengerNote(String text, int mode, int mode2) {
        final OutPacket p = OutPacket.create(SendOpcode.MESSENGER);
        p.writeByte(mode);
        p.writeString(text);
        p.writeByte(mode2);
        return p;
    }

    private void addPetInfo(final OutPacket p, Pet pet, boolean showpet) {
        p.writeByte(1);
        if (showpet) {
            p.writeByte(0);
        }

        p.writeInt(pet.getItemId());
        p.writeString(pet.getName());
        p.writeLong(pet.getUniqueId());
        p.writePos(pet.getPos());
        p.writeByte(pet.getStance());
        p.writeInt(pet.getFh());
    }

    public Packet showPet(Character chr, Pet pet, boolean remove, boolean hunger) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_PET);
        p.writeInt(chr.getId());
        p.writeByte(chr.getPetIndex(pet));
        if (remove) {
            p.writeByte(0);
            p.writeBool(hunger);
        } else {
            addPetInfo(p, pet, true);
        }
        return p;
    }

    public Packet movePet(int cid, int pid, byte slot, List<LifeMovementFragment> moves) {
        final OutPacket p = OutPacket.create(SendOpcode.MOVE_PET);
        p.writeInt(cid);
        p.writeByte(slot);
        p.writeInt(pid);
        serializeMovementList(p, moves);
        return p;
    }

    public Packet petChat(int cid, byte index, int act, String text) {
        final OutPacket p = OutPacket.create(SendOpcode.PET_CHAT);
        p.writeInt(cid);
        p.writeByte(index);
        p.writeByte(0);
        p.writeByte(act);
        p.writeString(text);
        p.writeByte(0);
        return p;
    }

    public Packet petFoodResponse(int cid, byte index, boolean success, boolean balloonType) {
        final OutPacket p = OutPacket.create(SendOpcode.PET_COMMAND);
        p.writeInt(cid);
        p.writeByte(index);
        p.writeByte(1);
        p.writeBool(success);
        p.writeBool(balloonType);
        return p;
    }

    public Packet commandResponse(int cid, byte index, boolean talk, int animation, boolean balloonType) {
        final OutPacket p = OutPacket.create(SendOpcode.PET_COMMAND);
        p.writeInt(cid);
        p.writeByte(index);
        p.writeByte(0);
        p.writeByte(animation);
        p.writeBool(!talk);
        p.writeBool(balloonType);
        return p;
    }

    public Packet showOwnPetLevelUp(byte index) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_ITEM_GAIN_INCHAT);
        p.writeByte(4);
        p.writeByte(0);
        p.writeByte(index);
        return p;
    }

    public Packet showPetLevelUp(Character chr, byte index) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_FOREIGN_EFFECT);
        p.writeInt(chr.getId());
        p.writeByte(4);
        p.writeByte(0);
        p.writeByte(index);
        return p;
    }

    public Packet changePetName(Character chr, String newname, int slot) {
        OutPacket p = OutPacket.create(SendOpcode.PET_NAMECHANGE);
        p.writeInt(chr.getId());
        p.writeByte(0);
        p.writeString(newname);
        p.writeByte(0);
        return p;
    }

    public Packet loadExceptionList(final int cid, final int petId, final byte petIdx, final List<Integer> data) {
        final OutPacket p = OutPacket.create(SendOpcode.PET_EXCEPTION_LIST);
        p.writeInt(cid);
        p.writeByte(petIdx);
        p.writeLong(petId);
        p.writeByte(data.size());
        for (final Integer ids : data) {
            p.writeInt(ids);
        }
        return p;
    }

    public Packet petStatUpdate(Character chr) {


        final OutPacket p = OutPacket.create(SendOpcode.STAT_CHANGED);
        int mask = 0;
        mask |= Stat.PET.getValue();
        p.writeByte(0);
        p.writeInt(mask);
        Pet[] pets = chr.getPets();
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                p.writeLong(pets[i].getUniqueId());
            } else {
                p.writeLong(0);
            }
        }
        p.writeByte(0);
        return p;
    }

    public Packet showForcedEquip(int team) {
        OutPacket p = OutPacket.create(SendOpcode.FORCED_MAP_EQUIP);
        if (team > -1) {
            p.writeByte(team);
        }
        return p;
    }

    public Packet summonSkill(int cid, int summonSkillId, int newStance) {
        final OutPacket p = OutPacket.create(SendOpcode.SUMMON_SKILL);
        p.writeInt(cid);
        p.writeInt(summonSkillId);
        p.writeByte(newStance);
        return p;
    }

    public Packet skillCooldown(int sid, int time) {
        final OutPacket p = OutPacket.create(SendOpcode.COOLDOWN);
        p.writeInt(sid);
        p.writeShort(time);
        return p;
    }

    public Packet skillBookResult(Character chr, int skillid, int maxlevel, boolean canuse, boolean success) {
        final OutPacket p = OutPacket.create(SendOpcode.SKILL_LEARN_ITEM_RESULT);
        p.writeInt(chr.getId());
        p.writeByte(1);
        p.writeInt(skillid);
        p.writeInt(maxlevel);
        p.writeByte(canuse ? 1 : 0);
        p.writeByte(success ? 1 : 0);
        return p;
    }

    public Packet getMacros(SkillMacro[] macros) {
        final OutPacket p = OutPacket.create(SendOpcode.MACRO_SYS_DATA_INIT);
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        p.writeByte(count);
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                p.writeString(macro.getName());
                p.writeByte(macro.getShout());
                p.writeInt(macro.getSkill1());
                p.writeInt(macro.getSkill2());
                p.writeInt(macro.getSkill3());
            }
        }
        return p;
    }

    public Packet showAllCharacterInfo(int worldid, List<Character> chars, boolean usePic) {
        final OutPacket p = OutPacket.create(SendOpcode.VIEW_ALL_CHAR);
        p.writeByte(0);
        p.writeByte(worldid);
        p.writeByte(chars.size());
        for (Character chr : chars) {
            addCharEntry(p, chr, true);
        }
        p.writeByte(usePic ? 1 : 2);
        return p;
    }

    public Packet updateMount(int charid, Mount mount, boolean levelup) {
        final OutPacket p = OutPacket.create(SendOpcode.SET_TAMING_MOB_INFO);
        p.writeInt(charid);
        p.writeInt(mount.getLevel());
        p.writeInt(mount.getExp());
        p.writeInt(mount.getTiredness());
        p.writeByte(levelup ? (byte) 1 : (byte) 0);
        return p;
    }

    public Packet crogBoatPacket(boolean type) {
        OutPacket p = OutPacket.create(SendOpcode.CONTI_MOVE);
        p.writeByte(10);
        p.writeByte(type ? 4 : 5);
        return p;
    }

    public Packet boatPacket(boolean type) {
        OutPacket p = OutPacket.create(SendOpcode.CONTI_STATE);
        p.writeByte(type ? 1 : 2);
        p.writeByte(0);
        return p;
    }

    public Packet getMiniGame(Client c, MiniGame minigame, boolean owner, int piece) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.ROOM.getCode());
        p.writeByte(1);
        p.writeByte(0);
        p.writeBool(!owner);
        p.writeByte(0);
        addCharLook(p, minigame.getOwner(), false);
        p.writeString(minigame.getOwner().getName());
        if (minigame.getVisitor() != null) {
            Character visitor = minigame.getVisitor();
            p.writeByte(1);
            addCharLook(p, visitor, false);
            p.writeString(visitor.getName());
        }
        p.writeByte(0xFF);
        p.writeByte(0);
        p.writeInt(1);
        p.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.WIN, true));
        p.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.TIE, true));
        p.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.LOSS, true));
        p.writeInt(minigame.getOwnerScore());
        if (minigame.getVisitor() != null) {
            Character visitor = minigame.getVisitor();
            p.writeByte(1);
            p.writeInt(1);
            p.writeInt(visitor.getMiniGamePoints(MiniGameResult.WIN, true));
            p.writeInt(visitor.getMiniGamePoints(MiniGameResult.TIE, true));
            p.writeInt(visitor.getMiniGamePoints(MiniGameResult.LOSS, true));
            p.writeInt(minigame.getVisitorScore());
        }
        p.writeByte(0xFF);
        p.writeString(minigame.getDescription());
        p.writeByte(piece);
        p.writeByte(0);
        return p;
    }

    public Packet getMiniGameReady(MiniGame game) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.READY.getCode());
        return p;
    }

    public Packet getMiniGameUnReady(MiniGame game) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.UN_READY.getCode());
        return p;
    }

    public Packet getMiniGameStart(MiniGame game, int loser) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.START.getCode());
        p.writeByte(loser);
        return p;
    }

    public Packet getMiniGameSkipOwner(MiniGame game) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.SKIP.getCode());
        p.writeByte(0x01);
        return p;
    }

    public Packet getMiniGameRequestTie(MiniGame game) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.REQUEST_TIE.getCode());
        return p;
    }

    public Packet getMiniGameDenyTie(MiniGame game) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.ANSWER_TIE.getCode());
        return p;
    }

    public Packet getMiniRoomError(int status) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.ROOM.getCode());
        p.writeByte(0);
        p.writeByte(status);
        return p;
    }

    public Packet getMiniGameSkipVisitor(MiniGame game) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeShort(PlayerInteractionHandler.Action.SKIP.getCode());
        return p;
    }

    public Packet getMiniGameMoveOmok(MiniGame game, int move1, int move2, int move3) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.MOVE_OMOK.getCode());
        p.writeInt(move1);
        p.writeInt(move2);
        p.writeByte(move3);
        return p;
    }

    public Packet getMiniGameNewVisitor(MiniGame minigame, Character chr, int slot) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.VISIT.getCode());
        p.writeByte(slot);
        addCharLook(p, chr, false);
        p.writeString(chr.getName());
        p.writeInt(1);
        p.writeInt(chr.getMiniGamePoints(MiniGameResult.WIN, true));
        p.writeInt(chr.getMiniGamePoints(MiniGameResult.TIE, true));
        p.writeInt(chr.getMiniGamePoints(MiniGameResult.LOSS, true));
        p.writeInt(minigame.getVisitorScore());
        return p;
    }

    public Packet getMiniGameRemoveVisitor() {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.EXIT.getCode());
        p.writeByte(1);
        return p;
    }

    private Packet getMiniGameResult(MiniGame game, int tie, int result, int forfeit) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.GET_RESULT.getCode());

        int matchResultType;
        if (tie == 0 && forfeit != 1) {
            matchResultType = 0;
        } else if (tie != 0) {
            matchResultType = 1;
        } else {
            matchResultType = 2;
        }

        p.writeByte(matchResultType);
        p.writeBool(result == 2);

        boolean omok = game.isOmok();
        if (matchResultType == 1) {
            p.writeByte(0);
            p.writeShort(0);
            p.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.WIN, omok));
            p.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.TIE, omok));
            p.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.LOSS, omok));
            p.writeInt(game.getOwnerScore());

            p.writeInt(0);
            p.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.WIN, omok));
            p.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.TIE, omok));
            p.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.LOSS, omok));
            p.writeInt(game.getVisitorScore());
            p.writeByte(0);
        } else {
            p.writeInt(0);
            p.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.WIN, omok));
            p.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.TIE, omok));
            p.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.LOSS, omok));
            p.writeInt(game.getOwnerScore());
            p.writeInt(0);
            p.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.WIN, omok));
            p.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.TIE, omok));
            p.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.LOSS, omok));
            p.writeInt(game.getVisitorScore());
        }

        return p;
    }

    public Packet getMiniGameOwnerWin(MiniGame game, boolean forfeit) {
        return getMiniGameResult(game, 0, 1, forfeit ? 1 : 0);
    }

    public Packet getMiniGameVisitorWin(MiniGame game, boolean forfeit) {
        return getMiniGameResult(game, 0, 2, forfeit ? 1 : 0);
    }

    public Packet getMiniGameTie(MiniGame game) {
        return getMiniGameResult(game, 1, 3, 0);
    }

    public Packet getMiniGameClose(boolean visitor, int type) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.EXIT.getCode());
        p.writeBool(visitor);
        p.writeByte(type);
        return p;
    }

    public Packet getMatchCard(Client c, MiniGame minigame, boolean owner, int piece) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.ROOM.getCode());
        p.writeByte(2);
        p.writeByte(2);
        p.writeBool(!owner);
        p.writeByte(0);
        addCharLook(p, minigame.getOwner(), false);
        p.writeString(minigame.getOwner().getName());
        if (minigame.getVisitor() != null) {
            Character visitor = minigame.getVisitor();
            p.writeByte(1);
            addCharLook(p, visitor, false);
            p.writeString(visitor.getName());
        }
        p.writeByte(0xFF);
        p.writeByte(0);
        p.writeInt(2);
        p.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.WIN, false));
        p.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.TIE, false));
        p.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.LOSS, false));


        p.writeInt(minigame.getOwnerScore());
        if (minigame.getVisitor() != null) {
            Character visitor = minigame.getVisitor();
            p.writeByte(1);
            p.writeInt(2);
            p.writeInt(visitor.getMiniGamePoints(MiniGameResult.WIN, false));
            p.writeInt(visitor.getMiniGamePoints(MiniGameResult.TIE, false));
            p.writeInt(visitor.getMiniGamePoints(MiniGameResult.LOSS, false));
            p.writeInt(minigame.getVisitorScore());
        }
        p.writeByte(0xFF);
        p.writeString(minigame.getDescription());
        p.writeByte(piece);
        p.writeByte(0);
        return p;
    }

    public Packet getMatchCardStart(MiniGame game, int loser) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.START.getCode());
        p.writeByte(loser);

        int last;
        if (game.getMatchesToWin() > 10) {
            last = 30;
        } else if (game.getMatchesToWin() > 6) {
            last = 20;
        } else {
            last = 12;
        }

        p.writeByte(last);
        for (int i = 0; i < last; i++) {
            p.writeInt(game.getCardId(i));
        }
        return p;
    }

    public Packet getMatchCardNewVisitor(MiniGame minigame, Character chr, int slot) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.VISIT.getCode());
        p.writeByte(slot);
        addCharLook(p, chr, false);
        p.writeString(chr.getName());
        p.writeInt(1);
        p.writeInt(chr.getMiniGamePoints(MiniGameResult.WIN, false));
        p.writeInt(chr.getMiniGamePoints(MiniGameResult.TIE, false));
        p.writeInt(chr.getMiniGamePoints(MiniGameResult.LOSS, false));
        p.writeInt(minigame.getVisitorScore());
        return p;
    }

    public Packet getMatchCardSelect(MiniGame game, int turn, int slot, int firstslot, int type) {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.SELECT_CARD.getCode());
        p.writeByte(turn);
        if (turn == 1) {
            p.writeByte(slot);
        } else if (turn == 0) {
            p.writeByte(slot);
            p.writeByte(firstslot);
            p.writeByte(type);
        }
        return p;
    }

    public Packet rpsMesoError(int mesos) {
        OutPacket p = OutPacket.create(SendOpcode.RPS_GAME);
        p.writeByte(0x06);
        if (mesos != -1) {
            p.writeInt(mesos);
        }
        return p;
    }

    public Packet rpsSelection(byte selection, byte answer) {
        OutPacket p = OutPacket.create(SendOpcode.RPS_GAME);
        p.writeByte(0x0B);
        p.writeByte(selection);
        p.writeByte(answer);
        return p;
    }

    public Packet rpsMode(byte mode) {
        OutPacket p = OutPacket.create(SendOpcode.RPS_GAME);
        p.writeByte(mode);
        return p;
    }

    public Packet fredrickMessage(byte operation) {
        final OutPacket p = OutPacket.create(SendOpcode.FREDRICK_MESSAGE);
        p.writeByte(operation);
        return p;
    }

    public Packet getFredrick(byte op) {
        final OutPacket p = OutPacket.create(SendOpcode.FREDRICK);
        p.writeByte(op);

        switch (op) {
            case 0x24:
                p.skip(8);
                break;
            default:
                p.writeByte(0);
                break;
        }

        return p;
    }

    public Packet getFredrick(Character chr) {
        final OutPacket p = OutPacket.create(SendOpcode.FREDRICK);
        p.writeByte(0x23);
        p.writeInt(NpcId.FREDRICK);
        p.writeInt(32272);
        p.skip(5);
        p.writeInt(chr.getMerchantNetMeso());
        p.writeByte(0);
        try {
            List<Pair<Item, InventoryType>> items = ItemFactory.MERCHANT.loadItems(chr.getId(), false);
            p.writeByte(items.size());

            for (Pair<Item, InventoryType> item : items) {
                addItemInfo(p, item.getLeft(), true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        p.skip(3);
        return p;
    }

    public Packet addOmokBox(Character chr, int amount, int type) {
        OutPacket p = OutPacket.create(SendOpcode.UPDATE_CHAR_BOX);
        p.writeInt(chr.getId());
        addAnnounceBox(p, chr.getMiniGame(), amount, type);
        return p;
    }

    public Packet addMatchCardBox(Character chr, int amount, int type) {
        OutPacket p = OutPacket.create(SendOpcode.UPDATE_CHAR_BOX);
        p.writeInt(chr.getId());
        addAnnounceBox(p, chr.getMiniGame(), amount, type);
        return p;
    }

    public Packet removeMinigameBox(Character chr) {
        OutPacket p = OutPacket.create(SendOpcode.UPDATE_CHAR_BOX);
        p.writeInt(chr.getId());
        p.writeByte(0);
        return p;
    }

    public Packet getPlayerShopChat(Character chr, String chat, byte slot) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.CHAT.getCode());
        p.writeByte(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        p.writeByte(slot);
        p.writeString(chr.getName() + " : " + chat);
        return p;
    }

    public Packet getTradeChat(Character chr, String chat, boolean owner) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.CHAT.getCode());
        p.writeByte(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        p.writeByte(owner ? 0 : 1);
        p.writeString(chr.getName() + " : " + chat);
        return p;
    }

    public Packet hiredMerchantBox() {
        final OutPacket p = OutPacket.create(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT);
        p.writeByte(0x07);
        return p;
    }


    public Packet getOwlMessage(int msg) {
        OutPacket p = OutPacket.create(SendOpcode.SHOP_LINK_RESULT);
        p.writeByte(msg);
        return p;
    }

    public Packet owlOfMinerva(Client c, int itemId, List<Pair<PlayerShopItem, AbstractMapObject>> hmsAvailable) {
        byte itemType = ItemConstants.getInventoryType(itemId).getType();

        OutPacket p = OutPacket.create(SendOpcode.SHOP_SCANNER_RESULT);
        p.writeByte(6);
        p.writeInt(0);
        p.writeInt(itemId);
        p.writeInt(hmsAvailable.size());
        for (Pair<PlayerShopItem, AbstractMapObject> hme : hmsAvailable) {
            PlayerShopItem item = hme.getLeft();
            AbstractMapObject mo = hme.getRight();

            if (mo instanceof PlayerShop ps) {
                Character owner = ps.getOwner();

                p.writeString(owner.getName());
                p.writeInt(owner.getMapId());
                p.writeString(ps.getDescription());
                p.writeInt(item.getBundles());
                p.writeInt(item.getItem().getQuantity());
                p.writeInt(item.getPrice());
                p.writeInt(owner.getId());
                p.writeByte(owner.getClient().getChannel() - 1);
            } else {
                HiredMerchant hm = (HiredMerchant) mo;

                p.writeString(hm.getOwner());
                p.writeInt(hm.getMapId());
                p.writeString(hm.getDescription());
                p.writeInt(item.getBundles());
                p.writeInt(item.getItem().getQuantity());
                p.writeInt(item.getPrice());
                p.writeInt(hm.getOwnerId());
                p.writeByte(hm.getChannel() - 1);
            }

            p.writeByte(itemType);
            if (itemType == InventoryType.EQUIP.getType()) {
                addItemInfo(p, item.getItem(), true);
            }
        }
        return p;
    }

    public Packet getOwlOpen(List<Integer> owlLeaderboards) {
        OutPacket p = OutPacket.create(SendOpcode.SHOP_SCANNER_RESULT);
        p.writeByte(7);
        p.writeByte(owlLeaderboards.size());
        for (Integer i : owlLeaderboards) {
            p.writeInt(i);
        }

        return p;
    }

    public Packet retrieveFirstMessage() {
        final OutPacket p = OutPacket.create(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT);
        p.writeByte(0x09);
        return p;
    }

    public Packet remoteChannelChange(byte ch) {
        final OutPacket p = OutPacket.create(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT);
        p.writeByte(0x10);
        p.writeInt(0);
        p.writeByte(ch);
        return p;
    }

    public Packet getHiredMerchant(Character chr, HiredMerchant hm, boolean firstTime) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.ROOM.getCode());
        p.writeByte(0x05);
        p.writeByte(0x04);
        p.writeShort(hm.getVisitorSlotThreadsafe(chr) + 1);
        p.writeInt(hm.getItemId());
        p.writeString("Hired Merchant");

        Character[] visitors = hm.getVisitorCharacters();
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null) {
                p.writeByte(i + 1);
                addCharLook(p, visitors[i], false);
                p.writeString(visitors[i].getName());
            }
        }
        p.writeByte(-1);
        if (hm.isOwner(chr)) {
            List<Pair<String, Byte>> msgList = hm.getMessages();

            p.writeShort(msgList.size());
            for (Pair<String, Byte> stringBytePair : msgList) {
                p.writeString(stringBytePair.getLeft());
                p.writeByte(stringBytePair.getRight());
            }
        } else {
            p.writeShort(0);
        }
        p.writeString(hm.getOwner());
        if (hm.isOwner(chr)) {
            p.writeShort(0);
            p.writeShort(hm.getTimeOpen());
            p.writeByte(firstTime ? 1 : 0);
            List<HiredMerchant.SoldItem> sold = hm.getSold();
            p.writeByte(sold.size());
            for (HiredMerchant.SoldItem s : sold) {
                p.writeInt(s.getItemId());
                p.writeShort(s.getQuantity());
                p.writeInt(s.getMesos());
                p.writeString(s.getBuyer());
            }
            p.writeInt(chr.getMerchantMeso());
        }
        p.writeString(hm.getDescription());
        p.writeByte(0x10);
        p.writeInt(hm.isOwner(chr) ? chr.getMerchantMeso() : chr.getMeso());
        p.writeByte(hm.getItems().size());
        if (hm.getItems().isEmpty()) {
            p.writeByte(0);
        } else {
            for (PlayerShopItem item : hm.getItems()) {
                p.writeShort(item.getBundles());
                p.writeShort(item.getItem().getQuantity());
                p.writeInt(item.getPrice());
                addItemInfo(p, item.getItem(), true);
            }
        }
        return p;
    }

    public Packet updateHiredMerchant(HiredMerchant hm, Character chr) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
        p.writeInt(hm.isOwner(chr) ? chr.getMerchantMeso() : chr.getMeso());
        p.writeByte(hm.getItems().size());
        for (PlayerShopItem item : hm.getItems()) {
            p.writeShort(item.getBundles());
            p.writeShort(item.getItem().getQuantity());
            p.writeInt(item.getPrice());
            addItemInfo(p, item.getItem(), true);
        }
        return p;
    }

    public Packet hiredMerchantChat(String message, byte slot) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.CHAT.getCode());
        p.writeByte(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        p.writeByte(slot);
        p.writeString(message);
        return p;
    }

    public Packet hiredMerchantVisitorLeave(int slot) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.EXIT.getCode());
        if (slot != 0) {
            p.writeByte(slot);
        }
        return p;
    }

    public Packet hiredMerchantOwnerLeave() {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.REAL_CLOSE_MERCHANT.getCode());
        p.writeByte(0);
        return p;
    }

    public Packet hiredMerchantOwnerMaintenanceLeave() {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.REAL_CLOSE_MERCHANT.getCode());
        p.writeByte(5);
        return p;
    }

    public Packet hiredMerchantMaintenanceMessage() {
        OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.ROOM.getCode());
        p.writeByte(0x00);
        p.writeByte(0x12);
        return p;
    }

    public Packet leaveHiredMerchant(int slot, int status2) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.EXIT.getCode());
        p.writeByte(slot);
        p.writeByte(status2);
        return p;
    }

    public Packet viewMerchantVisitorHistory(List<HiredMerchant.PastVisitor> pastVisitors) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.VIEW_VISITORS.getCode());
        p.writeShort(pastVisitors.size());
        for (HiredMerchant.PastVisitor pastVisitor : pastVisitors) {
            p.writeString(pastVisitor.chrName());
            p.writeInt((int) pastVisitor.visitDuration().toMillis());
        }
        return p;
    }

    public Packet viewMerchantBlacklist(Set<String> chrNames) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.VIEW_BLACKLIST.getCode());
        p.writeShort(chrNames.size());
        for (String chrName : chrNames) {
            p.writeString(chrName);
        }
        return p;
    }

    public Packet hiredMerchantVisitorAdd(Character chr, int slot) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(PlayerInteractionHandler.Action.VISIT.getCode());
        p.writeByte(slot);
        addCharLook(p, chr, false);
        p.writeString(chr.getName());
        return p;
    }

    public Packet spawnHiredMerchantBox(HiredMerchant hm) {
        final OutPacket p = OutPacket.create(SendOpcode.SPAWN_HIRED_MERCHANT);
        p.writeInt(hm.getOwnerId());
        p.writeInt(hm.getItemId());
        p.writeShort((short) hm.getPosition().getX());
        p.writeShort((short) hm.getPosition().getY());
        p.writeShort(0);
        p.writeString(hm.getOwner());
        p.writeByte(0x05);
        p.writeInt(hm.getObjectId());
        p.writeString(hm.getDescription());
        p.writeByte(hm.getItemId() % 100);
        p.writeBytes(new byte[]{1, 4});
        return p;
    }

    public Packet removeHiredMerchantBox(int id) {
        final OutPacket p = OutPacket.create(SendOpcode.DESTROY_HIRED_MERCHANT);
        p.writeInt(id);
        return p;
    }

    public Packet spawnPlayerNPC(PlayerNPC npc) {
        final OutPacket p = OutPacket.create(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER);
        p.writeByte(1);
        p.writeInt(npc.getObjectId());
        p.writeInt(npc.getScriptId());
        p.writeShort(npc.getPosition().x);
        p.writeShort(npc.getCY());
        p.writeByte(npc.getDirection());
        p.writeShort(npc.getFH());
        p.writeShort(npc.getRX0());
        p.writeShort(npc.getRX1());
        p.writeByte(1);
        return p;
    }

    public Packet getPlayerNPC(PlayerNPC npc) {
        final OutPacket p = OutPacket.create(SendOpcode.IMITATED_NPC_DATA);
        p.writeByte(0x01);
        p.writeInt(npc.getScriptId());
        p.writeString(npc.getName());
        p.writeByte(npc.getGender());
        p.writeByte(npc.getSkin());
        p.writeInt(npc.getFace());
        p.writeByte(0);
        p.writeInt(npc.getHair());
        Map<Short, Integer> equip = npc.getEquips();
        Map<Short, Integer> myEquip = new LinkedHashMap<>();
        Map<Short, Integer> maskedEquip = new LinkedHashMap<>();
        for (short position : equip.keySet()) {
            short pos = (byte) (position * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, equip.get(position));
            } else if ((pos > 100 && pos != 111) || pos == -128) {
                pos -= 100;
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, equip.get(position));
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, equip.get(position));
            }
        }
        for (Entry<Short, Integer> entry : myEquip.entrySet()) {
            p.writeByte(entry.getKey());
            p.writeInt(entry.getValue());
        }
        p.writeByte(0xFF);
        for (Entry<Short, Integer> entry : maskedEquip.entrySet()) {
            p.writeByte(entry.getKey());
            p.writeInt(entry.getValue());
        }
        p.writeByte(0xFF);
        Integer cWeapon = equip.get((byte) -111);
        if (cWeapon != null) {
            p.writeInt(cWeapon);
        } else {
            p.writeInt(0);
        }
        for (int i = 0; i < 3; i++) {
            p.writeInt(0);
        }
        return p;
    }

    public Packet removePlayerNPC(int oid) {
        final OutPacket p = OutPacket.create(SendOpcode.IMITATED_NPC_DATA);
        p.writeByte(0x00);
        p.writeInt(oid);
        return p;
    }

    public Packet sendYellowTip(String tip) {
        final OutPacket p = OutPacket.create(SendOpcode.SET_WEEK_EVENT_MESSAGE);
        p.writeByte(0xFF);
        p.writeString(tip);
        p.writeShort(0);
        return p;
    }

    public Packet givePirateBuff(List<Pair<BuffStat, Integer>> statups, int buffid, int duration) {
        OutPacket p = OutPacket.create(SendOpcode.GIVE_BUFF);
        boolean infusion = buffid == Buccaneer.SPEED_INFUSION || buffid == ThunderBreaker.SPEED_INFUSION || buffid == Corsair.SPEED_INFUSION;
        writeLongMask(p, statups);
        p.writeShort(0);
        for (Pair<BuffStat, Integer> stat : statups) {
            p.writeInt(stat.getRight().shortValue());
            p.writeInt(buffid);
            p.skip(infusion ? 10 : 5);
            p.writeShort(duration);
        }
        p.skip(3);
        return p;
    }

    public Packet giveForeignPirateBuff(int cid, int buffid, int time, List<Pair<BuffStat, Integer>> statups) {
        OutPacket p = OutPacket.create(SendOpcode.GIVE_FOREIGN_BUFF);
        boolean infusion = buffid == Buccaneer.SPEED_INFUSION || buffid == ThunderBreaker.SPEED_INFUSION || buffid == Corsair.SPEED_INFUSION;
        p.writeInt(cid);
        writeLongMask(p, statups);
        p.writeShort(0);
        for (Pair<BuffStat, Integer> statup : statups) {
            p.writeInt(statup.getRight().shortValue());
            p.writeInt(buffid);
            p.skip(infusion ? 10 : 5);
            p.writeShort(time);
        }
        p.writeShort(0);
        p.writeByte(2);
        return p;
    }

    public Packet sendMTS(List<MTSItemInfo> items, int tab, int type, int page, int pages) {
        final OutPacket p = OutPacket.create(SendOpcode.MTS_OPERATION);
        p.writeByte(0x15);
        p.writeInt(pages * 16);
        p.writeInt(items.size());
        p.writeInt(tab);
        p.writeInt(type);
        p.writeInt(page);
        p.writeByte(1);
        p.writeByte(1);
        for (MTSItemInfo item : items) {
            addItemInfo(p, item.getItem(), true);
            p.writeInt(item.getID());
            p.writeInt(item.getTaxes());
            p.writeInt(item.getPrice());
            p.writeInt(0);
            p.writeLong(getTime(item.getEndingDate()));
            p.writeString(item.getSeller());
            p.writeString(item.getSeller());
            for (int j = 0; j < 28; j++) {
                p.writeByte(0);
            }
        }
        p.writeByte(1);
        return p;
    }

    public Packet useChalkboard(Character chr, boolean close) {
        OutPacket p = OutPacket.create(SendOpcode.CHALKBOARD);
        p.writeInt(chr.getId());
        if (close) {
            p.writeByte(0);
        } else {
            p.writeByte(1);
            p.writeString(chr.getChalkboard());
        }
        return p;
    }

    public Packet trockRefreshMapList(Character chr, boolean delete, boolean vip) {
        final OutPacket p = OutPacket.create(SendOpcode.MAP_TRANSFER_RESULT);
        p.writeByte(delete ? 2 : 3);
        if (vip) {
            p.writeByte(1);
            List<Integer> map = chr.getVipTrockMaps();
            for (int i = 0; i < 10; i++) {
                p.writeInt(map.get(i));
            }
        } else {
            p.writeByte(0);
            List<Integer> map = chr.getTrockMaps();
            for (int i = 0; i < 5; i++) {
                p.writeInt(map.get(i));
            }
        }
        return p;
    }

    public Packet sendWorldTransferRules(int error, Client c) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_CHECK_TRANSFER_WORLD_POSSIBLE_RESULT);
        p.writeInt(0);
        p.writeByte(error);
        p.writeInt(0);
        p.writeBool(error == 0);
        if (error == 0) {
            List<World> worlds = Server.getInstance().getWorlds();
            p.writeInt(worlds.size());
            for (World world : worlds) {
                p.writeString(GameConstants.WORLD_NAMES[world.getId()]);
            }
        }
        return p;
    }

    public Packet showWorldTransferSuccess(Item item, int accountId) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);
        p.writeByte(0xA0);
        addCashItemInformation(p, item, accountId);
        return p;
    }

    public Packet sendNameTransferRules(int error) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_CHECK_NAME_CHANGE_POSSIBLE_RESULT);
        p.writeInt(0);
        p.writeByte(error);
        p.writeInt(0);

        return p;
    }

    public Packet sendNameTransferCheck(String availableName, boolean canUseName) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_CHECK_NAME_CHANGE);

        p.writeString(availableName);
        p.writeBool(!canUseName);
        return p;
    }

    public Packet showNameChangeSuccess(Item item, int accountId) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);
        p.writeByte(0x9E);
        addCashItemInformation(p, item, accountId);
        return p;
    }

    public Packet showNameChangeCancel(boolean success) {
        OutPacket p = OutPacket.create(SendOpcode.CANCEL_NAME_CHANGE_RESULT);
        p.writeBool(success);
        if (!success) {
            p.writeByte(0);
        }

        return p;
    }

    public Packet showWorldTransferCancel(boolean success) {
        OutPacket p = OutPacket.create(SendOpcode.CANCEL_TRANSFER_WORLD_RESULT);
        p.writeBool(success);
        if (!success) {
            p.writeByte(0);
        }

        return p;
    }

    public Packet showMTSCash(Character chr) {
        final OutPacket p = OutPacket.create(SendOpcode.MTS_OPERATION2);
        p.writeInt(chr.getCashShop().getCash(CashShop.NX_PREPAID));
        p.writeInt(chr.getCashShop().getCash(CashShop.MAPLE_POINT));
        return p;
    }

    public Packet MTSWantedListingOver(int nx, int items) {
        final OutPacket p = OutPacket.create(SendOpcode.MTS_OPERATION);
        p.writeByte(0x3D);
        p.writeInt(nx);
        p.writeInt(items);
        return p;
    }

    public Packet MTSConfirmSell() {
        final OutPacket p = OutPacket.create(SendOpcode.MTS_OPERATION);
        p.writeByte(0x1D);
        return p;
    }

    public Packet MTSConfirmBuy() {
        final OutPacket p = OutPacket.create(SendOpcode.MTS_OPERATION);
        p.writeByte(0x33);
        return p;
    }

    public Packet MTSFailBuy() {
        final OutPacket p = OutPacket.create(SendOpcode.MTS_OPERATION);
        p.writeByte(0x34);
        p.writeByte(0x42);
        return p;
    }

    public Packet MTSConfirmTransfer(int quantity, int pos) {
        final OutPacket p = OutPacket.create(SendOpcode.MTS_OPERATION);
        p.writeByte(0x27);
        p.writeInt(quantity);
        p.writeInt(pos);
        return p;
    }

    public Packet notYetSoldInv(List<MTSItemInfo> items) {
        final OutPacket p = OutPacket.create(SendOpcode.MTS_OPERATION);
        p.writeByte(0x23);
        p.writeInt(items.size());
        if (!items.isEmpty()) {
            for (MTSItemInfo item : items) {
                addItemInfo(p, item.getItem(), true);
                p.writeInt(item.getID());
                p.writeInt(item.getTaxes());
                p.writeInt(item.getPrice());
                p.writeInt(0);
                p.writeLong(getTime(item.getEndingDate()));
                p.writeString(item.getSeller());
                p.writeString(item.getSeller());
                for (int i = 0; i < 28; i++) {
                    p.writeByte(0);
                }
            }
        } else {
            p.writeInt(0);
        }
        return p;
    }

    public Packet transferInventory(List<MTSItemInfo> items) {
        final OutPacket p = OutPacket.create(SendOpcode.MTS_OPERATION);
        p.writeByte(0x21);
        p.writeInt(items.size());
        if (!items.isEmpty()) {
            for (MTSItemInfo item : items) {
                addItemInfo(p, item.getItem(), true);
                p.writeInt(item.getID());
                p.writeInt(item.getTaxes());
                p.writeInt(item.getPrice());
                p.writeInt(0);
                p.writeLong(getTime(item.getEndingDate()));
                p.writeString(item.getSeller());
                p.writeString(item.getSeller());
                for (int i = 0; i < 28; i++) {
                    p.writeByte(0);
                }
            }
        }
        p.writeByte(0xD0 + items.size());
        p.writeBytes(new byte[]{-1, -1, -1, 0});
        return p;
    }

    public Packet showCouponRedeemedItems(int accountId, int maplePoints, int mesos, List<Item> cashItems, List<Pair<Integer, Integer>> items) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);
        p.writeByte(0x59);
        p.writeByte((byte) cashItems.size());
        for (Item item : cashItems) {
            addCashItemInformation(p, item, accountId);
        }
        p.writeInt(maplePoints);
        p.writeInt(items.size());
        for (Pair<Integer, Integer> itemPair : items) {
            int quantity = itemPair.getLeft();
            p.writeShort((short) quantity);
            p.writeShort(0x1F);
            p.writeInt(itemPair.getRight());
        }
        p.writeInt(mesos);
        return p;
    }

    public Packet showCash(Character mc) {
        final OutPacket p = OutPacket.create(SendOpcode.QUERY_CASH_RESULT);
        p.writeInt(mc.getCashShop().getCash(CashShop.NX_CREDIT));
        p.writeInt(mc.getCashShop().getCash(CashShop.MAPLE_POINT));
        p.writeInt(mc.getCashShop().getCash(CashShop.NX_PREPAID));
        return p;
    }

    public Packet enableCSUse(Character mc) {
        return showCash(mc);
    }

    public Packet getFindResult(Character target, byte type, int fieldOrChannel, byte flag) {
        OutPacket p = OutPacket.create(SendOpcode.WHISPER);

        p.writeByte(flag | WhisperFlag.RESULT);
        p.writeString(target.getName());
        p.writeByte(type);
        p.writeInt(fieldOrChannel);

        if (type == WhisperHandler.RT_SAME_CHANNEL) {
            p.writeInt(target.getPosition().x);
            p.writeInt(target.getPosition().y);
        }

        return p;
    }

    public Packet getWhisperResult(String target, boolean success) {
        OutPacket p = OutPacket.create(SendOpcode.WHISPER);
        p.writeByte(WhisperFlag.WHISPER | WhisperFlag.RESULT);
        p.writeString(target);
        p.writeBool(success);
        return p;
    }

    public Packet getWhisperReceive(String sender, int channel, boolean fromAdmin, String message) {
        OutPacket p = OutPacket.create(SendOpcode.WHISPER);
        p.writeByte(WhisperFlag.WHISPER | WhisperFlag.RECEIVE);
        p.writeString(sender);
        p.writeByte(channel);
        p.writeBool(fromAdmin);
        p.writeString(message);
        return p;
    }

    public Packet sendAutoHpPot(int itemId) {
        final OutPacket p = OutPacket.create(SendOpcode.AUTO_HP_POT);
        p.writeInt(itemId);
        return p;
    }

    public Packet sendAutoMpPot(int itemId) {
        OutPacket p = OutPacket.create(SendOpcode.AUTO_MP_POT);
        p.writeInt(itemId);
        return p;
    }

    public Packet showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
        OutPacket p = OutPacket.create(SendOpcode.OX_QUIZ);
        p.writeByte(askQuestion ? 1 : 0);
        p.writeByte(questionSet);
        p.writeShort(questionId);
        return p;
    }

    public Packet updateGender(Character chr) {
        OutPacket p = OutPacket.create(SendOpcode.SET_GENDER);
        p.writeByte(chr.getGender());
        return p;
    }

    public Packet enableReport() {
        OutPacket p = OutPacket.create(SendOpcode.CLAIM_STATUS_CHANGED);
        p.writeByte(1);
        return p;
    }

    public Packet loadFamily(Character player) {
        final OutPacket p = OutPacket.create(SendOpcode.FAMILY_PRIVILEGE_LIST);
        p.writeInt(FamilyEntitlement.values().length);
        for (int i = 0; i < FamilyEntitlement.values().length; i++) {
            FamilyEntitlement entitlement = FamilyEntitlement.values()[i];
            p.writeByte(i <= 1 ? 1 : 2);
            p.writeInt(entitlement.getRepCost());
            p.writeInt(entitlement.getUsageLimit());
            p.writeString(entitlement.getName());
            p.writeString(entitlement.getDescription());
        }
        return p;
    }

    public Packet sendFamilyMessage(int type, int mesos) {
        OutPacket p = OutPacket.create(SendOpcode.FAMILY_RESULT);
        p.writeInt(type);
        p.writeInt(mesos);
        return p;
    }

    public Packet getFamilyInfo(FamilyEntry f) {
        if (f == null) {
            return getEmptyFamilyInfo();
        }

        OutPacket p = OutPacket.create(SendOpcode.FAMILY_INFO_RESULT);
        p.writeInt(f.getReputation());
        p.writeInt(f.getTotalReputation());
        p.writeInt(f.getTodaysRep());
        p.writeShort(f.getJuniorCount());
        p.writeShort(2);
        p.writeShort(0);
        p.writeInt(f.getFamily().getLeader().getChrId());
        p.writeString(f.getFamily().getName());
        p.writeString(f.getFamily().getMessage());
        p.writeInt(FamilyEntitlement.values().length);
        for (FamilyEntitlement entitlement : FamilyEntitlement.values()) {
            p.writeInt(entitlement.ordinal());
            p.writeInt(f.isEntitlementUsed(entitlement) ? 1 : 0);
        }
        return p;
    }

    private Packet getEmptyFamilyInfo() {
        OutPacket p = OutPacket.create(SendOpcode.FAMILY_INFO_RESULT);
        p.writeInt(0);
        p.writeInt(0);
        p.writeInt(0);
        p.writeShort(0);
        p.writeShort(2);
        p.writeShort(0);
        p.writeInt(0);
        p.writeString("");
        p.writeString("");
        p.writeInt(0);
        return p;
    }

    public Packet showPedigree(FamilyEntry entry) {
        final OutPacket p = OutPacket.create(SendOpcode.FAMILY_CHART_RESULT);
        p.writeInt(entry.getChrId());
        List<FamilyEntry> superJuniors = new ArrayList<>(4);
        boolean hasOtherJunior = false;
        int entryCount = 2;
        entryCount += Math.min(2, entry.getTotalSeniors());

        if (entry.getSenior() != null) {
            if (entry.getSenior().getJuniorCount() == 2) {
                entryCount++;
                hasOtherJunior = true;
            }
        }
        for (FamilyEntry junior : entry.getJuniors()) {
            if (junior == null) {
                continue;
            }
            entryCount++;
            for (FamilyEntry superJunior : junior.getJuniors()) {
                if (superJunior == null) {
                    continue;
                }
                entryCount++;
                superJuniors.add(superJunior);
            }
        }

        boolean missingEntries = entryCount == 2;
        if (missingEntries) {
            entryCount++;
        }
        p.writeInt(entryCount);
        addPedigreeEntry(p, entry.getFamily().getLeader());
        if (entry.getSenior() != null) {
            if (entry.getSenior().getSenior() != null) {
                addPedigreeEntry(p, entry.getSenior().getSenior());
            }
            addPedigreeEntry(p, entry.getSenior());
        }
        addPedigreeEntry(p, entry);
        if (hasOtherJunior) {
            FamilyEntry otherJunior = entry.getSenior().getOtherJunior(entry);
            if (otherJunior != null) {
                addPedigreeEntry(p, otherJunior);
            }
        }
        if (missingEntries) {
            addPedigreeEntry(p, entry);
        }
        for (FamilyEntry junior : entry.getJuniors()) {
            if (junior == null) {
                continue;
            }
            addPedigreeEntry(p, junior);
            for (FamilyEntry superJunior : junior.getJuniors()) {
                if (superJunior != null) {
                    addPedigreeEntry(p, superJunior);
                }
            }
        }
        p.writeInt(2 + superJuniors.size());

        p.writeInt(-1);
        p.writeInt(entry.getFamily().getTotalMembers());
        p.writeInt(0);
        p.writeInt(entry.getTotalSeniors());
        for (FamilyEntry superJunior : superJuniors) {
            p.writeInt(superJunior.getChrId());
            p.writeInt(superJunior.getTotalJuniors());
        }
        p.writeInt(0);


        p.writeShort(entry.getJuniorCount() >= 2 ? 0 : 2);
        return p;
    }

    private void addPedigreeEntry(OutPacket p, FamilyEntry entry) {
        Character chr = entry.getChr();
        boolean isOnline = chr != null;
        p.writeInt(entry.getChrId());
        p.writeInt(entry.getSenior() != null ? entry.getSenior().getChrId() : 0);
        p.writeShort(entry.getJob().getId());
        p.writeByte(entry.getLevel());
        p.writeBool(isOnline);
        p.writeInt(entry.getReputation());
        p.writeInt(entry.getTotalReputation());
        p.writeInt(entry.getRepsToSenior());
        p.writeInt(entry.getTodaysRep());
        p.writeInt(isOnline ? ((chr.isAwayFromWorld() || chr.getCashShop().isOpened()) ? -1 : chr.getClient().getChannel() - 1) : 0);
        p.writeInt(isOnline ? (int) (chr.getLoggedInTime() / 60000) : 0);
        p.writeString(entry.getName());
    }

    public Packet updateAreaInfo(int area, String info) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_STATUS_INFO);
        p.writeByte(0x0A);
        p.writeShort(area);
        p.writeString(info);
        return p;
    }

    public Packet getGPMessage(int gpChange) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_STATUS_INFO);
        p.writeByte(6);
        p.writeInt(gpChange);
        return p;
    }

    public Packet getItemMessage(int itemid) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_STATUS_INFO);
        p.writeByte(7);
        p.writeInt(itemid);
        return p;
    }

    public Packet addCard(boolean full, int cardid, int level) {
        OutPacket p = OutPacket.create(SendOpcode.MONSTER_BOOK_SET_CARD);
        p.writeByte(full ? 0 : 1);
        p.writeInt(cardid);
        p.writeInt(level);
        return p;
    }

    public Packet showGainCard() {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_ITEM_GAIN_INCHAT);
        p.writeByte(0x0D);
        return p;
    }

    public Packet showForeignCardEffect(int id) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_FOREIGN_EFFECT);
        p.writeInt(id);
        p.writeByte(0x0D);
        return p;
    }

    public Packet changeCover(int cardid) {
        OutPacket p = OutPacket.create(SendOpcode.MONSTER_BOOK_SET_COVER);
        p.writeInt(cardid);
        return p;
    }

    public Packet aranGodlyStats() {
        OutPacket p = OutPacket.create(SendOpcode.FORCED_STAT_SET);
        p.writeBytes(new byte[]{
                (byte) 0x1F, (byte) 0x0F, 0, 0,
                (byte) 0xE7, 3, (byte) 0xE7, 3,
                (byte) 0xE7, 3, (byte) 0xE7, 3,
                (byte) 0xFF, 0, (byte) 0xE7, 3,
                (byte) 0xE7, 3, (byte) 0x78, (byte) 0x8C});
        return p;
    }

    public Packet showIntro(String path) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_ITEM_GAIN_INCHAT);
        p.writeByte(0x12);
        p.writeString(path);
        return p;
    }

    public Packet showInfo(String path) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_ITEM_GAIN_INCHAT);
        p.writeByte(0x17);
        p.writeString(path);
        p.writeInt(1);
        return p;
    }

    public Packet showForeignInfo(int cid, String path) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_FOREIGN_EFFECT);
        p.writeInt(cid);
        p.writeByte(0x17);
        p.writeString(path);
        p.writeInt(1);
        return p;
    }

    public Packet openUI(byte ui) {
        OutPacket p = OutPacket.create(SendOpcode.OPEN_UI);
        p.writeByte(ui);
        return p;
    }

    public Packet lockUI(boolean enable) {
        OutPacket p = OutPacket.create(SendOpcode.LOCK_UI);
        p.writeByte(enable ? 1 : 0);
        return p;
    }

    public Packet disableUI(boolean enable) {
        final OutPacket p = OutPacket.create(SendOpcode.DISABLE_UI);
        p.writeByte(enable ? 1 : 0);
        return p;
    }

    public Packet itemMegaphone(String msg, boolean whisper, int channel, Item item) {
        final OutPacket p = OutPacket.create(SendOpcode.SERVERMESSAGE);
        p.writeByte(8);
        p.writeString(msg);
        p.writeByte(channel - 1);
        p.writeByte(whisper ? 1 : 0);
        if (item == null) {
            p.writeByte(0);
        } else {
            p.writeByte(item.getPosition());
            addItemInfo(p, item, true);
        }
        return p;
    }

    public Packet removeNPC(int objId) {
        OutPacket p = OutPacket.create(SendOpcode.REMOVE_NPC);
        p.writeInt(objId);
        return p;
    }

    public Packet removeNPCController(int objId) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER);
        p.writeByte(0);
        p.writeInt(objId);
        return p;
    }

    public Packet reportResponse(byte mode) {
        final OutPacket p = OutPacket.create(SendOpcode.SUE_CHARACTER_RESULT);
        p.writeByte(mode);
        return p;
    }

    public Packet sendHammerData(int hammerUsed) {
        OutPacket p = OutPacket.create(SendOpcode.VICIOUS_HAMMER);
        p.writeByte(0x39);
        p.writeInt(0);
        p.writeInt(hammerUsed);
        return p;
    }

    public Packet sendHammerMessage() {
        final OutPacket p = OutPacket.create(SendOpcode.VICIOUS_HAMMER);
        p.writeByte(0x3D);
        p.writeInt(0);
        return p;
    }

    public Packet playPortalSound() {
        return showSpecialEffect(7);
    }

    public Packet showEquipmentLevelUp() {
        return showSpecialEffect(15);
    }

    public Packet showSpecialEffect(int effect) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_ITEM_GAIN_INCHAT);
        p.writeByte(effect);
        return p;
    }

    public Packet showMakerEffect(boolean makerSucceeded) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_ITEM_GAIN_INCHAT);
        p.writeByte(16);
        p.writeInt(makerSucceeded ? 0 : 1);
        return p;
    }

    public Packet showForeignMakerEffect(int cid, boolean makerSucceeded) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_FOREIGN_EFFECT);
        p.writeInt(cid);
        p.writeByte(16);
        p.writeInt(makerSucceeded ? 0 : 1);
        return p;
    }

    public Packet showForeignEffect(int chrId, int effect) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_FOREIGN_EFFECT);
        p.writeInt(chrId);
        p.writeByte(effect);
        return p;
    }

    public Packet showOwnRecovery(byte heal) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_ITEM_GAIN_INCHAT);
        p.writeByte(0x0A);
        p.writeByte(heal);
        return p;
    }

    public Packet showRecovery(int chrId, byte amount) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_FOREIGN_EFFECT);
        p.writeInt(chrId);
        p.writeByte(0x0A);
        p.writeByte(amount);
        return p;
    }

    public Packet showWheelsLeft(int left) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_ITEM_GAIN_INCHAT);
        p.writeByte(0x15);
        p.writeByte(left);
        return p;
    }

    public Packet updateQuestFinish(short quest, int npc, short nextquest) {
        final OutPacket p = OutPacket.create(SendOpcode.UPDATE_QUEST_INFO);
        p.writeByte(8);
        p.writeShort(quest);
        p.writeInt(npc);
        p.writeShort(nextquest);
        return p;
    }

    public Packet showInfoText(String text) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_STATUS_INFO);
        p.writeByte(9);
        p.writeString(text);
        return p;
    }

    public Packet questExpire(short quest) {
        final OutPacket p = OutPacket.create(SendOpcode.UPDATE_QUEST_INFO);
        p.writeByte(0x0F);
        p.writeShort(quest);
        return p;
    }

    public Packet makerResult(boolean success, int itemMade, int itemCount, int mesos, List<Pair<Integer, Integer>> itemsLost, int catalystID, List<Integer> INCBuffGems) {
        final OutPacket p = OutPacket.create(SendOpcode.MAKER_RESULT);
        p.writeInt(success ? 0 : 1);
        p.writeInt(1);
        p.writeBool(!success);
        if (success) {
            p.writeInt(itemMade);
            p.writeInt(itemCount);
        }
        p.writeInt(itemsLost.size());
        for (Pair<Integer, Integer> item : itemsLost) {
            p.writeInt(item.getLeft());
            p.writeInt(item.getRight());
        }
        p.writeInt(INCBuffGems.size());
        for (Integer gem : INCBuffGems) {
            p.writeInt(gem);
        }
        if (catalystID != -1) {
            p.writeByte(1);
            p.writeInt(catalystID);
        } else {
            p.writeByte(0);
        }

        p.writeInt(mesos);
        return p;
    }

    public Packet makerResultCrystal(int itemIdGained, int itemIdLost) {
        final OutPacket p = OutPacket.create(SendOpcode.MAKER_RESULT);
        p.writeInt(0);
        p.writeInt(3);
        p.writeInt(itemIdGained);
        p.writeInt(itemIdLost);
        return p;
    }

    public Packet makerResultDesynth(int itemId, int mesos, List<Pair<Integer, Integer>> itemsGained) {
        final OutPacket p = OutPacket.create(SendOpcode.MAKER_RESULT);
        p.writeInt(0);
        p.writeInt(4);
        p.writeInt(itemId);
        p.writeInt(itemsGained.size());
        for (Pair<Integer, Integer> item : itemsGained) {
            p.writeInt(item.getLeft());
            p.writeInt(item.getRight());
        }
        p.writeInt(mesos);
        return p;
    }

    public Packet makerEnableActions() {
        final OutPacket p = OutPacket.create(SendOpcode.MAKER_RESULT);
        p.writeInt(0);
        p.writeInt(0);
        p.writeInt(0);
        p.writeInt(0);
        return p;
    }

    public Packet getMultiMegaphone(String[] messages, int channel, boolean showEar) {
        final OutPacket p = OutPacket.create(SendOpcode.SERVERMESSAGE);
        p.writeByte(0x0A);
        if (messages[0] != null) {
            p.writeString(messages[0]);
        }
        p.writeByte(messages.length);
        for (int i = 1; i < messages.length; i++) {
            if (messages[i] != null) {
                p.writeString(messages[i]);
            }
        }
        for (int i = 0; i < 10; i++) {
            p.writeByte(channel - 1);
        }
        p.writeByte(showEar ? 1 : 0);
        p.writeByte(1);
        return p;
    }

    public Packet getGMEffect(int type, byte mode) {
        OutPacket p = OutPacket.create(SendOpcode.ADMIN_RESULT);
        p.writeByte(type);
        p.writeByte(mode);
        return p;
    }

    public Packet disableMinimap() {
        final OutPacket p = OutPacket.create(SendOpcode.ADMIN_RESULT);
        p.writeShort(0x1C);
        return p;
    }

    public Packet sendFamilyInvite(int playerId, String inviter) {
        final OutPacket p = OutPacket.create(SendOpcode.FAMILY_JOIN_REQUEST);
        p.writeInt(playerId);
        p.writeString(inviter);
        return p;
    }

    public Packet sendFamilySummonRequest(String familyName, String from) {
        final OutPacket p = OutPacket.create(SendOpcode.FAMILY_SUMMON_REQUEST);
        p.writeString(from);
        p.writeString(familyName);
        return p;
    }

    public Packet sendFamilyLoginNotice(String name, boolean loggedIn) {
        final OutPacket p = OutPacket.create(SendOpcode.FAMILY_NOTIFY_LOGIN_OR_LOGOUT);
        p.writeBool(loggedIn);
        p.writeString(name);
        return p;
    }

    public Packet sendFamilyJoinResponse(boolean accepted, String added) {
        final OutPacket p = OutPacket.create(SendOpcode.FAMILY_JOIN_REQUEST_RESULT);
        p.writeByte(accepted ? 1 : 0);
        p.writeString(added);
        return p;
    }

    public Packet getSeniorMessage(String name) {
        final OutPacket p = OutPacket.create(SendOpcode.FAMILY_JOIN_ACCEPTED);
        p.writeString(name);
        p.writeInt(0);
        return p;
    }

    public Packet sendGainRep(int gain, String from) {
        final OutPacket p = OutPacket.create(SendOpcode.FAMILY_REP_GAIN);
        p.writeInt(gain);
        p.writeString(from);
        return p;
    }

    public Packet showBoughtCashPackage(List<Item> cashPackage, int accountId) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);

        p.writeByte(0x89);
        p.writeByte(cashPackage.size());

        for (Item item : cashPackage) {
            addCashItemInformation(p, item, accountId);
        }

        p.writeShort(0);

        return p;
    }

    public Packet showBoughtQuestItem(int itemId) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);
        p.writeByte(0x8D);
        p.writeInt(1);
        p.writeShort(1);
        p.writeByte(0x0B);
        p.writeByte(0);
        p.writeInt(itemId);
        return p;
    }

    public Packet onCashItemGachaponOpenFailed() {
        OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_CASH_ITEM_GACHAPON_RESULT);
        p.writeByte(0xE4);
        return p;
    }

    public Packet onCashGachaponOpenSuccess(int accountid, long boxCashId, int remainingBoxes, Item reward,
                                            int rewardItemId, int rewardQuantity, boolean bJackpot) {
        OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_CASH_ITEM_GACHAPON_RESULT);
        p.writeByte(0xE5);
        p.writeLong(boxCashId);
        p.writeInt(remainingBoxes);
        addCashItemInformation(p, reward, accountid);
        p.writeInt(rewardItemId);
        p.writeByte(rewardQuantity);
        p.writeBool(bJackpot);
        return p;
    }

    public Packet removeItemFromDuey(boolean remove, int Package) {
        final OutPacket p = OutPacket.create(SendOpcode.PARCEL);
        p.writeByte(0x17);
        p.writeInt(Package);
        p.writeByte(remove ? 3 : 4);
        return p;
    }

    public Packet sendDueyParcelReceived(String from, boolean quick) {
        OutPacket p = OutPacket.create(SendOpcode.PARCEL);
        p.writeByte(0x19);
        p.writeString(from);
        p.writeBool(quick);
        return p;
    }

    public Packet sendDueyParcelNotification(boolean quick) {
        final OutPacket p = OutPacket.create(SendOpcode.PARCEL);
        p.writeByte(0x1B);
        p.writeBool(quick);
        return p;
    }

    public Packet sendDueyMSG(byte operation) {
        return sendDuey(operation, null);
    }

    public Packet sendDuey(int operation, List<DueyPackage> packages) {
        final OutPacket p = OutPacket.create(SendOpcode.PARCEL);
        p.writeByte(operation);
        if (operation == 8) {
            p.writeByte(0);
            p.writeByte(packages.size());
            for (DueyPackage dp : packages) {
                p.writeInt(dp.getPackageId());
                p.writeFixedString(dp.getSender());
                for (int i = dp.getSender().length(); i < 13; i++) {
                    p.writeByte(0);
                }

                p.writeInt(dp.getMesos());
                p.writeLong(getTime(dp.sentTimeInMilliseconds()));

                String msg = dp.getMessage();
                if (msg != null) {
                    p.writeInt(1);
                    p.writeFixedString(msg);
                    for (int i = msg.length(); i < 200; i++) {
                        p.writeByte(0);
                    }
                } else {
                    p.writeInt(0);
                    p.skip(200);
                }

                p.writeByte(0);
                if (dp.getItem() != null) {
                    p.writeByte(1);
                    addItemInfo(p, dp.getItem(), true);
                } else {
                    p.writeByte(0);
                }
            }
            p.writeByte(0);
        }

        return p;
    }

    public Packet blockedMessage(int type) {
        final OutPacket p = OutPacket.create(SendOpcode.BLOCKED_MAP);
        p.writeByte(type);
        return p;
    }

    public Packet blockedMessage2(int type) {
        final OutPacket p = OutPacket.create(SendOpcode.BLOCKED_SERVER);
        p.writeByte(type);
        return p;
    }

    public Packet levelUpMessage(int type, int level, String charname) {
        final OutPacket p = OutPacket.create(SendOpcode.NOTIFY_LEVELUP);
        p.writeByte(type);
        p.writeInt(level);
        p.writeString(charname);

        return p;
    }

    public Packet marriageMessage(int type, String charname) {
        final OutPacket p = OutPacket.create(SendOpcode.NOTIFY_MARRIAGE);
        p.writeByte(type);
        p.writeString("> " + charname);

        return p;
    }

    public Packet jobMessage(int type, int job, String charname) {
        OutPacket p = OutPacket.create(SendOpcode.NOTIFY_JOB_CHANGE);
        p.writeByte(type);
        p.writeInt(job);
        p.writeString("> " + charname);
        return p;
    }

    public Packet getEnergy(String info, int amount) {
        final OutPacket p = OutPacket.create(SendOpcode.SESSION_VALUE);
        p.writeString(info);
        p.writeString(Integer.toString(amount));
        return p;
    }

    public Packet dojoWarpUp() {
        final OutPacket p = OutPacket.create(SendOpcode.DOJO_WARP_UP);
        p.writeByte(0);
        p.writeByte(6);
        return p;
    }

    public Packet itemExpired(int itemid) {
        final OutPacket p = OutPacket.create(SendOpcode.SHOW_STATUS_INFO);
        p.writeByte(2);
        p.writeInt(itemid);
        return p;
    }

    private String getRightPaddedStr(String in, char padchar, int length) {
        StringBuilder builder = new StringBuilder(in);
        for (int x = in.length(); x < length; x++) {
            builder.append(padchar);
        }
        return builder.toString();
    }

    public Packet MobDamageMobFriendly(Monster mob, int damage, int remainingHp) {
        final OutPacket p = OutPacket.create(SendOpcode.DAMAGE_MONSTER);
        p.writeInt(mob.getObjectId());
        p.writeByte(1);
        p.writeInt(damage);
        p.writeInt(remainingHp);
        p.writeInt(mob.getMaxHp());
        return p;
    }

    public Packet shopErrorMessage(int error, int type) {
        final OutPacket p = OutPacket.create(SendOpcode.PLAYER_INTERACTION);
        p.writeByte(0x0A);
        p.writeByte(type);
        p.writeByte(error);
        return p;
    }

    private void addRingInfo(OutPacket p, Character chr) {
        p.writeShort(0);
        p.writeShort(0);
        p.writeShort(0);
    }

    public Packet finishedSort(int inv) {
        OutPacket p = OutPacket.create(SendOpcode.GATHER_ITEM_RESULT);
        p.writeByte(0);
        p.writeByte(inv);
        return p;
    }

    public Packet finishedSort2(int inv) {
        OutPacket p = OutPacket.create(SendOpcode.SORT_ITEM_RESULT);
        p.writeByte(0);
        p.writeByte(inv);
        return p;
    }

    public Packet leftKnockBack() {
        return OutPacket.create(SendOpcode.LEFT_KNOCK_BACK);
    }

    public Packet rollSnowBall(boolean entermap, int state, Snowball ball0, Snowball ball1) {
        OutPacket p = OutPacket.create(SendOpcode.SNOWBALL_STATE);
        if (entermap) {
            p.skip(21);
        } else {
            p.writeByte(state);
            p.writeInt(ball0.getSnowmanHP() / 75);
            p.writeInt(ball1.getSnowmanHP() / 75);
            p.writeShort(ball0.getPosition());
            p.writeByte(-1);
            p.writeShort(ball1.getPosition());
            p.writeByte(-1);
        }
        return p;
    }

    public Packet hitSnowBall(int what, int damage) {
        OutPacket p = OutPacket.create(SendOpcode.HIT_SNOWBALL);
        p.writeByte(what);
        p.writeInt(damage);
        return p;
    }

    public Packet snowballMessage(int team, int message) {
        OutPacket p = OutPacket.create(SendOpcode.SNOWBALL_MESSAGE);
        p.writeByte(team);
        p.writeInt(message);
        return p;
    }

    public Packet coconutScore(int team1, int team2) {
        OutPacket p = OutPacket.create(SendOpcode.COCONUT_SCORE);
        p.writeShort(team1);
        p.writeShort(team2);
        return p;
    }

    public Packet hitCoconut(boolean spawn, int id, int type) {
        OutPacket p = OutPacket.create(SendOpcode.COCONUT_HIT);
        if (spawn) {
            p.writeShort(-1);
            p.writeShort(5000);
            p.writeByte(0);
        } else {
            p.writeShort(id);
            p.writeShort(1000);
            p.writeByte(type);
        }
        return p;
    }

    public Packet customPacket(byte[] packet) {
        OutPacket p = new ByteBufOutPacket();
        p.writeBytes(packet);
        return p;
    }

    public Packet spawnGuide(boolean spawn) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_GUIDE);
        p.writeBool(spawn);
        return p;
    }

    public Packet talkGuide(String talk) {
        final OutPacket p = OutPacket.create(SendOpcode.TALK_GUIDE);
        p.writeByte(0);
        p.writeString(talk);
        p.writeBytes(new byte[]{(byte) 0xC8, 0, 0, 0, (byte) 0xA0, (byte) 0x0F, 0, 0});
        return p;
    }

    public Packet guideHint(int hint) {
        OutPacket p = OutPacket.create(SendOpcode.TALK_GUIDE);
        p.writeByte(1);
        p.writeInt(hint);
        p.writeInt(7000);
        return p;
    }

    public void addCashItemInformation(OutPacket p, Item item, int accountId) {
        addCashItemInformation(p, item, accountId, null);
    }

    public void addCashItemInformation(OutPacket p, Item item, int accountId, String giftMessage) {
        boolean isGift = giftMessage != null;
        boolean isRing = false;
        Equip equip = null;
        if (item.getInventoryType().equals(InventoryType.EQUIP)) {
            equip = (Equip) item;
            isRing = equip.getRingId() > -1;
        }
        p.writeLong(item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
        if (!isGift) {
            p.writeInt(accountId);
            p.writeInt(0);
        }
        p.writeInt(item.getItemId());
        if (!isGift) {
            p.writeInt(item.getSN());
            p.writeShort(item.getQuantity());
        }
        p.writeFixedString(StringUtil.getRightPaddedStr(item.getGiftFrom(), '\0', 13));
        if (isGift) {
            p.writeFixedString(StringUtil.getRightPaddedStr(giftMessage, '\0', 73));
            return;
        }
        addExpirationTime(p, item.getExpiration());
        p.writeLong(0);
    }

    public Packet showWishList(Character mc, boolean update) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);

        if (update) {
            p.writeByte(0x55);
        } else {
            p.writeByte(0x4F);
        }

        for (int sn : mc.getCashShop().getWishList()) {
            p.writeInt(sn);
        }

        for (int i = mc.getCashShop().getWishList().size(); i < 10; i++) {
            p.writeInt(0);
        }

        return p;
    }

    public Packet showBoughtCashItem(Item item, int accountId) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);

        p.writeByte(0x57);
        addCashItemInformation(p, item, accountId);

        return p;
    }

    public Packet showBoughtCashRing(Item ring, String recipient, int accountId) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);
        p.writeByte(0x87);
        addCashItemInformation(p, ring, accountId);
        p.writeString(recipient);
        p.writeInt(ring.getItemId());
        p.writeShort(1);
        return p;
    }

    public Packet showCashShopMessage(byte message) {
        OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);
        p.writeByte(0x5C);
        p.writeByte(message);
        return p;
    }

    public Packet showCashInventory(Client c) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);

        p.writeByte(0x4B);
        p.writeShort(c.getPlayer().getCashShop().getInventory().size());

        for (Item item : c.getPlayer().getCashShop().getInventory()) {
            addCashItemInformation(p, item, c.getAccID());
        }

        p.writeShort(c.getPlayer().getStorage().getSlots());
        p.writeShort(c.getCharacterSlots());

        return p;
    }

    public Packet showGifts(List<Pair<Item, String>> gifts) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);

        p.writeByte(0x4D);
        p.writeShort(gifts.size());

        for (Pair<Item, String> gift : gifts) {
            addCashItemInformation(p, gift.getLeft(), 0, gift.getRight());
        }

        return p;
    }

    public Packet showGiftSucceed(String to, CashItem item) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);

        p.writeByte(0x5E);
        p.writeString(to);
        p.writeInt(item.getItemId());
        p.writeShort(item.getCount());
        p.writeInt(item.getPrice());

        return p;
    }

    public Packet showBoughtInventorySlots(int type, short slots) {
        OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);

        p.writeByte(0x60);
        p.writeByte(type);
        p.writeShort(slots);

        return p;
    }

    public Packet showBoughtStorageSlots(short slots) {
        OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);

        p.writeByte(0x62);
        p.writeShort(slots);

        return p;
    }

    public Packet showBoughtCharacterSlot(short slots) {
        OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);

        p.writeByte(0x64);
        p.writeShort(slots);

        return p;
    }

    public Packet takeFromCashInventory(Item item) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);

        p.writeByte(0x68);
        p.writeShort(item.getPosition());
        addItemInfo(p, item, true);

        return p;
    }

    public Packet putIntoCashInventory(Item item, int accountId) {
        final OutPacket p = OutPacket.create(SendOpcode.CASHSHOP_OPERATION);

        p.writeByte(0x6A);
        addCashItemInformation(p, item, accountId);

        return p;
    }

    public Packet openCashShop(Client c, boolean mts) throws Exception {
        final OutPacket p = OutPacket.create(mts ? SendOpcode.SET_ITC : SendOpcode.SET_CASH_SHOP);

        addCharacterInfo(p, c.getPlayer());

        if (!mts) {
            p.writeByte(1);
        }

        p.writeString(c.getAccountName());
        if (mts) {
            p.writeBytes(new byte[]{(byte) 0x88, 19, 0, 0,
                    7, 0, 0, 0,
                    (byte) 0xF4, 1, 0, 0,
                    (byte) 0x18, 0, 0, 0,
                    (byte) 0xA8, 0, 0, 0,
                    (byte) 0x70, (byte) 0xAA, (byte) 0xA7, (byte) 0xC5,
                    (byte) 0x4E, (byte) 0xC1, (byte) 0xCA, 1});
        } else {
            p.writeInt(0);
            List<SpecialCashItem> lsci = CashItemFactory.getSpecialCashItems();
            p.writeShort(lsci.size());
            for (SpecialCashItem sci : lsci) {
                p.writeInt(sci.getSN());
                p.writeInt(sci.getModifier());
                p.writeByte(sci.getInfo());
            }
            p.skip(121);

            List<List<Integer>> mostSellers = c.getWorldServer().getMostSellerCashItems();
            for (int i = 1; i <= 8; i++) {
                List<Integer> mostSellersTab = mostSellers.get(i);

                for (int j = 0; j < 2; j++) {
                    for (Integer snid : mostSellersTab) {
                        p.writeInt(i);
                        p.writeInt(j);
                        p.writeInt(snid);
                    }
                }
            }

            p.writeInt(0);
            p.writeShort(0);
            p.writeByte(0);
            p.writeInt(75);
        }
        return p;
    }

    public Packet sendVegaScroll(int op) {
        OutPacket p = OutPacket.create(SendOpcode.VEGA_SCROLL);
        p.writeByte(op);
        return p;
    }

    public Packet resetForcedStats() {
        return OutPacket.create(SendOpcode.FORCED_STAT_RESET);
    }

    public Packet showCombo(int count) {
        OutPacket p = OutPacket.create(SendOpcode.SHOW_COMBO);
        p.writeInt(count);
        return p;
    }

    public Packet earnTitleMessage(String msg) {
        final OutPacket p = OutPacket.create(SendOpcode.SCRIPT_PROGRESS_MESSAGE);
        p.writeString(msg);
        return p;
    }

    public Packet CPUpdate(boolean party, int curCP, int totalCP, int team) {
        final OutPacket p;
        if (!party) {
            p = OutPacket.create(SendOpcode.MONSTER_CARNIVAL_OBTAINED_CP);
        } else {
            p = OutPacket.create(SendOpcode.MONSTER_CARNIVAL_PARTY_CP);
            p.writeByte(team);
        }
        p.writeShort(curCP);
        p.writeShort(totalCP);
        return p;
    }

    public Packet CPQMessage(byte message) {
        OutPacket p = OutPacket.create(SendOpcode.MONSTER_CARNIVAL_MESSAGE);
        p.writeByte(message);
        return p;
    }

    public Packet playerSummoned(String name, int tab, int number) {
        OutPacket p = OutPacket.create(SendOpcode.MONSTER_CARNIVAL_SUMMON);
        p.writeByte(tab);
        p.writeByte(number);
        p.writeString(name);
        return p;
    }

    public Packet playerDiedMessage(String name, int lostCP, int team) {
        OutPacket p = OutPacket.create(SendOpcode.MONSTER_CARNIVAL_DIED);
        p.writeByte(team);
        p.writeString(name);
        p.writeByte(lostCP);
        return p;
    }

    public Packet startMonsterCarnival(Character chr, int team, int opposition) {
        OutPacket p = OutPacket.create(SendOpcode.MONSTER_CARNIVAL_START);
        p.writeByte(team);
        p.writeShort(chr.getCP());
        p.writeShort(chr.getTotalCP());
        p.writeShort(chr.getMonsterCarnival().getCP(team));
        p.writeShort(chr.getMonsterCarnival().getTotalCP(team));
        p.writeShort(chr.getMonsterCarnival().getCP(opposition));
        p.writeShort(chr.getMonsterCarnival().getTotalCP(opposition));
        p.writeShort(0);
        p.writeLong(0);
        return p;
    }

    public Packet pyramidGauge(int gauge) {
        OutPacket p = OutPacket.create(SendOpcode.PYRAMID_GAUGE);
        p.writeInt(gauge);
        return p;
    }

    public Packet pyramidScore(byte score, int exp) {
        OutPacket p = OutPacket.create(SendOpcode.PYRAMID_SCORE);
        p.writeByte(score);
        p.writeInt(exp);
        return p;
    }

    public Packet spawnDragon(Dragon dragon) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_DRAGON);
        p.writeInt(dragon.getOwner().getId());
        p.writeShort(dragon.getPosition().x);
        p.writeShort(0);
        p.writeShort(dragon.getPosition().y);
        p.writeShort(0);
        p.writeByte(dragon.getStance());
        p.writeByte(0);
        p.writeShort(dragon.getOwner().getJob().getId());
        return p;
    }

    public Packet moveDragon(Dragon dragon, Point startPos, InPacket movementPacket, long movementDataLength) {
        final OutPacket p = OutPacket.create(SendOpcode.MOVE_DRAGON);
        p.writeInt(dragon.getOwner().getId());
        p.writePos(startPos);
        rebroadcastMovementList(p, movementPacket, movementDataLength);
        return p;
    }

    public Packet removeDragon(int chrId) {
        OutPacket p = OutPacket.create(SendOpcode.REMOVE_DRAGON);
        p.writeInt(chrId);
        return p;
    }

    public Packet changeBackgroundEffect(boolean remove, int layer, int transition) {
        OutPacket p = OutPacket.create(SendOpcode.SET_BACK_EFFECT);
        p.writeBool(remove);
        p.writeInt(0);
        p.writeByte(layer);
        p.writeInt(transition);
        return p;
    }

    public Packet setNPCScriptable(Map<Integer, String> scriptableNpcIds) {
        OutPacket p = OutPacket.create(SendOpcode.SET_NPC_SCRIPTABLE);
        p.writeByte(scriptableNpcIds.size());
        scriptableNpcIds.forEach((id, name) -> {
            p.writeInt(id);

            p.writeString(name);
            p.writeInt(0);
            p.writeInt(Integer.MAX_VALUE);
        });
        return p;
    }

    public static class WhisperFlag {
        public static final byte LOCATION = 0x01;
        public static final byte WHISPER = 0x02;
        public static final byte REQUEST = 0x04;
        public static final byte RESULT = 0x08;
        public static final byte RECEIVE = 0x10;
        public static final byte BLOCKED = 0x20;
        public static final byte LOCATION_FRIEND = 0x40;
    }
}
