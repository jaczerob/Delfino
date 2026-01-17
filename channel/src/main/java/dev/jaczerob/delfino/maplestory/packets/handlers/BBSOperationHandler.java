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

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.net.server.guild.GuildPackets;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public final class BBSOperationHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(BBSOperationHandler.class);

    private void listBBSThreads(Client client, int start, ChannelHandlerContext context) {
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM bbs_threads WHERE guildid = ? ORDER BY localthreadid DESC",
                     ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            ps.setInt(1, client.getPlayer().getGuildId());
            try (ResultSet rs = ps.executeQuery()) {
                context.writeAndFlush(GuildPackets.BBSThreadList(rs, start));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private void newBBSReply(Client client, int localthreadid, String text, ChannelHandlerContext context) {
        if (client.getPlayer().getGuildId() <= 0) {
            return;
        }
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            final int threadid;
            try (PreparedStatement ps = con.prepareStatement("SELECT threadid FROM bbs_threads WHERE guildid = ? AND localthreadid = ?")) {
                ps.setInt(1, client.getPlayer().getGuildId());
                ps.setInt(2, localthreadid);

                try (ResultSet threadRS = ps.executeQuery()) {
                    if (!threadRS.next()) {
                        return;
                    }

                    threadid = threadRS.getInt("threadid");
                }
            }

            try (PreparedStatement ps = con.prepareStatement("INSERT INTO bbs_replies " + "(`threadid`, `postercid`, `timestamp`, `content`) VALUES " + "(?, ?, ?, ?)")) {
                ps.setInt(1, threadid);
                ps.setInt(2, client.getPlayer().getId());
                ps.setLong(3, currentServerTime());
                ps.setString(4, text);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE bbs_threads SET replycount = replycount + 1 WHERE threadid = ?")) {
                ps.setInt(1, threadid);
                ps.executeUpdate();
            }

            displayThread(client, localthreadid, context);
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private void editBBSThread(Client client, String title, String text, int icon, int localthreadid, ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        if (chr.getGuildId() < 1) {
            return;
        }
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE bbs_threads SET `name` = ?, `timestamp` = ?, " + "`icon` = ?, " + "`startpost` = ? WHERE guildid = ? AND localthreadid = ? AND (postercid = ? OR ?)")) {

            ps.setString(1, title);
            ps.setLong(2, currentServerTime());
            ps.setInt(3, icon);
            ps.setString(4, text);
            ps.setInt(5, chr.getGuildId());
            ps.setInt(6, localthreadid);
            ps.setInt(7, chr.getId());
            ps.setBoolean(8, chr.getGuildRank() < 3);
            ps.execute();

            displayThread(client, localthreadid, context);
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private void newBBSThread(Client client, String title, String text, int icon, boolean bNotice, ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        if (chr.getGuildId() <= 0) {
            return;
        }
        int nextId = 0;
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            if (!bNotice) {
                try (PreparedStatement ps = con.prepareStatement("SELECT MAX(localthreadid) AS lastLocalId FROM bbs_threads WHERE guildid = ?")) {
                    ps.setInt(1, chr.getGuildId());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        nextId = rs.getInt("lastLocalId") + 1;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("INSERT INTO bbs_threads (`postercid`, `name`, `timestamp`, `icon`, `startpost`, `guildid`, `localthreadid`) VALUES(?, ?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, chr.getId());
                ps.setString(2, title);
                ps.setLong(3, currentServerTime());
                ps.setInt(4, icon);
                ps.setString(5, text);
                ps.setInt(6, chr.getGuildId());
                ps.setInt(7, nextId);
                ps.executeUpdate();
            }

            displayThread(client, nextId, context);
        } catch (SQLException se) {
            se.printStackTrace();
        }

    }

    public void deleteBBSThread(Client client, int localthreadid) {
        Character mc = client.getPlayer();
        if (mc.getGuildId() <= 0) {
            return;
        }

        try (Connection con = DatabaseConnection.getStaticConnection()) {

            final int threadid;
            try (PreparedStatement ps = con.prepareStatement("SELECT threadid, postercid FROM bbs_threads WHERE guildid = ? AND localthreadid = ?")) {
                ps.setInt(1, mc.getGuildId());
                ps.setInt(2, localthreadid);

                try (ResultSet threadRS = ps.executeQuery()) {
                    if (!threadRS.next()) {
                        return;
                    }

                    if (mc.getId() != threadRS.getInt("postercid") && mc.getGuildRank() > 2) {
                        return;
                    }

                    threadid = threadRS.getInt("threadid");
                }
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM bbs_replies WHERE threadid = ?")) {
                ps.setInt(1, threadid);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM bbs_threads WHERE threadid = ?")) {
                ps.setInt(1, threadid);
                ps.executeUpdate();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void deleteBBSReply(Client client, int replyid, ChannelHandlerContext context) {
        Character mc = client.getPlayer();
        if (mc.getGuildId() <= 0) {
            return;
        }

        final int threadid;
        try (Connection con = DatabaseConnection.getStaticConnection()) {

            try (PreparedStatement ps = con.prepareStatement("SELECT postercid, threadid FROM bbs_replies WHERE replyid = ?")) {
                ps.setInt(1, replyid);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return;
                    }

                    if (mc.getId() != rs.getInt("postercid") && mc.getGuildRank() > 2) {
                        return;
                    }

                    threadid = rs.getInt("threadid");
                }
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM bbs_replies WHERE replyid = ?")) {
                ps.setInt(1, replyid);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE bbs_threads SET replycount = replycount - 1 WHERE threadid = ?")) {
                ps.setInt(1, threadid);
                ps.executeUpdate();
            }

            displayThread(client, threadid, false, context);
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void displayThread(Client client, int threadid, ChannelHandlerContext context) {
        displayThread(client, threadid, true, context);
    }

    public void displayThread(Client client, int threadid, boolean bIsThreadIdLocal, ChannelHandlerContext context) {
        Character mc = client.getPlayer();
        if (mc.getGuildId() <= 0) {
            return;
        }

        try (Connection con = DatabaseConnection.getStaticConnection()) {
            // TODO clean up this block and use try-with-resources
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM bbs_threads WHERE guildid = ? AND " + (bIsThreadIdLocal ? "local" : "") + "threadid = ?")) {
                ps.setInt(1, mc.getGuildId());
                ps.setInt(2, threadid);
                ResultSet threadRS = ps.executeQuery();
                if (!threadRS.next()) {
                    return;
                }
                ResultSet repliesRS = null;
                try (PreparedStatement ps2 = con.prepareStatement("SELECT * FROM bbs_replies WHERE threadid = ?")) {
                    if (threadRS.getInt("replycount") >= 0) {
                        ps2.setInt(1, !bIsThreadIdLocal ? threadid : threadRS.getInt("threadid"));
                        repliesRS = ps2.executeQuery();
                    }
                    context.writeAndFlush(GuildPackets.showThread(bIsThreadIdLocal ? threadid : threadRS.getInt("localthreadid"), threadRS, repliesRS));
                }
            }
        } catch (SQLException se) {
            log.error("Error displaying thread", se);
        } catch (RuntimeException re) {//btw we get this everytime for some reason, but replies work!
            log.error("The number of reply rows does not match the replycount in thread.", re);
        }
    }

    private String correctLength(String in, int maxSize) {
        return in.length() > maxSize ? in.substring(0, maxSize) : in;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.BBS_OPERATION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (client.getPlayer().getGuildId() < 1) {
            return;
        }
        byte mode = packet.readByte();
        int localthreadid = 0;
        switch (mode) {
            case 0:
                boolean bEdit = packet.readByte() == 1;
                if (bEdit) {
                    localthreadid = packet.readInt();
                }
                boolean bNotice = packet.readByte() == 1;
                String title = correctLength(packet.readString(), 25);
                String text = correctLength(packet.readString(), 600);
                int icon = packet.readInt();
                if (icon >= 0x64 && icon <= 0x6a) {
                    if (!client.getPlayer().haveItemWithId(5290000 + icon - 0x64, false)) {
                        return;
                    }
                } else if (icon < 0 || icon > 3) {
                    return;
                }
                if (!bEdit) {
                    newBBSThread(client, title, text, icon, bNotice, context);
                } else {
                    editBBSThread(client, title, text, icon, localthreadid, context);
                }
                break;
            case 1:
                localthreadid = packet.readInt();
                deleteBBSThread(client, localthreadid);
                break;
            case 2:
                int start = packet.readInt();
                listBBSThreads(client, start * 10, context);
                break;
            case 3: // list thread + reply, following by id (int)
                localthreadid = packet.readInt();
                displayThread(client, localthreadid, context);
                break;
            case 4: // reply
                localthreadid = packet.readInt();
                text = correctLength(packet.readString(), 25);
                newBBSReply(client, localthreadid, text, context);
                break;
            case 5: // delete reply
                packet.readInt(); // we don't use this
                int replyid = packet.readInt();
                deleteBBSReply(client, replyid, context);
                break;
            default:
                //System.out.println("Unhandled BBS mode: " + slea.toString());
        }
    }
}
