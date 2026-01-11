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
package dev.jaczerob.delfino.network.tools;

import dev.jaczerob.delfino.network.encryption.InitializationVector;
import dev.jaczerob.delfino.network.packets.ByteBufOutPacket;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.network.packets.Packet;

public abstract class PacketCreator {
    private final short mapleVersion;

    protected PacketCreator(final short mapleVersion) {
        this.mapleVersion = mapleVersion;
    }

    public final Packet getHello(final InitializationVector sendIv, final InitializationVector recvIv) {
        OutPacket p = new ByteBufOutPacket();
        p.writeShort(0x0E);
        p.writeShort(this.mapleVersion);
        p.writeShort(1);
        p.writeByte(49);
        p.writeBytes(recvIv.getBytes());
        p.writeBytes(sendIv.getBytes());
        p.writeByte(8);
        return p;
    }
}
