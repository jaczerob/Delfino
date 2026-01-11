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

import dev.jaczerob.delfino.grpc.proto.account.Account;
import dev.jaczerob.delfino.grpc.proto.account.AccountRequest;
import dev.jaczerob.delfino.grpc.proto.account.AccountServiceGrpc;
import dev.jaczerob.delfino.grpc.proto.character.Character;
import dev.jaczerob.delfino.grpc.proto.character.CharacterServiceGrpc;
import dev.jaczerob.delfino.grpc.proto.character.CharactersRequest;
import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.packets.coordinators.session.HWID;
import dev.jaczerob.delfino.login.tools.HexTool;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoginPasswordHandler extends AbstractPacketHandler {
    private final Logger log = LoggerFactory.getLogger(LoginPasswordHandler.class);
    private final AccountServiceGrpc.AccountServiceBlockingV2Stub accountService;
    private final CharacterServiceGrpc.CharacterServiceBlockingV2Stub characterService;

    public LoginPasswordHandler(
            final AccountServiceGrpc.AccountServiceBlockingV2Stub accountService,
            final CharacterServiceGrpc.CharacterServiceBlockingV2Stub characterService
    ) {
        this.accountService = accountService;
        this.characterService = characterService;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.LOGIN_PASSWORD;
    }

    @Override
    public boolean validateState(final LoginClient client) {
        return !client.isLoggedIn();
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client) {
        final var remoteHost = client.getRemoteAddress();
        if (remoteHost.contentEquals("null")) {
            client.sendPacket(LoginPacketCreator.getInstance().getLoginFailed(14));
            return;
        }

        final var username = packet.readString();
        final Account account;
        final List<Character> characters;
        try {
            account = this.accountService.getAccount(AccountRequest.newBuilder().setUsername(username).build()).getAccount();
            characters = this.characterService.getCharacters(CharactersRequest.newBuilder().setAccountId(account.getId()).build()).getCharactersList();
        } catch (final Exception exc) {
            log.error("Error retrieving account for username {}", username, exc);
            client.sendPacket(LoginPacketCreator.getInstance().getLoginFailed(5));
            return;
        }

        final var pwd = packet.readString();
        client.setAccount(account);
        client.setCharacters(characters);
        client.setAccountName(username);

        packet.skip(6);
        final var hwidNibbles = packet.readBytes(4);
        final var hwid = new HWID(HexTool.toCompactHexString(hwidNibbles));
        final var loginok = client.login(username, pwd, hwid);

        if (loginok != 0) {
            client.sendPacket(LoginPacketCreator.getInstance().getLoginFailed(loginok));
            return;
        }

        if (client.finishLogin() == 0) {
            client.sendPacket(LoginPacketCreator.getInstance().getAuthSuccess(client));
        } else {
            client.sendPacket(LoginPacketCreator.getInstance().getLoginFailed(7));
        }
    }
}
