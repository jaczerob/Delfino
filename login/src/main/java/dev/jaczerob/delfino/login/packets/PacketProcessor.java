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
package dev.jaczerob.delfino.login.packets;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.network.packets.PacketHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public final class PacketProcessor {
    private static PacketProcessor INSTANCE;

    private final Map<Integer, PacketHandler<LoginClient>> packetHandlerMap = new ConcurrentHashMap<>();

    private final List<PacketHandler<LoginClient>> packetHandlers;

    public PacketProcessor(final List<PacketHandler<LoginClient>> packetHandlers) {
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

    public PacketHandler<LoginClient> getHandler(final short packetId) {
        return this.packetHandlerMap.get((int) packetId);
    }

    public static PacketProcessor getProcessor() {
        return INSTANCE;
    }

    private void registerHandler(final PacketHandler<LoginClient> handler) {
        this.packetHandlerMap.put(handler.getOpcode().getValue(), handler);
    }
}
