package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.BuddyList;
import dev.jaczerob.delfino.maplestory.client.BuddylistEntry;
import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.CharacterNameAndId;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.Disease;
import dev.jaczerob.delfino.maplestory.client.Mount;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.Equip;
import dev.jaczerob.delfino.maplestory.client.inventory.Inventory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.Pet;
import dev.jaczerob.delfino.maplestory.client.keybind.KeyBinding;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.net.server.PlayerBuffValueHolder;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.net.server.channel.Channel;
import dev.jaczerob.delfino.maplestory.net.server.channel.CharacterIdChannelPair;
import dev.jaczerob.delfino.maplestory.net.server.world.PartyCharacter;
import dev.jaczerob.delfino.maplestory.net.server.world.PartyOperation;
import dev.jaczerob.delfino.maplestory.net.server.world.World;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.life.MobSkill;
import dev.jaczerob.delfino.maplestory.service.NoteService;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;
import dev.jaczerob.delfino.maplestory.tools.Pair;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public final class PlayerLoggedinHandler extends AbstractPacketHandler {
    private static final Set<Integer> attemptingLoginAccounts = new HashSet<>();

    private final NoteService noteService;

    public PlayerLoggedinHandler(NoteService noteService) {
        this.noteService = noteService;
    }

    private static void showDueyNotification(Character player, ChannelHandlerContext context) {
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("SELECT Type FROM dueypackages WHERE ReceiverId = ? AND Checked = 1 ORDER BY Type DESC")) {
            ps.setInt(1, player.getId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement ps2 = con.prepareStatement("UPDATE dueypackages SET Checked = 0 WHERE ReceiverId = ?")) {
                        ps2.setInt(1, player.getId());
                        ps2.executeUpdate();

                        context.writeAndFlush(ChannelPacketCreator.getInstance().sendDueyParcelNotification(rs.getInt("Type") == 1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static List<Pair<Long, PlayerBuffValueHolder>> getLocalStartTimes(List<PlayerBuffValueHolder> lpbvl) {
        List<Pair<Long, PlayerBuffValueHolder>> timedBuffs = new ArrayList<>();
        long curtime = currentServerTime();

        for (PlayerBuffValueHolder pb : lpbvl) {
            timedBuffs.add(new Pair<>(curtime - pb.usedTime, pb));
        }

        timedBuffs.sort((p1, p2) -> p1.getLeft().compareTo(p2.getLeft()));

        return timedBuffs;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.PLAYER_LOGGEDIN;
    }

    private boolean tryAcquireAccount(int accId) {
        synchronized (attemptingLoginAccounts) {
            if (attemptingLoginAccounts.contains(accId)) {
                return false;
            }

            attemptingLoginAccounts.add(accId);
            return true;
        }
    }

    private void releaseAccount(int accId) {
        synchronized (attemptingLoginAccounts) {
            attemptingLoginAccounts.remove(accId);
        }
    }

    @Override
    public boolean validateState(Client client) {
        return !client.isLoggedIn();
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        final int cid = packet.readInt(); // TODO: investigate if this is the "client id" supplied in PacketCreator#getServerIP()
        final Server server = Server.getInstance();

        if (!client.tryacquireClient()) {
            // thanks MedicOP for assisting on concurrency protection here
            context.writeAndFlush(ChannelPacketCreator.getInstance().getAfterLoginError(10));
        }

        try {
            World wserv = server.getWorld(client.getWorld());
            if (wserv == null) {
                client.disconnect(true, false);
                return;
            }

            Channel cserv = wserv.getChannel(client.getChannel());
            if (cserv == null) {
                client.setChannel(1);
                cserv = wserv.getChannel(client.getChannel());

                if (cserv == null) {
                    client.disconnect(true, false);
                    return;
                }
            }

            Character player = wserv.getPlayerStorage().getCharacterById(cid);

            boolean newcomer = false;
            if (player == null) {
                try {
                    player = Character.loadCharFromDB(cid, client, true);
                    newcomer = true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (player == null) { //If you are still getting null here then please just uninstall the game >.>, we dont need you fucking with the logs
                    client.disconnect(true, false);
                    return;
                }
            }
            client.setPlayer(player);
            client.setAccID(player.getAccountID());

            int accId = client.getAccID();
            if (tryAcquireAccount(accId)) {
                try {
                    client.updateLoginState(Client.LOGIN_LOGGEDIN);
                } finally {
                    releaseAccount(accId);
                }
            } else {
                client.setPlayer(null);
                client.setAccID(0);
                context.writeAndFlush(ChannelPacketCreator.getInstance().getAfterLoginError(10));
                return;
            }

            if (!newcomer) {
                client.setLanguage(player.getClient().getLanguage());
                client.setCharacterSlots((byte) player.getClient().getCharacterSlots());
                player.newClient(client);
            }

            cserv.addPlayer(player);
            wserv.addPlayer(player);
            player.setEnteredChannelWorld();

            List<PlayerBuffValueHolder> buffs = server.getPlayerBuffStorage().getBuffsFromStorage(cid);
            if (buffs != null) {
                List<Pair<Long, PlayerBuffValueHolder>> timedBuffs = getLocalStartTimes(buffs);
                player.silentGiveBuffs(timedBuffs);
            }

            Map<Disease, Pair<Long, MobSkill>> diseases = server.getPlayerBuffStorage().getDiseasesFromStorage(cid);
            if (diseases != null) {
                player.silentApplyDiseases(diseases);
            }

            context.writeAndFlush(ChannelPacketCreator.getInstance().getCharInfo(player));
            if (!player.isHidden()) {
                if (player.isGM() && YamlConfig.config.server.USE_AUTOHIDE_GM) {
                    player.toggleHide();
                }
            }
            player.sendKeymap();
            player.sendQuickmap();
            player.sendMacros();

            // pot bindings being passed through other characters on the account detected thanks to Croosade dev team
            KeyBinding autohpPot = player.getKeymap().get(91);
            player.sendPacket(ChannelPacketCreator.getInstance().sendAutoHpPot(autohpPot != null ? autohpPot.getAction() : 0));

            KeyBinding autompPot = player.getKeymap().get(92);
            player.sendPacket(ChannelPacketCreator.getInstance().sendAutoMpPot(autompPot != null ? autompPot.getAction() : 0));

            player.getMap().addPlayer(player);
            player.visitMap(player.getMap());

            BuddyList bl = player.getBuddylist();
            int[] buddyIds = bl.getBuddyIds();
            wserv.loggedOn(player.getName(), player.getId(), client.getChannel(), buddyIds);
            for (CharacterIdChannelPair onlineBuddy : wserv.multiBuddyFind(player.getId(), buddyIds)) {
                BuddylistEntry ble = bl.get(onlineBuddy.getCharacterId());
                ble.setChannel(onlineBuddy.getChannel());
                bl.put(ble);
            }
            context.writeAndFlush(ChannelPacketCreator.getInstance().updateBuddylist(bl.getBuddies()));

            context.writeAndFlush(ChannelPacketCreator.getInstance().loadFamily());
            context.writeAndFlush(ChannelPacketCreator.getInstance().getFamilyInfo());

            noteService.show(player);

            if (player.getParty() != null) {
                PartyCharacter pchar = player.getMPC();
                pchar.setChannel(client.getChannel());
                pchar.setMapId(player.getMapId());
                pchar.setOnline(true);
                wserv.updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, pchar);
                player.updatePartyMemberHP();
            }

            Inventory eqpInv = player.getInventory(InventoryType.EQUIPPED);
            eqpInv.lockInventory();
            try {
                for (Item it : eqpInv.list()) {
                    player.equippedItem((Equip) it);
                }
            } finally {
                eqpInv.unlockInventory();
            }

            context.writeAndFlush(ChannelPacketCreator.getInstance().updateBuddylist(player.getBuddylist().getBuddies()));

            CharacterNameAndId pendingBuddyRequest = client.getPlayer().getBuddylist().pollPendingRequest();
            if (pendingBuddyRequest != null) {
                context.writeAndFlush(ChannelPacketCreator.getInstance().requestBuddylistAdd(pendingBuddyRequest.getId(), client.getPlayer().getId(), pendingBuddyRequest.getName()));
            }

            context.writeAndFlush(ChannelPacketCreator.getInstance().updateGender(player));
            player.checkMessenger();
            context.writeAndFlush(ChannelPacketCreator.getInstance().enableReport());
            player.changeSkillLevel(SkillFactory.getSkill(10000000 * player.getJobType() + 12), (byte) (player.getLinkedLevel() / 10), 20, -1);

            if (newcomer) {
                for (Pet pet : player.getPets()) {
                    if (pet != null) {
                        wserv.registerPetHunger(player, player.getPetIndex(pet));
                    }
                }

                Mount mount = player.getMount();   // thanks Ari for noticing a scenario where Silver Mane quest couldn't be started
                if (mount.getItemId() != 0) {
                    player.sendPacket(ChannelPacketCreator.getInstance().updateMount(player.getId(), mount, false));
                }

                player.reloadQuestExpirations();
                if (player.isGM()) {
                    Server.getInstance().broadcastGMMessage(client.getWorld(), ChannelPacketCreator.getInstance().earnTitleMessage((player.gmLevel() < 6 ? "GM " : "Admin ") + player.getName() + " has logged in"));
                }

                if (diseases != null) {
                    for (Entry<Disease, Pair<Long, MobSkill>> e : diseases.entrySet()) {
                        final List<Pair<Disease, Integer>> debuff = Collections.singletonList(new Pair<>(e.getKey(), e.getValue().getRight().getX()));
                        context.writeAndFlush(ChannelPacketCreator.getInstance().giveDebuff(debuff, e.getValue().getRight()));
                    }
                }
            }
            player.buffExpireTask();
            player.diseaseExpireTask();
            player.skillCooldownTask();
            player.expirationTask();
            player.questExpirationTask();

            player.commitExcludedItems();
            showDueyNotification(player, context);

            player.resetPlayerRates();
            if (YamlConfig.config.server.USE_ADD_RATES_BY_LEVEL) {
                player.setPlayerRates();
            }

            player.setWorldRates();
            player.updateCouponRates();

            player.receivePartyMemberHP();

            if (YamlConfig.config.server.USE_NPCS_SCRIPTABLE) {

                // Create a copy to prevent always adding entries to the server's list.
                Map<Integer, String> npcsIds = YamlConfig.config.server.NPCS_SCRIPTABLE
                        .entrySet().stream().collect(Collectors.toMap(
                                entry -> Integer.parseInt(entry.getKey()),
                                Entry::getValue
                        ));

                context.writeAndFlush(ChannelPacketCreator.getInstance().setNPCScriptable(npcsIds));
            }

            if (newcomer) {
                player.setLoginTime(System.currentTimeMillis());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.releaseClient();
        }
    }
}
