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
package dev.jaczerob.delfino.login.net.server.handlers;

import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.net.PacketHandler;
import dev.jaczerob.delfino.login.net.opcodes.RecvOpcode;
import dev.jaczerob.delfino.login.net.packet.InPacket;
import dev.jaczerob.delfino.login.tools.PacketCreator;
import org.springframework.stereotype.Component;

@Component
public class CustomPacketHandler implements PacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CUSTOM_PACKET;
    }

    @Override
    public void handlePacket(final InPacket p, final Client c) {
        if (p.available() > 0 && c.getGMLevel() == 4) {//w/e
            c.sendPacket(PacketCreator.customPacket(p.readBytes(p.available())));
        }
    }

    @Override
    public boolean validateState(Client c) {
        return true;
    }
}
