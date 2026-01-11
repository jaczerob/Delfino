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
package dev.jaczerob.delfino.login.net.server.handlers.login;

import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.net.AbstractPacketHandler;
import dev.jaczerob.delfino.login.net.opcodes.RecvOpcode;
import dev.jaczerob.delfino.login.net.packet.InPacket;
import dev.jaczerob.delfino.login.net.server.Server;
import dev.jaczerob.delfino.login.tools.PacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public final class CharSelectedHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(CharSelectedHandler.class);

    private final Server server;

    public CharSelectedHandler(final Server server) {
        this.server = server;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CHAR_SELECT;
    }

    @Override
    public void handlePacket(final InPacket p, final Client c) {
        int charId = p.readInt();

        p.readString();
        p.readString();

        final var socket = this.server.getInetSocket();
        if (socket == null) {
            c.sendPacket(PacketCreator.getAfterLoginError(10));
            return;
        }

        try {
            c.sendPacket(PacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
        } catch (final UnknownHostException exc) {
            log.error("Failed to resolve server address", exc);
        }
    }
}