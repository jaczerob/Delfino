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

import dev.jaczerob.delfino.grpc.proto.account.AccountServiceGrpc;
import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import org.springframework.stereotype.Component;

@Component
public final class GuestLoginHandler extends LoginPasswordHandler {
    public GuestLoginHandler(final AccountServiceGrpc.AccountServiceBlockingV2Stub accountService) {
        super(accountService);
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.GUEST_LOGIN;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client) {
        client.sendPacket(LoginPacketCreator.getInstance().sendGuestTOS());
        super.handlePacket(packet, client);
    }
}
