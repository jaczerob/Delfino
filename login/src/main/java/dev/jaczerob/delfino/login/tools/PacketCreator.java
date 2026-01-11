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
package dev.jaczerob.delfino.login.tools;

import dev.jaczerob.delfino.grpc.proto.character.Character;
import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.net.encryption.InitializationVector;
import dev.jaczerob.delfino.login.net.opcodes.SendOpcode;
import dev.jaczerob.delfino.login.net.packet.ByteBufOutPacket;
import dev.jaczerob.delfino.login.net.packet.OutPacket;
import dev.jaczerob.delfino.login.net.packet.Packet;
import dev.jaczerob.delfino.login.net.server.Server;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class PacketCreator {
    private final static long FT_UT_OFFSET = 116444736010800000L + (10000L * TimeZone.getDefault().getOffset(System.currentTimeMillis())); // normalize with timezone offset suggested by Ari
    private final static long DEFAULT_TIME = 150842304000000000L;//00 80 05 BB 46 E6 17 02
    public final static long ZERO_TIME = 94354848000000000L;//00 40 E0 FD 3B 37 4F 01
    private final static long PERMANENT = 150841440000000000L; // 00 C0 9B 90 7D E5 17 02

    public static long getTime(long utcTimestamp) {
        if (utcTimestamp < 0 && utcTimestamp >= -3) {
            if (utcTimestamp == -1) {
                return DEFAULT_TIME;    //high number ll
            } else if (utcTimestamp == -2) {
                return ZERO_TIME;
            } else {
                return PERMANENT;
            }
        }

        return utcTimestamp * 10000 + FT_UT_OFFSET;
    }

    private static void addCharStats(OutPacket p, Character chr) {
        p.writeInt(chr.getId());
        p.writeFixedString(StringUtil.getRightPaddedStr(chr.getName(), '\0', 13));
        p.writeByte(chr.getGender());
        p.writeByte(chr.getSkinColor());
        p.writeInt(chr.getFace());
        p.writeInt(chr.getHair());

        for (final var pet : chr.getPetsList()) {
            p.writeLong(pet);
        }

        p.writeByte(chr.getLevel());
        p.writeShort(chr.getJob());
        p.writeShort(chr.getStr());
        p.writeShort(chr.getDex());
        p.writeShort(chr.getInt());
        p.writeShort(chr.getLuk());
        p.writeShort(chr.getHp());
        p.writeShort(chr.getMaxHp());
        p.writeShort(chr.getMp());
        p.writeShort(chr.getMaxMp());
        p.writeShort(chr.getRemainingAp());
        p.writeShort(chr.getRemainingSp());
        p.writeInt(chr.getExp());
        p.writeShort(chr.getFame());
        p.writeInt(chr.getGachaExp());
        p.writeInt(chr.getMapId());
        p.writeByte(chr.getSpawnPoint());
        p.writeInt(0);
    }

    protected static void addCharLook(final OutPacket p, Character chr, boolean mega) {
        p.writeByte(chr.getGender());
        p.writeByte(chr.getSkinColor()); // skin color
        p.writeInt(chr.getFace()); // face
        p.writeBool(!mega);
        p.writeInt(chr.getHair()); // hair
        addCharEquips(p, chr);
    }

    private static void addCharEquips(final OutPacket p, final Character chr) {
        for (final var equip : chr.getEquipmentList()) {
            p.writeByte(Math.abs(equip.getPosition()));
            p.writeInt(equip.getId());
        }

        p.writeByte(0xFF);

        for (final var equip : chr.getMaskedEquipmentList()) {
            p.writeByte(Math.abs(equip.getPosition()));
            p.writeInt(equip.getId());
        }

        p.writeByte(0xFF);
        p.writeInt(0);

        for (final var pet : chr.getPetEquipmentList()) {
            p.writeInt(pet.getId());
        }
    }

    private static void addCharEntry(OutPacket p, Character chr, boolean viewall) {
        addCharStats(p, chr);
        addCharLook(p, chr, false);
        if (!viewall) {
            p.writeByte(0);
        }

        if (chr.getGmLevel() > 0) {
            p.writeByte(0);
            return;
        }

        p.writeByte(1);
        p.writeInt(chr.getRank()); // world rank
        p.writeInt(chr.getRankMove()); // move (negative is downwards)
        p.writeInt(chr.getJobRank()); // job rank
        p.writeInt(chr.getJobRankMove()); // move (negative is downwards)
    }

    public static Packet sendGuestTOS() {
        final OutPacket p = OutPacket.create(SendOpcode.GUEST_ID_LOGIN);
        p.writeShort(0x100);
        p.writeInt(Randomizer.nextInt(999999));
        p.writeLong(0);
        p.writeLong(getTime(-2));
        p.writeLong(getTime(System.currentTimeMillis()));
        p.writeInt(0);
        p.writeString("http://maplesolaxia.com");
        return p;
    }

    public static Packet getHello(short mapleVersion, InitializationVector sendIv, InitializationVector recvIv) {
        OutPacket p = new ByteBufOutPacket();
        p.writeShort(0x0E);
        p.writeShort(mapleVersion);
        p.writeShort(1);
        p.writeByte(49);
        p.writeBytes(recvIv.getBytes());
        p.writeBytes(sendIv.getBytes());
        p.writeByte(8);
        return p;
    }

    public static Packet getPing() {
        return OutPacket.create(SendOpcode.PING);
    }

    public static Packet getLoginFailed(int reason) {
        OutPacket p = OutPacket.create(SendOpcode.LOGIN_STATUS);
        p.writeByte(reason);
        p.writeByte(0);
        p.writeInt(0);
        return p;
    }

    public static Packet getAfterLoginError(int reason) {//same as above o.o
        OutPacket p = OutPacket.create(SendOpcode.SELECT_CHARACTER_BY_VAC);
        p.writeShort(reason);//using other types than stated above = CRASH
        return p;
    }

    public static Packet getPermBan(byte reason) {
        final OutPacket p = OutPacket.create(SendOpcode.LOGIN_STATUS);
        p.writeByte(2); // Account is banned
        p.writeByte(0);
        p.writeInt(0);
        p.writeByte(0);
        p.writeLong(getTime(-1));
        return p;
    }

    public static Packet getAuthSuccess(Client c) {
        Server.getInstance().loadAccountCharacters(c);    // locks the login session until data is recovered from the cache or the DB.
        Server.getInstance().loadAccountStorages(c);

        final OutPacket p = OutPacket.create(SendOpcode.LOGIN_STATUS);
        p.writeInt(0);
        p.writeShort(0);
        p.writeInt(c.getAccID());
        p.writeByte(c.getGender());

        p.writeBool(false);    // thanks Steve(kaito1410) for pointing the GM account boolean here
        p.writeByte(0);  // Admin Byte. 0x80,0x40,0x20.. Rubbish.
        p.writeByte(0); // Country Code.

        p.writeString(c.getAccountName());
        p.writeByte(0);

        p.writeByte(0); // IsQuietBan
        p.writeLong(0);//IsQuietBanTimeStamp
        p.writeLong(0); //CreationTimeStamp

        p.writeInt(1); // 1: Remove the "Select the world you want to play in"

        p.writeByte(1); // 0 = Pin-System Enabled, 1 = Disabled
        p.writeByte(2); // 0 = Register PIC, 1 = Ask for PIC, 2 = Disabled

        return p;
    }

    private static Packet pinOperation(byte mode) {
        OutPacket p = OutPacket.create(SendOpcode.CHECK_PINCODE);
        p.writeByte(mode);
        return p;
    }

    public static Packet pinRegistered() {
        OutPacket p = OutPacket.create(SendOpcode.UPDATE_PINCODE);
        p.writeByte(0);
        return p;
    }

    public static Packet requestPin() {
        return pinOperation((byte) 4);
    }

    public static Packet requestPinAfterFailure() {
        return pinOperation((byte) 2);
    }

    public static Packet registerPin() {
        return pinOperation((byte) 1);
    }

    public static Packet pinAccepted() {
        return pinOperation((byte) 0);
    }

    public static Packet wrongPic() {
        OutPacket p = OutPacket.create(SendOpcode.CHECK_SPW_RESULT);
        p.writeByte(0);
        return p;
    }

    public static Packet getServerList(int serverId, String serverName, int flag, String eventmsg, List<dev.jaczerob.delfino.grpc.proto.Channel> channelLoad) {
        final OutPacket p = OutPacket.create(SendOpcode.SERVERLIST);
        p.writeByte(serverId);
        p.writeString(serverName);
        p.writeByte(flag);
        p.writeString(eventmsg);
        p.writeByte(100); // rate modifier, don't ask O.O!
        p.writeByte(0); // event xp * 2.6 O.O!
        p.writeByte(100); // rate modifier, don't ask O.O!
        p.writeByte(0); // drop rate * 2.6
        p.writeByte(0);
        p.writeByte(channelLoad.size());
        for (final var ch : channelLoad) {
            p.writeString(serverName + "-" + ch.getId());
            p.writeInt(ch.getCapacity());

            // thanks GabrielSin for this channel packet structure part
            p.writeByte(1);// nWorldID
            p.writeByte(ch.getId() - 1);// nChannelID
            p.writeBool(false);// bAdultChannel
        }
        p.writeShort(0);
        return p;
    }

    public static Packet getEndOfServerList() {
        OutPacket p = OutPacket.create(SendOpcode.SERVERLIST);
        p.writeByte(0xFF);
        return p;
    }

    public static Packet getServerStatus(int status) {
        OutPacket p = OutPacket.create(SendOpcode.SERVERSTATUS);
        p.writeShort(status);
        return p;
    }

    public static Packet getServerIP(InetAddress inetAddr, int port, int clientId) {
        final OutPacket p = OutPacket.create(SendOpcode.SERVER_IP);
        p.writeShort(0);
        byte[] addr = inetAddr.getAddress();
        p.writeBytes(addr);
        p.writeShort(port);
        p.writeInt(clientId);
        p.writeBytes(new byte[]{0, 0, 0, 0, 0});
        return p;
    }

    public static Packet getCharList(Client c, int status) {
        // TODO: Load characters from RPC
        final var p = OutPacket.create(SendOpcode.CHARLIST);
        p.writeByte(status);

        final var chars = Server.getInstance().loadCharacters(c.getAccID());
        p.writeByte((byte) chars.size());

        for (final var chr : chars) {
            addCharEntry(p, chr, false);
        }


        p.writeByte(1);
        p.writeInt(c.getCharacterSlots());
        System.out.println(Arrays.toString(p.getBytes()));
        return p;
    }

    public static Packet getRelogResponse() {
        OutPacket p = OutPacket.create(SendOpcode.RELOG_RESPONSE);
        p.writeByte(1);//1 O.O Must be more types ):
        return p;
    }

    public static Packet showAllCharacter(int totalWorlds, int totalChrs) {
        OutPacket p = OutPacket.create(SendOpcode.VIEW_ALL_CHAR);
        p.writeByte(totalChrs > 0 ? 1 : 5); // 2: already connected to server, 3 : unk error (view-all-characters), 5 : cannot find any
        p.writeInt(totalWorlds);
        p.writeInt(totalChrs);
        return p;
    }

    public static Packet charNameResponse(String charname, boolean nameUsed) {
        final OutPacket p = OutPacket.create(SendOpcode.CHAR_NAME_RESPONSE);
        p.writeString(charname);
        p.writeByte(nameUsed ? 1 : 0);
        return p;
    }

    public static Packet deleteCharResponse(int cid, int state) {
        final OutPacket p = OutPacket.create(SendOpcode.DELETE_CHAR_RESPONSE);
        p.writeInt(cid);
        p.writeByte(state);
        return p;
    }

    public static Packet selectWorld(int world) {
        final OutPacket p = OutPacket.create(SendOpcode.LAST_CONNECTED_WORLD);
        p.writeInt(world);//According to GMS, it should be the world that contains the most characters (most active)
        return p;
    }

    public static Packet sendRecommended(List<Pair<Integer, String>> worlds) {
        final OutPacket p = OutPacket.create(SendOpcode.RECOMMENDED_WORLD_MESSAGE);
        p.writeByte(worlds.size());//size
        for (Pair<Integer, String> world : worlds) {
            p.writeInt(world.getLeft());
            p.writeString(world.getRight());
        }
        return p;
    }

    public static Packet showAllCharacterInfo(int worldid, List<Character> chars, boolean usePic) {
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

    public static Packet customPacket(byte[] packet) {
        OutPacket p = new ByteBufOutPacket();
        p.writeBytes(packet);
        return p;
    }
}
