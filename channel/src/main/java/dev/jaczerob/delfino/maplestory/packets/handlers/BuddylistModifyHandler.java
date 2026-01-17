package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.BuddyList;
import dev.jaczerob.delfino.maplestory.client.BuddyList.BuddyAddResult;
import dev.jaczerob.delfino.maplestory.client.BuddyList.BuddyOperation;
import dev.jaczerob.delfino.maplestory.client.BuddylistEntry;
import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.CharacterNameAndId;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.net.server.world.World;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class BuddylistModifyHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.BUDDYLIST_MODIFY;
    }

    private void nextPendingRequest(Client client, ChannelHandlerContext context) {
        CharacterNameAndId pendingBuddyRequest = client.getPlayer().getBuddylist().pollPendingRequest();
        if (pendingBuddyRequest != null) {
            context.writeAndFlush(ChannelPacketCreator.getInstance().requestBuddylistAdd(pendingBuddyRequest.getId(), client.getPlayer().getId(), pendingBuddyRequest.getName()));
        }
    }

    private CharacterIdNameBuddyCapacity getCharacterIdAndNameFromDatabase(String name) throws SQLException {
        CharacterIdNameBuddyCapacity ret = null;

        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, name, buddyCapacity FROM characters WHERE name LIKE ?")) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret = new CharacterIdNameBuddyCapacity(rs.getInt("id"), rs.getString("name"), rs.getInt("buddyCapacity"));
                }
            }
        }

        return ret;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int mode = packet.readByte();
        Character player = client.getPlayer();
        BuddyList buddylist = player.getBuddylist();
        if (mode == 1) { // add
            String addName = packet.readString();
            String group = packet.readString();
            if (group.length() > 16 || addName.length() < 4 || addName.length() > 13) {
                return; //hax.
            }
            BuddylistEntry ble = buddylist.get(addName);
            if (ble != null && !ble.isVisible() && group.equals(ble.getGroup())) {
                context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "You already have \"" + ble.getName() + "\" on your Buddylist"));
            } else if (buddylist.isFull() && ble == null) {
                context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "Your buddylist is already full"));
            } else if (ble == null) {
                try {
                    World world = client.getWorldServer();
                    CharacterIdNameBuddyCapacity charWithId;
                    int channel;
                    Character otherChar = client.getChannelServer().getPlayerStorage().getCharacterByName(addName);
                    if (otherChar != null) {
                        channel = client.getChannel();
                        charWithId = new CharacterIdNameBuddyCapacity(otherChar.getId(), otherChar.getName(), otherChar.getBuddylist().getCapacity());
                    } else {
                        channel = world.find(addName);
                        charWithId = getCharacterIdAndNameFromDatabase(addName);
                    }
                    if (charWithId != null) {
                        BuddyAddResult buddyAddResult = null;
                        if (channel != -1) {
                            buddyAddResult = world.requestBuddyAdd(addName, client.getChannel(), player.getId(), player.getName());
                        } else {
                            try (Connection con = DatabaseConnection.getStaticConnection()) {
                                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as buddyCount FROM buddies WHERE characterid = ? AND pending = 0")) {
                                    ps.setInt(1, charWithId.getId());

                                    try (ResultSet rs = ps.executeQuery()) {
                                        if (!rs.next()) {
                                            throw new RuntimeException("Result set expected");
                                        } else if (rs.getInt("buddyCount") >= charWithId.getBuddyCapacity()) {
                                            buddyAddResult = BuddyAddResult.BUDDYLIST_FULL;
                                        }
                                    }
                                }

                                try (PreparedStatement ps = con.prepareStatement("SELECT pending FROM buddies WHERE characterid = ? AND buddyid = ?")) {
                                    ps.setInt(1, charWithId.getId());
                                    ps.setInt(2, player.getId());

                                    try (ResultSet rs = ps.executeQuery()) {
                                        if (rs.next()) {
                                            buddyAddResult = BuddyAddResult.ALREADY_ON_LIST;
                                        }
                                    }
                                }
                            }
                        }
                        if (buddyAddResult == BuddyAddResult.BUDDYLIST_FULL) {
                            context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "\"" + addName + "\"'s Buddylist is full"));
                        } else {
                            int displayChannel;
                            displayChannel = -1;
                            int otherCid = charWithId.getId();
                            if (buddyAddResult == BuddyAddResult.ALREADY_ON_LIST && channel != -1) {
                                displayChannel = channel;
                                notifyRemoteChannel(client, channel, otherCid, BuddyOperation.ADDED);
                            } else if (buddyAddResult != BuddyAddResult.ALREADY_ON_LIST && channel == -1) {
                                try (Connection con = DatabaseConnection.getStaticConnection();
                                     PreparedStatement ps = con.prepareStatement("INSERT INTO buddies (characterid, buddyid, pending) VALUES (?, ?, 1)")) {
                                    ps.setInt(1, charWithId.getId());
                                    ps.setInt(2, player.getId());
                                    ps.executeUpdate();
                                }
                            }
                            buddylist.put(new BuddylistEntry(charWithId.getName(), group, otherCid, displayChannel, true));
                            context.writeAndFlush(ChannelPacketCreator.getInstance().updateBuddylist(buddylist.getBuddies()));
                        }
                    } else {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "A character called \"" + addName + "\" does not exist"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                ble.changeGroup(group);
                context.writeAndFlush(ChannelPacketCreator.getInstance().updateBuddylist(buddylist.getBuddies()));
            }
        } else if (mode == 2) { // accept buddy
            int otherCid = packet.readInt();
            if (!buddylist.isFull()) {
                try {
                    int channel = client.getWorldServer().find(otherCid);//worldInterface.find(otherCid);
                    String otherName = null;
                    Character otherChar = client.getChannelServer().getPlayerStorage().getCharacterById(otherCid);
                    if (otherChar == null) {
                        try (Connection con = DatabaseConnection.getStaticConnection();
                             PreparedStatement ps = con.prepareStatement("SELECT name FROM characters WHERE id = ?")) {
                            ps.setInt(1, otherCid);

                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    otherName = rs.getString("name");
                                }
                            }
                        }
                    } else {
                        otherName = otherChar.getName();
                    }
                    if (otherName != null) {
                        buddylist.put(new BuddylistEntry(otherName, "Default Group", otherCid, channel, true));
                        context.writeAndFlush(ChannelPacketCreator.getInstance().updateBuddylist(buddylist.getBuddies()));
                        notifyRemoteChannel(client, channel, otherCid, BuddyOperation.ADDED);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            nextPendingRequest(client, context);
        } else if (mode == 3) { // delete
            int otherCid = packet.readInt();
            player.deleteBuddy(otherCid);
        }
    }

    private void notifyRemoteChannel(Client client, int remoteChannel, int otherCid, BuddyOperation operation) {
        Character player = client.getPlayer();
        if (remoteChannel != -1) {
            client.getWorldServer().buddyChanged(otherCid, player.getId(), player.getName(), client.getChannel(), operation);
        }
    }

    private static class CharacterIdNameBuddyCapacity extends CharacterNameAndId {
        private final int buddyCapacity;

        public CharacterIdNameBuddyCapacity(int id, String name, int buddyCapacity) {
            super(id, name);
            this.buddyCapacity = buddyCapacity;
        }

        public int getBuddyCapacity() {
            return buddyCapacity;
        }
    }
}
