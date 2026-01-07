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
package dev.jaczerob.delfino.login;

import dev.jaczerob.delfino.login.opcodes.RecvOpcode;
import dev.jaczerob.delfino.login.server.handlers.CustomPacketHandler;
import dev.jaczerob.delfino.login.server.handlers.KeepAliveHandler;
import dev.jaczerob.delfino.login.server.handlers.login.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PacketProcessor {
    private static final Logger log = LoggerFactory.getLogger(PacketProcessor.class);

    private PacketHandler[] handlers;

    private PacketProcessor() {
        var maxRecvOp = 0;
        for (final var op : RecvOpcode.values())
            if (op.getValue() > maxRecvOp)
                maxRecvOp = op.getValue();

        this.handlers = new PacketHandler[maxRecvOp + 1];
        this.reset();
    }

    public Optional<PacketHandler> getHandler(final short packetId) {
        if (packetId > handlers.length) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.handlers[packetId]);
    }

    public void registerHandler(RecvOpcode code, PacketHandler handler) {
        try {
            handlers[code.getValue()] = handler;
        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("Error registering handler {}", code.name(), e);
        }
    }

    public void reset() {
        this.handlers = new PacketHandler[handlers.length];

        registerCommonHandlers();
        registerLoginHandlers();
    }

    private void registerCommonHandlers() {
        this.registerHandler(RecvOpcode.PONG, new KeepAliveHandler());
        this.registerHandler(RecvOpcode.CUSTOM_PACKET, new CustomPacketHandler());
    }

    private void registerLoginHandlers() {
        this.registerHandler(RecvOpcode.ACCEPT_TOS, new AcceptToSHandler());
        this.registerHandler(RecvOpcode.AFTER_LOGIN, new AfterLoginHandler());
        this.registerHandler(RecvOpcode.SERVERLIST_REREQUEST, new ServerlistRequestHandler());
        this.registerHandler(RecvOpcode.CHARLIST_REQUEST, new CharlistRequestHandler());
        this.registerHandler(RecvOpcode.CHAR_SELECT, new CharSelectedHandler());
        this.registerHandler(RecvOpcode.LOGIN_PASSWORD, new LoginPasswordHandler());
        this.registerHandler(RecvOpcode.RELOG, new RelogRequestHandler());
        this.registerHandler(RecvOpcode.SERVERLIST_REQUEST, new ServerlistRequestHandler());
        this.registerHandler(RecvOpcode.SERVERSTATUS_REQUEST, new ServerStatusRequestHandler());
        this.registerHandler(RecvOpcode.CHECK_CHAR_NAME, new CheckCharNameHandler());
        this.registerHandler(RecvOpcode.CREATE_CHAR, new CreateCharHandler());
        this.registerHandler(RecvOpcode.DELETE_CHAR, new DeleteCharHandler());
        this.registerHandler(RecvOpcode.VIEW_ALL_CHAR, new ViewAllCharHandler());
        this.registerHandler(RecvOpcode.PICK_ALL_CHAR, new ViewAllCharSelectedHandler());
        this.registerHandler(RecvOpcode.REGISTER_PIN, new RegisterPinHandler());
        this.registerHandler(RecvOpcode.GUEST_LOGIN, new GuestLoginHandler());
        this.registerHandler(RecvOpcode.REGISTER_PIC, new RegisterPicHandler());
        this.registerHandler(RecvOpcode.CHAR_SELECT_WITH_PIC, new CharSelectedWithPicHandler());
        this.registerHandler(RecvOpcode.SET_GENDER, new SetGenderHandler());
        this.registerHandler(RecvOpcode.VIEW_ALL_WITH_PIC, new ViewAllCharSelectedWithPicHandler());
        this.registerHandler(RecvOpcode.VIEW_ALL_PIC_REGISTER, new ViewAllCharRegisterPicHandler());
    }
}
