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

import com.google.protobuf.Empty;
import dev.jaczerob.delfino.grpc.proto.World;
import dev.jaczerob.delfino.grpc.proto.WorldServiceGrpc;
import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.grpc.StatusException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class ServerlistRequestHandler extends AbstractPacketHandler {
    private final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub;

    public ServerlistRequestHandler(final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub) {
        this.worldServiceStub = worldServiceStub;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.SERVERLIST_REQUEST;
    }

    @Override
    public void handlePacket(final InPacket p, final LoginClient c) {
        List<World> worlds;
        try {
            worlds = this.worldServiceStub.getWorlds(Empty.newBuilder().build()).getWorldsList();
        } catch (final StatusException exc) {
            worlds = List.of();
        }

        for (final var world : worlds) {
            c.sendPacket(LoginPacketCreator.getInstance().getServerList(world.getId(), world.getName(), world.getFlag(), world.getMessages().getEvent(), world.getChannelsList()));
        }

        c.sendPacket(LoginPacketCreator.getInstance().getEndOfServerList());
        c.sendPacket(LoginPacketCreator.getInstance().selectWorld(0));
        c.sendPacket(LoginPacketCreator.getInstance().sendRecommended(List.of()));
    }
}