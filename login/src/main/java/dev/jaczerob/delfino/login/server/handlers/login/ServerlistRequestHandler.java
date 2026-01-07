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
package dev.jaczerob.delfino.login.server.handlers.login;

import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.constants.game.GameConstants;
import dev.jaczerob.delfino.login.AbstractPacketHandler;
import dev.jaczerob.delfino.login.packet.InPacket;
import dev.jaczerob.delfino.login.server.Server;
import dev.jaczerob.delfino.login.server.world.World;
import dev.jaczerob.delfino.login.tools.PacketCreator;

import java.util.List;

public final class ServerlistRequestHandler extends AbstractPacketHandler {

    @Override
    public final void handlePacket(InPacket p, @org.jetbrains.annotations.UnknownNullability LoginClient c) {
        Server server = Server.getInstance();
        List<World> worlds = server.getWorlds();
        c.requestedServerlist(worlds.size());

        for (World world : worlds) {
            c.sendPacket(PacketCreator.getServerList(world.getId(), GameConstants.WORLD_NAMES[world.getId()], world.getFlag(), world.getEventMessage(), world.getChannels()));
        }
        c.sendPacket(PacketCreator.getEndOfServerList());
        c.sendPacket(PacketCreator.selectWorld(0));//too lazy to make a check lol
        c.sendPacket(PacketCreator.sendRecommended(server.worldRecommendedList()));
    }
}