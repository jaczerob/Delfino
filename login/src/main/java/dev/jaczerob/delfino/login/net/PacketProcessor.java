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
package dev.jaczerob.delfino.login.net;

import dev.jaczerob.delfino.login.net.opcodes.RecvOpcode;
import dev.jaczerob.delfino.login.net.server.handlers.CustomPacketHandler;
import dev.jaczerob.delfino.login.net.server.handlers.KeepAliveHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.AcceptToSHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.AfterLoginHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.CharSelectedHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.CharSelectedWithPicHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.CharlistRequestHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.CheckCharNameHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.CreateCharHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.DeleteCharHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.GuestLoginHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.LoginPasswordHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.RegisterPicHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.RegisterPinHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.RelogRequestHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.ServerStatusRequestHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.ServerlistRequestHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.SetGenderHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.ViewAllCharHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.ViewAllCharRegisterPicHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.ViewAllCharSelectedHandler;
import dev.jaczerob.delfino.login.net.server.handlers.login.ViewAllCharSelectedWithPicHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public final class PacketProcessor {
    private static PacketProcessor INSTANCE;

    private final Map<Integer, PacketHandler> packetHandlerMap = new ConcurrentHashMap<>();

    private final List<PacketHandler> packetHandlers;

    public PacketProcessor(final List<PacketHandler> packetHandlers) {
        this.packetHandlers = packetHandlers;
    }

    @PostConstruct
    public void init() {
        INSTANCE = this;
        this.packetHandlers.forEach(this::registerHandler);
    }

    public static PacketProcessor getLoginServerProcessor() {
        return getProcessor();
    }

    public PacketHandler getHandler(final short packetId) {
        return this.packetHandlerMap.get((int) packetId);
    }

    public static PacketProcessor getProcessor() {
        return INSTANCE;
    }

    private void registerHandler(final PacketHandler handler) {
        this.packetHandlerMap.put(handler.getOpcode().getValue(), handler);
    }
}
