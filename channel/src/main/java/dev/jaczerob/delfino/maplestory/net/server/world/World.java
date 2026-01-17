package dev.jaczerob.delfino.maplestory.net.server.world;

import dev.jaczerob.delfino.maplestory.client.BuddyList;
import dev.jaczerob.delfino.maplestory.client.BuddyList.BuddyAddResult;
import dev.jaczerob.delfino.maplestory.client.BuddyList.BuddyOperation;
import dev.jaczerob.delfino.maplestory.client.BuddylistEntry;
import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.net.server.PlayerStorage;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.net.server.channel.Channel;
import dev.jaczerob.delfino.maplestory.net.server.channel.CharacterIdChannelPair;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.partysearch.PartySearchCoordinator;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteResultType;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteType;
import dev.jaczerob.delfino.maplestory.net.server.services.BaseService;
import dev.jaczerob.delfino.maplestory.net.server.services.ServicesManager;
import dev.jaczerob.delfino.maplestory.net.server.services.type.WorldServices;
import dev.jaczerob.delfino.maplestory.net.server.task.CharacterAutosaverTask;
import dev.jaczerob.delfino.maplestory.net.server.task.CharacterHpDecreaseTask;
import dev.jaczerob.delfino.maplestory.net.server.task.MountTirednessTask;
import dev.jaczerob.delfino.maplestory.net.server.task.PartySearchTask;
import dev.jaczerob.delfino.maplestory.net.server.task.PetFullnessTask;
import dev.jaczerob.delfino.maplestory.net.server.task.ServerMessageTask;
import dev.jaczerob.delfino.maplestory.net.server.task.TimedMapObjectTask;
import dev.jaczerob.delfino.maplestory.net.server.task.TimeoutTask;
import dev.jaczerob.delfino.maplestory.server.Storage;
import dev.jaczerob.delfino.maplestory.server.TimerManager;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;
import dev.jaczerob.delfino.maplestory.tools.Pair;
import dev.jaczerob.delfino.network.packets.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class World {
    private static final Logger log = LoggerFactory.getLogger(World.class);

    private final int id;
    private final List<Channel> channels = new ArrayList<>();
    private final Map<Integer, Byte> pnpcStep = new HashMap<>();
    private final Map<Integer, Short> pnpcPodium = new HashMap<>();
    private final Map<Integer, Messenger> messengers = new HashMap<>();
    private final AtomicInteger runningMessengerId = new AtomicInteger();
    private final ServicesManager services = new ServicesManager(WorldServices.SAVE_CHARACTER);
    private final PartySearchCoordinator partySearch = new PartySearchCoordinator();
    private final Lock chnRLock;
    private final Lock chnWLock;
    private final Map<Integer, SortedMap<Integer, Character>> accountChars = new HashMap<>();
    private final Map<Integer, Storage> accountStorages = new HashMap<>();
    private final Lock accountCharsLock = new ReentrantLock(true);
    private final Map<Integer, Integer> partyChars = new HashMap<>();
    private final Map<Integer, Party> parties = new HashMap<>();
    private final AtomicInteger runningPartyId = new AtomicInteger();
    private final Lock partyLock = new ReentrantLock(true);
    private final Map<Integer, Integer> owlSearched = new LinkedHashMap<>();
    private final List<Map<Integer, Integer>> cashItemBought = new ArrayList<>(9);
    private final Lock suggestRLock;
    private final Lock suggestWLock;
    private final Map<Integer, Integer> disabledServerMessages = new HashMap<>();    // reuse owl lock
    private final Lock srvMessagesLock = new ReentrantLock();
    private final Map<Integer, Integer> activePets = new LinkedHashMap<>();
    private final Map<Integer, Integer> activeMounts = new LinkedHashMap<>();
    private final Map<Runnable, Long> registeredTimedMapObjects = new LinkedHashMap<>();
    private final Lock activePetsLock = new ReentrantLock(true);
    private final Lock activeMountsLock = new ReentrantLock(true);
    private final Lock timedMapObjectLock = new ReentrantLock(true);
    private final Map<Character, Integer> playerHpDec = Collections.synchronizedMap(new WeakHashMap<>());
    private final int expRate;
    private final int dropRate;
    private final int bossDropRate;
    private final int mesoRate;
    private final int questRate;
    private final int fishingRate;
    private final PlayerStorage players = new PlayerStorage();
    private long petUpdate;
    private long mountUpdate;

    public World(int world, int expRate, int dropRate, int bossDropRate, int mesorate, int questrate, int fishingRate) {
        this.id = world;
        this.expRate = expRate;
        this.dropRate = dropRate;
        this.bossDropRate = bossDropRate;
        this.mesoRate = mesorate;
        this.questRate = questrate;
        this.fishingRate = fishingRate;
        runningPartyId.set(1000000001); // partyid must not clash with charid to solve update item looting issues, found thanks to Vcoc
        runningMessengerId.set(1);

        ReadWriteLock channelLock = new ReentrantReadWriteLock(true);
        this.chnRLock = channelLock.readLock();
        this.chnWLock = channelLock.writeLock();

        ReadWriteLock suggestLock = new ReentrantReadWriteLock(true);
        this.suggestRLock = suggestLock.readLock();
        this.suggestWLock = suggestLock.writeLock();

        petUpdate = Server.getInstance().getCurrentTime();
        mountUpdate = petUpdate;

        for (int i = 0; i < 9; i++) {
            cashItemBought.add(new LinkedHashMap<>());
        }

        TimerManager tman = TimerManager.getInstance();
        tman.register(new PetFullnessTask(this), MINUTES.toMillis(1), MINUTES.toMillis(1));
        tman.register(new ServerMessageTask(this), SECONDS.toMillis(10), SECONDS.toMillis(10));
        tman.register(new MountTirednessTask(this), MINUTES.toMillis(1), MINUTES.toMillis(1));
        tman.register(new TimedMapObjectTask(this), MINUTES.toMillis(1), MINUTES.toMillis(1));
        tman.register(new CharacterAutosaverTask(this), HOURS.toMillis(1), HOURS.toMillis(1));
        tman.register(new PartySearchTask(this), SECONDS.toMillis(10), SECONDS.toMillis(10));
        tman.register(new TimeoutTask(this), SECONDS.toMillis(10), SECONDS.toMillis(10));
        tman.register(new CharacterHpDecreaseTask(this), YamlConfig.config.server.MAP_DAMAGE_OVERTIME_INTERVAL, YamlConfig.config.server.MAP_DAMAGE_OVERTIME_INTERVAL);
    }

    private static Integer getPetKey(Character chr, byte petSlot) {    // assuming max 3 pets
        return (chr.getId() << 2) + petSlot;
    }

    private static void executePlayerNpcMapDataUpdate(Connection con, boolean isPodium, Map<Integer, ?> pnpcData, int value, int worldid, int mapid) throws SQLException {
        final String query;
        if (pnpcData.containsKey(mapid)) {
            query = "UPDATE playernpcs_field SET " + (isPodium ? "podium" : "step") + " = ? WHERE world = ? AND map = ?";
        } else {
            query = "INSERT INTO playernpcs_field (" + (isPodium ? "podium" : "step") + ", world, map) VALUES (?, ?, ?)";
        }

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, value);
            ps.setInt(2, worldid);
            ps.setInt(3, mapid);
            ps.executeUpdate();
        }
    }

    public List<Channel> getChannels() {
        chnRLock.lock();
        try {
            return new ArrayList<>(channels);
        } finally {
            chnRLock.unlock();
        }
    }

    public Channel getChannel(int channel) {
        chnRLock.lock();
        try {
            try {
                return channels.get(channel - 1);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        } finally {
            chnRLock.unlock();
        }
    }

    public boolean addChannel(Channel channel) {
        chnWLock.lock();
        try {
            if (channel.getId() == channels.size() + 1) {
                channels.add(channel);
                return true;
            } else {
                return false;
            }
        } finally {
            chnWLock.unlock();
        }
    }

    public int getExpRate() {
        return expRate;
    }

    public int getDropRate() {
        return dropRate;
    }

    public int getBossDropRate() {  // boss rate concept thanks to Lapeiro
        return bossDropRate;
    }

    public int getMesoRate() {
        return mesoRate;
    }

    public int getQuestRate() {
        return questRate;
    }

    public int getFishingRate() {
        return fishingRate;
    }

    public void loadAccountCharactersView(Integer accountId, List<Character> chars) {
        SortedMap<Integer, Character> charsMap = new TreeMap<>();
        for (Character chr : chars) {
            charsMap.put(chr.getId(), chr);
        }

        accountCharsLock.lock();    // accountCharsLock should be used after server's lgnWLock for compliance
        try {
            accountChars.put(accountId, charsMap);
        } finally {
            accountCharsLock.unlock();
        }
    }

    public void loadAccountStorage(Integer accountId) {
        if (getAccountStorage(accountId) == null) {
            registerAccountStorage(accountId);
        }
    }

    private void registerAccountStorage(Integer accountId) {
        Storage storage = Storage.loadOrCreateFromDB(accountId, this.id);
        accountCharsLock.lock();
        try {
            accountStorages.put(accountId, storage);
        } finally {
            accountCharsLock.unlock();
        }
    }

    public Storage getAccountStorage(Integer accountId) {
        return accountStorages.get(accountId);
    }

    public PlayerStorage getPlayerStorage() {
        return players;
    }

    public PartySearchCoordinator getPartySearchCoordinator() {
        return partySearch;
    }

    public void addPlayer(Character chr) {
        players.addPlayer(chr);
    }

    public void removePlayer(Character chr) {
        Channel cserv = chr.getClient().getChannelServer();

        if (cserv != null) {
            if (!cserv.removePlayer(chr)) {
                // oy the player is not where they should be, find this mf

                for (Channel ch : getChannels()) {
                    if (ch.removePlayer(chr)) {
                        break;
                    }
                }
            }
        }

        players.removePlayer(chr.getId());
    }

    public int getId() {
        return id;
    }

    public void sendPacket(List<Integer> targetIds, Packet packet, int exception) {
        Character chr;
        for (int i : targetIds) {
            if (i == exception) {
                continue;
            }
            chr = getPlayerStorage().getCharacterById(i);
            if (chr != null) {
                chr.sendPacket(packet);
            }
        }
    }

    private void registerCharacterParty(Integer chrid, Integer partyid) {
        partyLock.lock();
        try {
            partyChars.put(chrid, partyid);
        } finally {
            partyLock.unlock();
        }
    }

    private void unregisterCharacterPartyInternal(Integer chrid) {
        partyChars.remove(chrid);
    }

    private void unregisterCharacterParty(Integer chrid) {
        partyLock.lock();
        try {
            unregisterCharacterPartyInternal(chrid);
        } finally {
            partyLock.unlock();
        }
    }

    public Party createParty(PartyCharacter chrfor) {
        int partyid = runningPartyId.getAndIncrement();
        Party party = new Party(partyid, chrfor);

        partyLock.lock();
        try {
            parties.put(party.getId(), party);
            registerCharacterParty(chrfor.getId(), partyid);
        } finally {
            partyLock.unlock();
        }

        party.addMember(chrfor);
        return party;
    }

    public Party getParty(int partyid) {
        partyLock.lock();
        try {
            return parties.get(partyid);
        } finally {
            partyLock.unlock();
        }
    }

    private Party disbandParty(int partyid) {
        partyLock.lock();
        try {
            return parties.remove(partyid);
        } finally {
            partyLock.unlock();
        }
    }

    private void updateCharacterParty(Party party, PartyOperation operation, PartyCharacter target, Collection<PartyCharacter> partyMembers) {
        switch (operation) {
            case JOIN:
                registerCharacterParty(target.getId(), party.getId());
                break;

            case LEAVE:
            case EXPEL:
                unregisterCharacterParty(target.getId());
                break;

            case DISBAND:
                partyLock.lock();
                try {
                    for (PartyCharacter partychar : partyMembers) {
                        unregisterCharacterPartyInternal(partychar.getId());
                    }
                } finally {
                    partyLock.unlock();
                }
                break;

            default:
                break;
        }
    }

    private void updateParty(Party party, PartyOperation operation, PartyCharacter target) {
        Collection<PartyCharacter> partyMembers = party.getMembers();
        updateCharacterParty(party, operation, target, partyMembers);

        for (PartyCharacter partychar : partyMembers) {
            Character chr = getPlayerStorage().getCharacterById(partychar.getId());
            if (chr != null) {
                if (operation == PartyOperation.DISBAND) {
                    chr.setParty(null);
                    chr.setMPC(null);
                } else {
                    chr.setParty(party);
                    chr.setMPC(partychar);
                }
                chr.sendPacket(ChannelPacketCreator.getInstance().updateParty(chr.getClient().getChannel(), party, operation, target));
            }
        }
        switch (operation) {
            case LEAVE:
            case EXPEL:
                Character chr = getPlayerStorage().getCharacterById(target.getId());
                if (chr != null) {
                    chr.sendPacket(ChannelPacketCreator.getInstance().updateParty(chr.getClient().getChannel(), party, operation, target));
                    chr.setParty(null);
                    chr.setMPC(null);
                }
            default:
                break;
        }
    }

    public void updateParty(int partyid, PartyOperation operation, PartyCharacter target) {
        Party party = getParty(partyid);
        if (party == null) {
            throw new IllegalArgumentException("no party with the specified partyid exists");
        }
        switch (operation) {
            case JOIN:
                party.addMember(target);
                break;
            case EXPEL:
            case LEAVE:
                party.removeMember(target);
                break;
            case DISBAND:
                disbandParty(partyid);
                break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
                party.updateMember(target);
                break;
            case CHANGE_LEADER:
                party.setLeader(target);
                break;
            default:
                log.warn("Unhandled updateParty operation: {}", operation.name());
        }
        updateParty(party, operation, target);
    }

    public void removeMapPartyMembers(int partyid) {
        Party party = getParty(partyid);
        if (party == null) {
            return;
        }

        for (PartyCharacter mpc : party.getMembers()) {
            Character mc = mpc.getPlayer();
            if (mc != null) {
                MapleMap map = mc.getMap();
                if (map != null) {
                    map.removeParty(partyid);
                }
            }
        }
    }

    public int find(String name) {
        int channel = -1;
        Character chr = getPlayerStorage().getCharacterByName(name);
        if (chr != null) {
            channel = chr.getClient().getChannel();
        }
        return channel;
    }

    public int find(int id) {
        int channel = -1;
        Character chr = getPlayerStorage().getCharacterById(id);
        if (chr != null) {
            channel = chr.getClient().getChannel();
        }
        return channel;
    }

    public void partyChat(Party party, String chattext, String namefrom) {
        for (PartyCharacter partychar : party.getMembers()) {
            if (!(partychar.getName().equals(namefrom))) {
                Character chr = getPlayerStorage().getCharacterByName(partychar.getName());
                if (chr != null) {
                    chr.sendPacket(ChannelPacketCreator.getInstance().multiChat(namefrom, chattext, 1));
                }
            }
        }
    }

    public void buddyChat(int[] recipientCharacterIds, int cidFrom, String nameFrom, String chattext) {
        PlayerStorage playerStorage = getPlayerStorage();
        for (int characterId : recipientCharacterIds) {
            Character chr = playerStorage.getCharacterById(characterId);
            if (chr != null) {
                if (chr.getBuddylist().containsVisible(cidFrom)) {
                    chr.sendPacket(ChannelPacketCreator.getInstance().multiChat(nameFrom, chattext, 0));
                }
            }
        }
    }

    public CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds) {
        List<CharacterIdChannelPair> foundsChars = new ArrayList<>(characterIds.length);
        for (Channel ch : getChannels()) {
            for (int charid : ch.multiBuddyFind(charIdFrom, characterIds)) {
                foundsChars.add(new CharacterIdChannelPair(charid, ch.getId()));
            }
        }
        return foundsChars.toArray(new CharacterIdChannelPair[foundsChars.size()]);
    }

    public Messenger getMessenger(int messengerid) {
        return messengers.get(messengerid);
    }

    public void leaveMessenger(int messengerid, MessengerCharacter target) {
        Messenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        int position = messenger.getPositionByName(target.getName());
        messenger.removeMember(target);
        removeMessengerPlayer(messenger, position);
    }

    public void messengerInvite(String sender, int messengerid, String target, int fromchannel) {
        if (isConnected(target)) {
            Character targetChr = getPlayerStorage().getCharacterByName(target);
            if (targetChr != null) {
                Messenger messenger = targetChr.getMessenger();
                if (messenger == null) {
                    Character from = getChannel(fromchannel).getPlayerStorage().getCharacterByName(sender);
                    if (from != null) {
                        if (InviteCoordinator.createInvite(InviteType.MESSENGER, from, messengerid, targetChr.getId())) {
                            targetChr.sendPacket(ChannelPacketCreator.getInstance().messengerInvite(sender, messengerid));
                            from.sendPacket(ChannelPacketCreator.getInstance().messengerNote(target, 4, 1));
                        } else {
                            from.sendPacket(ChannelPacketCreator.getInstance().messengerChat(sender + " : " + target + " is already managing a Maple Messenger invitation"));
                        }
                    }
                } else {
                    Character from = getChannel(fromchannel).getPlayerStorage().getCharacterByName(sender);
                    from.sendPacket(ChannelPacketCreator.getInstance().messengerChat(sender + " : " + target + " is already using Maple Messenger"));
                }
            }
        }
    }

    public void addMessengerPlayer(Messenger messenger, String namefrom, int fromchannel, int position) {
        for (MessengerCharacter messengerchar : messenger.getMembers()) {
            Character chr = getPlayerStorage().getCharacterByName(messengerchar.getName());
            if (chr == null) {
                continue;
            }
            if (!messengerchar.getName().equals(namefrom)) {
                Character from = getChannel(fromchannel).getPlayerStorage().getCharacterByName(namefrom);
                chr.sendPacket(ChannelPacketCreator.getInstance().addMessengerPlayer(namefrom, from, position, (byte) (fromchannel - 1)));
                from.sendPacket(ChannelPacketCreator.getInstance().addMessengerPlayer(chr.getName(), chr, messengerchar.getPosition(), (byte) (messengerchar.getChannel() - 1)));
            } else {
                chr.sendPacket(ChannelPacketCreator.getInstance().joinMessenger(messengerchar.getPosition()));
            }
        }
    }

    public void removeMessengerPlayer(Messenger messenger, int position) {
        for (MessengerCharacter messengerchar : messenger.getMembers()) {
            Character chr = getPlayerStorage().getCharacterByName(messengerchar.getName());
            if (chr != null) {
                chr.sendPacket(ChannelPacketCreator.getInstance().removeMessengerPlayer(position));
            }
        }
    }

    public void messengerChat(Messenger messenger, String chattext, String namefrom) {
        String from = "";
        String to1 = "";
        String to2 = "";
        for (MessengerCharacter messengerchar : messenger.getMembers()) {
            if (!(messengerchar.getName().equals(namefrom))) {
                Character chr = getPlayerStorage().getCharacterByName(messengerchar.getName());
                if (chr != null) {
                    chr.sendPacket(ChannelPacketCreator.getInstance().messengerChat(chattext));
                    if (to1.equals("")) {
                        to1 = messengerchar.getName();
                    } else if (to2.equals("")) {
                        to2 = messengerchar.getName();
                    }
                }
            } else {
                from = messengerchar.getName();
            }
        }
    }

    public void declineChat(String sender, Character player) {
        if (isConnected(sender)) {
            Character senderChr = getPlayerStorage().getCharacterByName(sender);
            if (senderChr != null && senderChr.getMessenger() != null) {
                if (InviteCoordinator.answerInvite(InviteType.MESSENGER, player.getId(), senderChr.getMessenger().getId(), false).result == InviteResultType.DENIED) {
                    senderChr.sendPacket(ChannelPacketCreator.getInstance().messengerNote(player.getName(), 5, 0));
                }
            }
        }
    }

    public void updateMessenger(int messengerid, String namefrom, int fromchannel) {
        Messenger messenger = getMessenger(messengerid);
        int position = messenger.getPositionByName(namefrom);
        updateMessenger(messenger, namefrom, position, fromchannel);
    }

    public void updateMessenger(Messenger messenger, String namefrom, int position, int fromchannel) {
        for (MessengerCharacter messengerchar : messenger.getMembers()) {
            Channel ch = getChannel(fromchannel);
            if (!(messengerchar.getName().equals(namefrom))) {
                Character chr = ch.getPlayerStorage().getCharacterByName(messengerchar.getName());
                if (chr != null) {
                    chr.sendPacket(ChannelPacketCreator.getInstance().updateMessengerPlayer(namefrom, getChannel(fromchannel).getPlayerStorage().getCharacterByName(namefrom), position, (byte) (fromchannel - 1)));
                }
            }
        }
    }

    public void joinMessenger(int messengerid, MessengerCharacter target, String from, int fromchannel) {
        Messenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.addMember(target, target.getPosition());
        addMessengerPlayer(messenger, from, fromchannel, target.getPosition());
    }

    public void silentJoinMessenger(int messengerid, MessengerCharacter target, int position) {
        Messenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.addMember(target, position);
    }

    public Messenger createMessenger(MessengerCharacter chrfor) {
        int messengerid = runningMessengerId.getAndIncrement();
        Messenger messenger = new Messenger(messengerid, chrfor);
        messengers.put(messenger.getId(), messenger);
        return messenger;
    }

    public boolean isConnected(String charName) {
        return getPlayerStorage().getCharacterByName(charName) != null;
    }

    public BuddyAddResult requestBuddyAdd(String addName, int channelFrom, int cidFrom, String nameFrom) {
        Character addChar = getPlayerStorage().getCharacterByName(addName);
        if (addChar != null) {
            BuddyList buddylist = addChar.getBuddylist();
            if (buddylist.isFull()) {
                return BuddyAddResult.BUDDYLIST_FULL;
            }
            if (!buddylist.contains(cidFrom)) {
                buddylist.addBuddyRequest(addChar.getClient(), cidFrom, nameFrom, channelFrom);
            } else if (buddylist.containsVisible(cidFrom)) {
                return BuddyAddResult.ALREADY_ON_LIST;
            }
        }
        return BuddyAddResult.OK;
    }

    public void buddyChanged(int cid, int cidFrom, String name, int channel, BuddyOperation operation) {
        Character addChar = getPlayerStorage().getCharacterById(cid);
        if (addChar != null) {
            BuddyList buddylist = addChar.getBuddylist();
            switch (operation) {
                case ADDED:
                    if (buddylist.contains(cidFrom)) {
                        buddylist.put(new BuddylistEntry(name, "Default Group", cidFrom, channel, true));
                        addChar.sendPacket(ChannelPacketCreator.getInstance().updateBuddyChannel(cidFrom, (byte) (channel - 1)));
                    }
                    break;
                case DELETED:
                    if (buddylist.contains(cidFrom)) {
                        buddylist.put(new BuddylistEntry(name, "Default Group", cidFrom, (byte) -1, buddylist.get(cidFrom).isVisible()));
                        addChar.sendPacket(ChannelPacketCreator.getInstance().updateBuddyChannel(cidFrom, (byte) -1));
                    }
                    break;
            }
        }
    }

    public void loggedOff(String name, int characterId, int channel, int[] buddies) {
        updateBuddies(characterId, channel, buddies, true);
    }

    public void loggedOn(String name, int characterId, int channel, int[] buddies) {
        updateBuddies(characterId, channel, buddies, false);
    }

    private void updateBuddies(int characterId, int channel, int[] buddies, boolean offline) {
        PlayerStorage playerStorage = getPlayerStorage();
        for (int buddy : buddies) {
            Character chr = playerStorage.getCharacterById(buddy);
            if (chr != null) {
                BuddylistEntry ble = chr.getBuddylist().get(characterId);
                if (ble != null && ble.isVisible()) {
                    int mcChannel;
                    if (offline) {
                        ble.setChannel((byte) -1);
                        mcChannel = -1;
                    } else {
                        ble.setChannel(channel);
                        mcChannel = (byte) (channel - 1);
                    }
                    chr.getBuddylist().put(ble);
                    chr.sendPacket(ChannelPacketCreator.getInstance().updateBuddyChannel(ble.getCharacterId(), mcChannel));
                }
            }
        }
    }

    public void addOwlItemSearch(Integer itemid) {
        suggestWLock.lock();
        try {
            Integer cur = owlSearched.get(itemid);
            if (cur != null) {
                owlSearched.put(itemid, cur + 1);
            } else {
                owlSearched.put(itemid, 1);
            }
        } finally {
            suggestWLock.unlock();
        }
    }

    public List<Pair<Integer, Integer>> getOwlSearchedItems() {
        if (YamlConfig.config.server.USE_ENFORCE_ITEM_SUGGESTION) {
            return new ArrayList<>(0);
        }

        suggestRLock.lock();
        try {
            List<Pair<Integer, Integer>> searchCounts = new ArrayList<>(owlSearched.size());

            for (Entry<Integer, Integer> e : owlSearched.entrySet()) {
                searchCounts.add(new Pair<>(e.getKey(), e.getValue()));
            }

            return searchCounts;
        } finally {
            suggestRLock.unlock();
        }
    }

    public void addCashItemBought(Integer snid) {
        suggestWLock.lock();
        try {
            Map<Integer, Integer> tabItemBought = cashItemBought.get(snid / 10000000);

            Integer cur = tabItemBought.get(snid);
            if (cur != null) {
                tabItemBought.put(snid, cur + 1);
            } else {
                tabItemBought.put(snid, 1);
            }
        } finally {
            suggestWLock.unlock();
        }
    }

    private List<List<Pair<Integer, Integer>>> getBoughtCashItems() {
        if (YamlConfig.config.server.USE_ENFORCE_ITEM_SUGGESTION) {
            List<List<Pair<Integer, Integer>>> boughtCounts = new ArrayList<>(9);

            // thanks GabrielSin for pointing out an issue here
            for (int i = 0; i < 9; i++) {
                List<Pair<Integer, Integer>> tabCounts = new ArrayList<>(0);
                boughtCounts.add(tabCounts);
            }

            return boughtCounts;
        }

        suggestRLock.lock();
        try {
            List<List<Pair<Integer, Integer>>> boughtCounts = new ArrayList<>(cashItemBought.size());

            for (Map<Integer, Integer> tab : cashItemBought) {
                List<Pair<Integer, Integer>> tabItems = new LinkedList<>();
                boughtCounts.add(tabItems);

                for (Entry<Integer, Integer> e : tab.entrySet()) {
                    tabItems.add(new Pair<>(e.getKey(), e.getValue()));
                }
            }

            return boughtCounts;
        } finally {
            suggestRLock.unlock();
        }
    }

    private List<Integer> getMostSellerOnTab(List<Pair<Integer, Integer>> tabSellers) {
        List<Integer> tabLeaderboards;

        // descending order
        Comparator<Pair<Integer, Integer>> comparator = (p1, p2) -> p2.getRight().compareTo(p1.getRight());

        PriorityQueue<Pair<Integer, Integer>> queue = new PriorityQueue<>(Math.max(1, tabSellers.size()), comparator);
        queue.addAll(tabSellers);

        tabLeaderboards = new LinkedList<>();
        for (int i = 0; i < Math.min(tabSellers.size(), 5); i++) {
            tabLeaderboards.add(queue.remove().getLeft());
        }

        return tabLeaderboards;
    }

    public List<List<Integer>> getMostSellerCashItems() {
        List<List<Pair<Integer, Integer>>> mostSellers = this.getBoughtCashItems();
        List<List<Integer>> cashLeaderboards = new ArrayList<>(9);
        List<Integer> tabLeaderboards;
        List<Integer> allLeaderboards = null;

        for (List<Pair<Integer, Integer>> tabSellers : mostSellers) {
            if (tabSellers.size() < 5) {
                if (allLeaderboards == null) {
                    List<Pair<Integer, Integer>> allSellers = new LinkedList<>();
                    for (List<Pair<Integer, Integer>> tabItems : mostSellers) {
                        allSellers.addAll(tabItems);
                    }

                    allLeaderboards = getMostSellerOnTab(allSellers);
                }

                tabLeaderboards = new LinkedList<>();
                if (allLeaderboards.size() < 5) {
                    for (int i : GameConstants.CASH_DATA) {
                        tabLeaderboards.add(i);
                    }
                } else {
                    tabLeaderboards.addAll(allLeaderboards);
                }
            } else {
                tabLeaderboards = getMostSellerOnTab(tabSellers);
            }

            cashLeaderboards.add(tabLeaderboards);
        }

        return cashLeaderboards;
    }

    public void registerPetHunger(Character chr, byte petSlot) {
        if (chr.isGM() && YamlConfig.config.server.GM_PETS_NEVER_HUNGRY || YamlConfig.config.server.PETS_NEVER_HUNGRY) {
            return;
        }

        Integer key = getPetKey(chr, petSlot);

        activePetsLock.lock();
        try {
            int initProc;
            if (Server.getInstance().getCurrentTime() - petUpdate > 55000) {
                initProc = YamlConfig.config.server.PET_EXHAUST_COUNT - 2;
            } else {
                initProc = YamlConfig.config.server.PET_EXHAUST_COUNT - 1;
            }

            activePets.put(key, initProc);
        } finally {
            activePetsLock.unlock();
        }
    }

    public void unregisterPetHunger(Character chr, byte petSlot) {
        Integer key = getPetKey(chr, petSlot);

        activePetsLock.lock();
        try {
            activePets.remove(key);
        } finally {
            activePetsLock.unlock();
        }
    }

    public void runPetSchedule() {
        Map<Integer, Integer> deployedPets;

        activePetsLock.lock();
        try {
            petUpdate = Server.getInstance().getCurrentTime();
            deployedPets = new HashMap<>(activePets);   // exception here found thanks to MedicOP
        } finally {
            activePetsLock.unlock();
        }

        for (Entry<Integer, Integer> dp : deployedPets.entrySet()) {
            Character chr = this.getPlayerStorage().getCharacterById(dp.getKey() / 4);
            if (chr == null || !chr.isLoggedinWorld()) {
                continue;
            }

            int dpVal = dp.getValue() + 1;
            if (dpVal == YamlConfig.config.server.PET_EXHAUST_COUNT) {
                chr.runFullnessSchedule(dp.getKey() % 4);
                dpVal = 0;
            }

            activePetsLock.lock();
            try {
                activePets.put(dp.getKey(), dpVal);
            } finally {
                activePetsLock.unlock();
            }
        }
    }

    public void registerMountHunger(Character chr) {
        if (chr.isGM() && YamlConfig.config.server.GM_PETS_NEVER_HUNGRY || YamlConfig.config.server.PETS_NEVER_HUNGRY) {
            return;
        }

        Integer key = chr.getId();
        activeMountsLock.lock();
        try {
            int initProc;
            if (Server.getInstance().getCurrentTime() - mountUpdate > 45000) {
                initProc = YamlConfig.config.server.MOUNT_EXHAUST_COUNT - 2;
            } else {
                initProc = YamlConfig.config.server.MOUNT_EXHAUST_COUNT - 1;
            }

            activeMounts.put(key, initProc);
        } finally {
            activeMountsLock.unlock();
        }
    }

    public void unregisterMountHunger(Character chr) {
        Integer key = chr.getId();

        activeMountsLock.lock();
        try {
            activeMounts.remove(key);
        } finally {
            activeMountsLock.unlock();
        }
    }

    public void runMountSchedule() {
        Map<Integer, Integer> deployedMounts;
        activeMountsLock.lock();
        try {
            mountUpdate = Server.getInstance().getCurrentTime();
            deployedMounts = new HashMap<>(activeMounts);
        } finally {
            activeMountsLock.unlock();
        }

        for (Entry<Integer, Integer> dp : deployedMounts.entrySet()) {
            Character chr = this.getPlayerStorage().getCharacterById(dp.getKey());
            if (chr == null || !chr.isLoggedinWorld()) {
                continue;
            }

            int dpVal = dp.getValue() + 1;
            if (dpVal == YamlConfig.config.server.MOUNT_EXHAUST_COUNT) {
                if (!chr.runTirednessSchedule()) {
                    continue;
                }
                dpVal = 0;
            }

            activeMountsLock.lock();
            try {
                activeMounts.put(dp.getKey(), dpVal);
            } finally {
                activeMountsLock.unlock();
            }
        }
    }

    public void registerTimedMapObject(Runnable r, long duration) {
        timedMapObjectLock.lock();
        try {
            long expirationTime = Server.getInstance().getCurrentTime() + duration;
            registeredTimedMapObjects.put(r, expirationTime);
        } finally {
            timedMapObjectLock.unlock();
        }
    }

    public void runTimedMapObjectSchedule() {
        List<Runnable> toRemove = new LinkedList<>();

        timedMapObjectLock.lock();
        try {
            long timeNow = Server.getInstance().getCurrentTime();

            for (Entry<Runnable, Long> rtmo : registeredTimedMapObjects.entrySet()) {
                if (rtmo.getValue() <= timeNow) {
                    toRemove.add(rtmo.getKey());
                }
            }

            for (Runnable r : toRemove) {
                registeredTimedMapObjects.remove(r);
            }
        } finally {
            timedMapObjectLock.unlock();
        }

        for (Runnable r : toRemove) {
            r.run();
        }
    }

    public void addPlayerHpDecrease(Character chr) {
        playerHpDec.putIfAbsent(chr, 0);
    }

    public void removePlayerHpDecrease(Character chr) {
        playerHpDec.remove(chr);
    }

    public void runPlayerHpDecreaseSchedule() {
        Map<Character, Integer> m = new HashMap<>();
        m.putAll(playerHpDec);

        for (Entry<Character, Integer> e : m.entrySet()) {
            Character chr = e.getKey();

            if (!chr.isAwayFromWorld()) {
                int c = e.getValue();
                c = (c + 1) % YamlConfig.config.server.MAP_DAMAGE_OVERTIME_COUNT;
                playerHpDec.replace(chr, c);

                if (c == 0) {
                    chr.doHurtHp();
                }
            }
        }
    }

    public void resetDisabledServerMessages() {
        srvMessagesLock.lock();
        try {
            disabledServerMessages.clear();
        } finally {
            srvMessagesLock.unlock();
        }
    }

    public boolean registerDisabledServerMessage(int chrid) {
        srvMessagesLock.lock();
        try {
            boolean alreadyDisabled = disabledServerMessages.containsKey(chrid);
            disabledServerMessages.put(chrid, 0);

            return alreadyDisabled;
        } finally {
            srvMessagesLock.unlock();
        }
    }

    public boolean unregisterDisabledServerMessage(int chrid) {
        srvMessagesLock.lock();
        try {
            return disabledServerMessages.remove(chrid) != null;
        } finally {
            srvMessagesLock.unlock();
        }
    }

    public void runDisabledServerMessagesSchedule() {
        List<Integer> toRemove = new LinkedList<>();

        srvMessagesLock.lock();
        try {
            for (Entry<Integer, Integer> dsm : disabledServerMessages.entrySet()) {
                int b = dsm.getValue();
                if (b >= 4) {   // ~35sec duration, 10sec update
                    toRemove.add(dsm.getKey());
                } else {
                    disabledServerMessages.put(dsm.getKey(), ++b);
                }
            }

            for (Integer chrid : toRemove) {
                disabledServerMessages.remove(chrid);
            }
        } finally {
            srvMessagesLock.unlock();
        }

        if (!toRemove.isEmpty()) {
            for (Integer chrid : toRemove) {
                Character chr = players.getCharacterById(chrid);

                if (chr != null && chr.isLoggedinWorld()) {
                    chr.sendPacket(ChannelPacketCreator.getInstance().serverMessage(chr.getClient().getChannelServer().getServerMessage()));
                }
            }
        }
    }

    public void setPlayerNpcMapStep(int mapid, int step) {
        setPlayerNpcMapData(mapid, step, -1, false);
    }

    public void setPlayerNpcMapPodiumData(int mapid, int podium) {
        setPlayerNpcMapData(mapid, -1, podium, false);
    }

    public void setPlayerNpcMapData(int mapid, int step, int podium) {
        setPlayerNpcMapData(mapid, step, podium, true);
    }

    private void setPlayerNpcMapData(int mapid, int step, int podium, boolean silent) {
        if (!silent) {
            try (Connection con = DatabaseConnection.getStaticConnection()) {
                if (step != -1) {
                    executePlayerNpcMapDataUpdate(con, false, pnpcStep, step, id, mapid);
                }

                if (podium != -1) {
                    executePlayerNpcMapDataUpdate(con, true, pnpcPodium, podium, id, mapid);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (step != -1) {
            pnpcStep.put(mapid, (byte) step);
        }
        if (podium != -1) {
            pnpcPodium.put(mapid, (short) podium);
        }
    }

    public int getPlayerNpcMapStep(int mapid) {
        try {
            return pnpcStep.get(mapid);
        } catch (NullPointerException npe) {
            return 0;
        }
    }

    public int getPlayerNpcMapPodiumData(int mapid) {
        try {
            return pnpcPodium.get(mapid);
        } catch (NullPointerException npe) {
            return 1;
        }
    }

    public void setServerMessage(String msg) {
        for (Channel ch : getChannels()) {
            ch.setServerMessage(msg);
        }
    }

    public void broadcastPacket(Packet packet) {
        for (Character chr : players.getAllCharacters()) {
            chr.sendPacket(packet);
        }
    }

    public void dropMessage(int type, String message) {
        for (Character player : getPlayerStorage().getAllCharacters()) {
            player.dropMessage(type, message);
        }
    }

    public void runPartySearchUpdateSchedule() {
        partySearch.updatePartySearchStorage();
        partySearch.runPartySearch();
    }

    public BaseService getServiceAccess(WorldServices sv) {
        return services.getAccess(sv).getService();
    }
}
