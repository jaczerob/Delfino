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
package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.server.LoginServer;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public final class ViewAllCharSelectedHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(ViewAllCharSelectedHandler.class);
    private final LoginServer server;

    public ViewAllCharSelectedHandler(
            final LoginServer server,
            final SessionCoordinator sessionCoordinator,
            final LoginPacketCreator loginPacketCreator
    ) {
        super(sessionCoordinator, loginPacketCreator);
        this.server = server;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.PICK_ALL_CHAR;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client, final ChannelHandlerContext context) {
        final var charId = packet.readInt();
        packet.readInt();
        packet.readString();
        packet.readString();

        final var socket = server.getInetSocket();
        if (socket == null) {
            context.writeAndFlush(this.loginPacketCreator.getAfterLoginError(10));
            return;
        }

        try {
            context.writeAndFlush(this.loginPacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
        } catch (final UnknownHostException exc) {
            log.error("Failed to resolve server address", exc);
        }
    }
}
